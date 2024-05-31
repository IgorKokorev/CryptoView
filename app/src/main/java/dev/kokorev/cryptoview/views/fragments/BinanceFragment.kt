package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.charts.Stock
import com.anychart.core.stock.Plot
import com.anychart.data.Table
import com.anychart.enums.StockSeriesType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.kokorev.binance_api.entity.BinanceKLineInterval
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.data.Constants
import dev.kokorev.cryptoview.databinding.FragmentBinanceBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.BinanceViewModel
import dev.kokorev.room_db.core_api.entity.BinanceSymbolDB
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

class BinanceFragment : Fragment() {
    private lateinit var binding: FragmentBinanceBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: BinanceViewModel by viewModels<BinanceViewModel>()
    private lateinit var symbol: String
    
    private var binanceSymbols: List<BinanceSymbolDB> = arrayListOf()
    private var binanceSymbolIndexListener: BehaviorSubject<Int> =
        BehaviorSubject.create() // index of the symbol in the list to show
    private var binanceSymbol: BinanceSymbolDB? = null

    // check if chart is ready toreceive data
    private val checkChartReady: BehaviorSubject<Boolean> = BehaviorSubject.create()
    private var isChartReady: Boolean = false
    private lateinit var table: Table
    private var chartData: List<DataEntry> = arrayListOf()
    private lateinit var stock: Stock
    private lateinit var plot: Plot

    private val intervals: List<BinanceKLineInterval> = listOf(
        BinanceKLineInterval.SECOND,
        BinanceKLineInterval.MINUTE,
        BinanceKLineInterval.MINUTE3,
        BinanceKLineInterval.MINUTE5,
        BinanceKLineInterval.MINUTE15,
        BinanceKLineInterval.MINUTE30,
        BinanceKLineInterval.HOUR,
        BinanceKLineInterval.HOUR2,
        BinanceKLineInterval.HOUR4,
        BinanceKLineInterval.HOUR6,
        BinanceKLineInterval.HOUR8,
        BinanceKLineInterval.HOUR12,
        BinanceKLineInterval.DAY,
        BinanceKLineInterval.DAY3,
        BinanceKLineInterval.WEEK,
        BinanceKLineInterval.MONTH,
    )

    private val intervalListener: BehaviorSubject<BinanceKLineInterval> = BehaviorSubject.create()
    private var interval = BinanceKLineInterval.HOUR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentBinanceBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
        
        symbol = arguments?.getString(Constants.COIN_SYMBOL) ?: ""
        
        setupIntervalSpinner()
        setupChart()

        BehaviorSubject.combineLatest(
            intervalListener,
            binanceSymbolIndexListener
        ) { newInterval, newBSIndex ->
            interval = newInterval
            if (newBSIndex >= 0) binanceSymbol = binanceSymbols.get(newBSIndex)
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (binanceSymbol != null )getChartData()
            }
            .addTo(autoDisposable)

        findBinanceSymbols()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    private fun getChartData() {
        viewModel.remoteApi.getBinanceKLines(symbol = binanceSymbol!!.symbol, interval = interval, limit = Constants.BINANCE_KLINES_LIMIT)
            .subscribe({ klineList ->
                Log.d(this.javaClass.simpleName, "Received ${klineList.size} candles")
                val newData = klineList.map {
                    Converter.binanceKLineToOHLCData(it)
                }
                if (chartData.isNotEmpty()) {
                    val start = getX(0)
                    val end = getX(chartData.size - 1)
                    table.remove(start, end)
                }
                if (newData.isNotEmpty()) {
                    chartData = newData
                    table.addData(chartData)
                    if (newData.size > Constants.NUM_BARS_TO_SHOW) {
                        stock.selectRange(
                            getX(chartData.size - Constants.NUM_BARS_TO_SHOW),
                            getX(chartData.size - 1)
                        )
                    }
                }
            },
                {
                    Log.d(
                        this.javaClass.simpleName,
                        "Error: ${it.localizedMessage}, ${it.stackTrace}"
                    )
                }
            )
            .addTo(autoDisposable)
    }
    
    // get "x" value as string from the chartData at index
    private fun getX(index: Int) = try {
        chartData.get(index).getValue("x") as String
    } catch (e: Exception) {
        ""
    }
    
    // set chart without data
    private fun setupChart() {
        Observable.just(1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                table = Table.instantiate("x")
                val mapping =
                    table.mapAs("{open: 'open', high: 'high', low: 'low', close: 'close'}")
                stock = AnyChart.stock()
                plot = stock.plot(0)
                plot.noData("No data to display")
                plot.legend(false)
                plot.yGrid(true)
                    .xGrid(true)
                    .yMinorGrid(true)
                    .xMinorGrid(true)
                plot.ema(table.mapAs("{value: 'close'}"), 20.0, StockSeriesType.LINE)
                plot.ema(table.mapAs("{value: 'close'}"), 50.0, StockSeriesType.LINE)
                plot.ema(table.mapAs("{value: 'close'}"), 100.0, StockSeriesType.LINE)
                plot.ohlc(mapping)
//                    .name(binanceSymbol!!.symbol)
                    .legendItem(
                        """{ iconType: 'rising-falling' }"""
                    )

                stock.scroller().ohlc(mapping)
                Log.d(
                    this.javaClass.simpleName,
                    "setupChart. End"
                )
            }
            .subscribe {
                binding.anyChartView.setChart(stock)
                isChartReady = true
                checkChartReady.onNext(true)
            }
            .addTo(autoDisposable)
    }

    private fun findBinanceSymbols() {
        viewModel.repository.findBinanceSymbolsByBaseAsset(symbol)
            .subscribe { list ->
                if (list.isEmpty()) {
                    MaterialAlertDialogBuilder(binding.root.context, R.style.CVDialogStyle )
                        .setTitle("Binance market info")
                        .setMessage("Coin ${symbol} is not listed on Binance.")
                        .setPositiveButton(R.string.ok) { dialogEmptyInput, whichEmptyInput ->
                            requireActivity().supportFragmentManager.popBackStack()
                        }
                        .show()
                } else {
                    binanceSymbols = list
                    val indexOfFirstAsset =
                        list.indexOfFirst { symbol -> symbol.quoteAsset == Constants.BINANCE_FIRST_ASSET }
                    binanceSymbolIndexListener.onNext(indexOfFirstAsset)
                    setupSymbolSpinner(list, indexOfFirstAsset)
                    Log.d(
                        this.javaClass.simpleName,
                        "Found ${list.size} Binance symbols, USDT index: ${binanceSymbolIndexListener}"
                    )
                }
            }
            .addTo(autoDisposable)
    }

    private fun setupSymbolSpinner(list: List<BinanceSymbolDB>, indexOfFirstAsset: Int) {
        val spinnerItems = list.map { it.symbol }
        val spinnerAdapter =
            ArrayAdapter<String>(binding.root.context, R.layout.spinner_text_item, spinnerItems)
        binding.symbolSelector.adapter = spinnerAdapter
        binding.symbolSelector.setSelection(indexOfFirstAsset)
        binding.symbolSelector.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    binanceSymbolIndexListener.onNext(position)
                    Log.d(
                        this.javaClass.simpleName,
                        "Selected symbol: ${binanceSymbols.get(position).symbol}"
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

    private fun setupIntervalSpinner() {
        val spinnerItems = intervals.map { it.value }
        val spinnerAdapter =
            ArrayAdapter<String>(binding.root.context, R.layout.spinner_text_item, spinnerItems)
        binding.intervalSelector.adapter = spinnerAdapter
        binding.intervalSelector.setSelection(intervals.indexOf(interval))
        binding.intervalSelector.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    intervalListener.onNext(intervals.get(position))
                    Log.d(
                        this.javaClass.simpleName,
                        "Selected interval: ${intervals.get(position)}"
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

}