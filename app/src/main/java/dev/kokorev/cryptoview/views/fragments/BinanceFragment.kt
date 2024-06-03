package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.charts.Stock
import com.anychart.core.stock.Plot
import com.anychart.core.stock.series.OHLC
import com.anychart.data.Table
import com.anychart.data.TableMapping
import com.anychart.enums.StockSeriesType
import com.anychart.graphics.vector.SolidFill
import com.anychart.graphics.vector.StrokeLineCap
import com.anychart.graphics.vector.StrokeLineJoin
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.kokorev.binance_api.entity.BinanceKLineInterval
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.FragmentBinanceBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.cryptoview.utils.NumbersUtils
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
    
    // list of Binance tickers with given symbol
    private var binanceSymbols: List<BinanceSymbolDB> = arrayListOf()
    private var binanceSymbolIndexBehaviour: BehaviorSubject<Int> =
        BehaviorSubject.create() // index of the symbol in the list to show
    private var binanceSymbol: BinanceSymbolDB? = null
    
    // chart fields
    private lateinit var table: Table
    private lateinit var mapping: TableMapping
    private lateinit var stock: Stock
    private lateinit var plotMain: Plot
    private lateinit var plotStochastic: Plot
    private lateinit var plotMACD: Plot
    private lateinit var plotMomentum: Plot
    private lateinit var plotRSI: Plot
    private var isStochasticEnabled: Boolean = false
    private var isMACDEnabled: Boolean = false
    private var isMomentumEnabled: Boolean = false
    private var isRSIEnabled: Boolean = false
    private lateinit var ohlc: OHLC
    private var chartData: List<DataEntry> = arrayListOf()
    
    // chart intervals to select from
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
    
    private val intervalBehaviour: BehaviorSubject<BinanceKLineInterval> = BehaviorSubject.create()
    private var interval = BinanceKLineInterval.DAY
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentBinanceBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
        
        symbol = arguments?.getString(Constants.COIN_SYMBOL) ?: ""
        
        setupIntervalSpinner()
        setupChart()
        findBinanceSymbols()
        
        // when ticker is selected get and show market info
        binanceSymbolIndexBehaviour
            .subscribe {
                if (it >= 0) {
                    binanceSymbol = binanceSymbols[it]
                    getMarketData()
                }
            }
            .addTo(autoDisposable)
        
        // when interval or ticker is changed redraw the chart with new data
        BehaviorSubject.combineLatest(
            intervalBehaviour,
            binanceSymbolIndexBehaviour
        ) { newInterval, newBSIndex ->
            interval = newInterval
            if (newBSIndex >= 0) binanceSymbol = binanceSymbols[newBSIndex]
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (binanceSymbol != null) getChartData()
            }
            .addTo(autoDisposable)
        
        // Turn stochastic on and off
        binding.stochastic.setOnClickListener {
            isStochasticEnabled = !isStochasticEnabled
            setStochasticPlotAndButtonVisibility()
        }
        binding.macd.setOnClickListener {
            isMACDEnabled = !isMACDEnabled
            setMACDPlotAndButtonVisibility()
        }
        binding.momentum.setOnClickListener {
            isMomentumEnabled = !isMomentumEnabled
            setMomentumPlotAndButtonVisibility()
        }
        binding.rsi.setOnClickListener {
            isRSIEnabled = !isRSIEnabled
            setRSIPlotAndButtonVisibility()
        }
    }
    
    private fun setStochasticPlotAndButtonVisibility() {
        plotStochastic.enabled(isStochasticEnabled)
        binding.stochastic.background = if (isStochasticEnabled) ContextCompat.getDrawable(
            binding.root.context,
            R.drawable.rounded_rectangle_accent
        )
        else ContextCompat.getDrawable(binding.root.context, R.drawable.rounded_rectangle_base)
    }
    
    
    private fun setMACDPlotAndButtonVisibility() {
        plotMACD.enabled(isMACDEnabled)
        binding.macd.background = if (isMACDEnabled) ContextCompat.getDrawable(
            binding.root.context,
            R.drawable.rounded_rectangle_accent
        )
        else ContextCompat.getDrawable(binding.root.context, R.drawable.rounded_rectangle_base)
    }
    
    private fun setMomentumPlotAndButtonVisibility() {
        plotMomentum.enabled(isMomentumEnabled)
        binding.momentum.background = if (isMomentumEnabled) ContextCompat.getDrawable(
            binding.root.context,
            R.drawable.rounded_rectangle_accent
        )
        else ContextCompat.getDrawable(binding.root.context, R.drawable.rounded_rectangle_base)
    }
    
    
    private fun setRSIPlotAndButtonVisibility() {
        plotRSI.enabled(isRSIEnabled)
        binding.rsi.background = if (isRSIEnabled) ContextCompat.getDrawable(
            binding.root.context,
            R.drawable.rounded_rectangle_accent
        )
        else ContextCompat.getDrawable(binding.root.context, R.drawable.rounded_rectangle_base)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
    
    private fun getMarketData() {
        if (binanceSymbol != null) {
            val ticker = binanceSymbol!!
            viewModel.remoteApi.getBinance24hrStats(ticker.symbol)
                .subscribe({ stats ->
                    binding.baseAsset.text = ticker.baseAsset
                    binding.quoteAsset.text = ticker.quoteAsset
                    binding.lastPrice.text = NumbersUtils.formatPrice(stats.lastPrice.toDouble())
                    NumbersUtils.setChangeView(
                        stats.priceChangePercent?.toDouble(),
                        binding.root.context,
                        binding.change24h,
                        "%"
                    )
                    binding.volume.text = NumbersUtils.formatBigNumber(stats.volume.toDouble())
                    binding.value.text = NumbersUtils.formatBigNumber(stats.quoteVolume.toDouble())
                    binding.openPrice.text = NumbersUtils.formatPrice(stats.openPrice.toDouble())
                    binding.highPrice.text = NumbersUtils.formatPrice(stats.highPrice.toDouble())
                    binding.lowPrice.text = NumbersUtils.formatPrice(stats.lowPrice.toDouble())
                },
                    {
                        Log.d(
                            this.javaClass.simpleName,
                            "Error getting Binance 24 hr stat for symbol ${binanceSymbol!!.symbol}"
                        )
                    })
                .addTo(autoDisposable)
        }
    }
    
    private fun getChartData() {
        viewModel.remoteApi.getBinanceKLines(
            symbol = binanceSymbol!!.symbol,
            interval = interval,
            limit = Constants.BINANCE_KLINES_LIMIT
        )
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
                    ohlc.name(binanceSymbol!!.symbol)
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
                val backgroundColorString = "#" + getColorHex(R.color.base1)
                val backgroundFill = SolidFill(backgroundColorString, 1)
                stock = AnyChart.stock().apply {
                    background().fill(backgroundFill)
                }
                table = Table.instantiate("x")
                mapping = table.mapAs("{open: 'open', high: 'high', low: 'low', close: 'close', value: 'value'}")
                setMainPlot()
                ohlc = plotMain.ohlc(mapping)
                setStochasticPlot()
                setMACDPlot()
                setMomentumPlot()
                setRSIPlot()
                setScroller()
            }
            .subscribe {
                binding.anyChartView.setChart(stock)
            }
            .addTo(autoDisposable)
    }
    
    private fun setScroller() {
        val scroller = stock.scroller()
        val ohlc = scroller.ohlc(mapping)
    }
    
    private fun setMainPlot() {
        plotMain = stock.plot(0).apply {
            noData("No data to display")
            legend(false)
            yGrid(false)
            xGrid(false)
            yMinorGrid(false)
            xMinorGrid(false)
            ema(table.mapAs("{value: 'close'}"), 20.0, StockSeriesType.LINE)
            ema(table.mapAs("{value: 'close'}"), 50.0, StockSeriesType.LINE)
            ema(table.mapAs("{value: 'close'}"), 100.0, StockSeriesType.LINE)
        }
    }
    
    private fun setStochasticPlot() {
        // Draw stochastic in a new plot
        plotStochastic = stock.plot(1).apply {
            noData("No data to display")
            legend(false)
            yGrid(false)
            xGrid(false)
            yMinorGrid(false)
            xMinorGrid(false)
            height("30%")
            xAxis(false)
            stochastic(mapping, 14, 3, 3, "line", "line", "line", "line")
        }
        val plotStochasticController = plotStochastic.annotations()
        val line1 = plotStochasticController.horizontalLine("{valueAnchor:'80.0'}").apply {
            stroke("#808080", 0.5, "5 5", StrokeLineJoin.BEVEL, StrokeLineCap.SQUARE)
        }
        val line2 = plotStochasticController.horizontalLine("{valueAnchor:'20.0'}").apply {
            stroke("#808080", 0.5, "5 5", StrokeLineJoin.BEVEL, StrokeLineCap.SQUARE)
        }
        setStochasticPlotAndButtonVisibility()
    }
    
    private fun setMACDPlot() {
        // Draw stochastic in a new plot
        plotMACD = stock.plot(2).apply {
            noData("No data to display")
            legend(false)
            yGrid(false)
            xGrid(false)
            yMinorGrid(false)
            xMinorGrid(false)
            height("30%")
            xAxis(false)
            
        }
        val redColorString = "#" + getColorHex(R.color.red)
        val blueColorString = "#" + getColorHex(R.color.blue)
        
        val macd = plotMACD.macd(mapping, 12, 26, 9, StockSeriesType.LINE, StockSeriesType.LINE, StockSeriesType.COLUMN)
        
        macd.macdSeries().normal().stroke(redColorString, 1, "50 0", StrokeLineJoin.BEVEL, StrokeLineCap.SQUARE)
        macd.signalSeries().normal().stroke(redColorString, 0.5, "5 5", StrokeLineJoin.BEVEL, StrokeLineCap.SQUARE)
        macd.histogramSeries().normal().fill(SolidFill(blueColorString, 1))
        
        val plotMACDController = plotMACD.annotations()
        val line = plotMACDController.horizontalLine("{valueAnchor:'0.0'}").apply {
            stroke("#808080", 0.5, "50 0", StrokeLineJoin.BEVEL, StrokeLineCap.SQUARE)
        }
        setMACDPlotAndButtonVisibility()
    }
    
    private fun setMomentumPlot() {
        // Draw stochastic in a new plot
        plotMomentum = stock.plot(3).apply {
            noData("No data to display")
            legend(false)
            yGrid(false)
            xGrid(false)
            yMinorGrid(false)
            xMinorGrid(false)
            height("30%")
            xAxis(false)
            momentum(mapping, 14 , StockSeriesType.LINE)
            
        }
        val plotMomentumController = plotMomentum.annotations()
        val line = plotMomentumController.horizontalLine("{valueAnchor:'0.0'}").apply {
            stroke("#808080", 0.5, "50 0", StrokeLineJoin.BEVEL, StrokeLineCap.SQUARE)
        }
        setMomentumPlotAndButtonVisibility()
    }
    
    private fun setRSIPlot() {
        // Draw stochastic in a new plot
        plotRSI = stock.plot(4).apply {
            noData("No data to display")
            legend(false)
            yGrid(false)
            xGrid(false)
            yMinorGrid(false)
            xMinorGrid(false)
            height("30%")
            xAxis(false)
            rsi(mapping, 14, StockSeriesType.LINE)
            
        }
        val plotRSIController = plotRSI.annotations()
        val line1 = plotRSIController.horizontalLine("{valueAnchor:'30.0'}").apply {
            stroke("#808080", 0.5, "5 5", StrokeLineJoin.BEVEL, StrokeLineCap.SQUARE)
        }
        val line2 = plotRSIController.horizontalLine("{valueAnchor:'70.0'}").apply {
            stroke("#808080", 0.5, "5 5", StrokeLineJoin.BEVEL, StrokeLineCap.SQUARE)
        }
        setRSIPlotAndButtonVisibility()
    }
    
    private fun getColorHex(colorResource: Int) =
        Integer.toHexString(ContextCompat.getColor(binding.root.context, colorResource))
            .substring(2)
    
    private fun findBinanceSymbols() {
        viewModel.repository.findBinanceSymbolsByBaseAsset(symbol)
            .subscribe { list ->
                if (list.isEmpty()) {
                    MaterialAlertDialogBuilder(binding.root.context, R.style.CVDialogStyle)
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
                    binanceSymbolIndexBehaviour.onNext(indexOfFirstAsset)
                    setupSymbolSpinner(list, indexOfFirstAsset)
                    Log.d(
                        this.javaClass.simpleName,
                        "Found ${list.size} Binance symbols, USDT index: ${binanceSymbolIndexBehaviour}"
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
                    binanceSymbolIndexBehaviour.onNext(position)
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
                    intervalBehaviour.onNext(intervals.get(position))
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