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
import dev.kokorev.binance_api.entity.BinanceKLineInterval
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.data.Constants
import dev.kokorev.cryptoview.databinding.FragmentBinanceBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.CoinViewModel
import dev.kokorev.room_db.core_api.entity.BinanceSymbolDB
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

class BinanceFragment : Fragment() {
    private lateinit var binding: FragmentBinanceBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: CoinViewModel by viewModels<CoinViewModel>(
        ownerProducer = { requireParentFragment() }
    )
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
        setupIntervalSpinner()
        setupChart()

        BehaviorSubject.combineLatest(intervalListener, binanceSymbolIndexListener, BiFunction { newInterval, newBSIndex ->
            interval = newInterval
            binanceSymbol = binanceSymbols.get(newBSIndex)
        })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                getChartData()
            }
            .addTo(autoDisposable)

        findBinanceSymbols()
    }

    private fun getChartData() {
        viewModel.remoteApi.getBinanceKLines(symbol = binanceSymbol!!.symbol, interval = interval)
            .subscribe({ klineList ->
                Log.d(this.javaClass.simpleName, "Received ${klineList.size} candles")
                val newData = klineList.map {
                    Converter.binanceKLineToOHLCData(it)
                }
//                setupChart()
                if (chartData.isNotEmpty()) {
                    val start = chartData.get(0).getValue("x") as String
                    val end = chartData.get(chartData.size - 1).getValue("x") as String
                    table.remove(start, end)
                }
                chartData = newData
//                table.remove(chartData.get(0).)
                table.addData(chartData)




/*                val jsBase = table.jsBase
                binding.anyChartView.jsListener.onJsLineAdd(
                    String.format(
                        Locale.US,
                        "$jsBase.data(%s);", arrayToString(chartData)
                    ))*/




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





    protected fun arrayToString(data: List<DataEntry>?): String {
        if (data == null) {
            return ""
        }

        val resultData = StringBuilder()
        resultData.append("[")
        for (dataEntry in data) {
            resultData.append(dataEntry.generateJs()).append(", ")
        }
        resultData.setLength(resultData.length - 1)
        resultData.append("]")

        return resultData.toString()
    }




    private fun setupChart() {
        Observable.just(1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                table = Table.instantiate("x")
//                table.addData(chartData)
                val mapping =
                    table.mapAs("{open: 'open', high: 'high', low: 'low', close: 'close'}")
                stock = AnyChart.stock()
                plot = stock.plot(0)
                plot.yGrid(true)
                    .xGrid(true)
                    .yMinorGrid(false)
                    .xMinorGrid(false)
//        plot.ema(table.mapAs("{value: 'close'}"), 20.0, StockSeriesType.LINE)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    private fun findBinanceSymbols() {
        viewModel.repository.findBinanceSymbolsByBaseAsset(viewModel.symbol)
            .subscribe { list ->
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