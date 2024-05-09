package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.coinpaprika.apiclient.entity.QuoteEntity
import com.highsoft.highcharts.common.HIColor
import com.highsoft.highcharts.common.hichartsclasses.HICSSObject
import com.highsoft.highcharts.common.hichartsclasses.HIChart
import com.highsoft.highcharts.common.hichartsclasses.HICredits
import com.highsoft.highcharts.common.hichartsclasses.HIExporting
import com.highsoft.highcharts.common.hichartsclasses.HILabels
import com.highsoft.highcharts.common.hichartsclasses.HILegend
import com.highsoft.highcharts.common.hichartsclasses.HILine
import com.highsoft.highcharts.common.hichartsclasses.HIOptions
import com.highsoft.highcharts.common.hichartsclasses.HITitle
import com.highsoft.highcharts.common.hichartsclasses.HITooltip
import com.highsoft.highcharts.common.hichartsclasses.HIXAxis
import com.highsoft.highcharts.common.hichartsclasses.HIYAxis
import com.highsoft.highcharts.core.HIChartView
import dev.kokorev.coin_paprika_api.entity.TickerTickEntity
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.FragmentChartBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.CoinViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.DecimalFormat

class ChartFragment : Fragment() {
    private lateinit var binding: FragmentChartBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: CoinViewModel by viewModels<CoinViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    // Chart data
    private lateinit var hiChartView: HIChartView
    private lateinit var options: HIOptions
    private lateinit var hiChart: HIChart

    // Used colors on chart
    private val transparentColor = HIColor.initWithRGBA(0, 0, 0, 0.0)
    private val semitransparentBlackColor = HIColor.initWithRGBA(0, 0, 0, 0.2)
    private val lightColorString = "00FFA0"
    private val lightColor = HIColor.initWithHexValue(lightColorString)
    private val whiteColorString = "FFFFFF"
    private val whiteColor = HIColor.initWithHexValue(whiteColorString)
    private val whiteTextStyle = HICSSObject().apply {
        color = "#" + whiteColorString
        fontSize = "8pt"
    }
    private lateinit var coinPaprikaId: String
    private lateinit var symbol: String
    private var ticks: List<TickerTickEntity> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentChartBinding.inflate(layoutInflater)
        coinPaprikaId = arguments?.getString(Constants.ID) ?: ""
        symbol = arguments?.getString(Constants.SYMBOL) ?: ""

        autoDisposable.bindTo(lifecycle)
        initChartView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel.remoteApi.getCoinPaprikaTicker(coinPaprikaId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                binding.symbol.text = symbol
                val quotes = it.quotes?.get("USD")
                if (quotes != null) showQuotes(quotes)
            },
                {
                    Log.d(
                        "ChartFragment",
                        "Error getting data from CoinPaparikaTicker",
                        it
                    )
                })
            .addTo(autoDisposable)

        viewModel.remoteApi.getCoinPaprikaTickerHistorical(coinPaprikaId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("ChartFragment", "${it.size} ticks received for the chart")
                ticks = it
                updateChart()
                Log.d("ChartFragment", "Finished updating the chart")
            },
                {
                    Log.d(
                        "ChartFragment",
                        "Error getting data from CoinPaparika TickerHistorical",
                        it
                    )
                })
            .addTo(autoDisposable)

        // Refresh chart with delay, otherwise sometimes it isn't drawn.
        Handler(Looper.getMainLooper()).postDelayed({
            updateChart()
            Log.d("ChartFragment",
                "Chart refreshed")
        }, 500)

        return binding.root
    }

    private fun initChartView() {
        hiChartView = binding.chartView

        // Do not show export menu
        val hiExporting = HIExporting().apply {
            enabled = false
        }

        // Do not show credits
        val hiCredits = HICredits().apply {
            enabled = false
        }

        // do not show legend
        val hiLegend = HILegend().apply {
            enabled = false
        }

        val yLabels = HILabels().apply {
            enabled = true
            style = whiteTextStyle
        }

        val ytitle = HITitle().apply {
            text = ""
        }

        // Y-axis params
        val hiyAxis = HIYAxis().apply {
            labels = yLabels
            title = ytitle
            lineColor = whiteColor
            lineWidth = 1
            gridLineColor = whiteColor
            gridLineWidth = 1
            gridLineDashStyle = "Dot"
            tickColor = whiteColor
            opposite = true
        }

        val xLabels = HILabels().apply {
            enabled = true
            style = whiteTextStyle
        }

        val xtitle = HITitle().apply {
            text = ""
        }

        // X-axis params
        val hixAxis = HIXAxis().apply {
            labels = xLabels
            title = xtitle
            lineColor = whiteColor
            lineWidth = 1
            tickColor = whiteColor
//            tickInterval = 31
            tickWidth = 1
        }

        // The chart itself
        hiChart = HIChart().apply {
            type = "line"
            backgroundColor = transparentColor
        }

        // Chart's title
        val whiteColorStyle = HICSSObject().apply {
            color = "#" + whiteColorString
        }

        val chartTitle = HITitle().apply {
            text = ""
            style = whiteColorStyle
        }

        val hiTooltip = HITooltip().apply {
            shared = true
            useHTML = true
            headerFormat = "<table><tr><th colspan=\"2\">{point.key}</th></tr>"
            pointFormat =
                "<tr><td style=\"{series.color}\">{series.name}</td><td style=\"text-align: right\"><b>{point.y}</b></td></tr>"
            footerFormat = "</table>"
            backgroundColor = semitransparentBlackColor
            style = whiteTextStyle
        }

        // Chart options
        options = HIOptions().apply {
            exporting = hiExporting
            credits = hiCredits
            legend = hiLegend
            xAxis = arrayListOf(hixAxis)
            yAxis = arrayListOf(hiyAxis)
            chart = hiChart
            title = chartTitle
//            series = arrayListOf(emptySeries)
            tooltip = hiTooltip
        }

        // setting all the options
        hiChartView.options = options
        updateChart()
    }

    private fun updateChart() {
        // defining data to show on the chart
        val newSeries = HILine().apply {
            data = ArrayList(
                ticks.asSequence()
                    .map { e -> e.price }
                    .toList()
            )
            color = lightColor
            name = symbol
        }

        hiChartView.options.series = arrayListOf(newSeries)
        hiChartView.redraw()

        // setting x-axis labels
        hiChartView.options.xAxis.get(0).categories = ArrayList(
            ticks.asSequence()
                .map { tick ->
                    convertDate(tick.timestamp) //.substring(5, 10)
                }
                .toList()
        )

        hiChartView.update(options)
        hiChartView.redraw()
    }

    private fun convertDate(timestamp: String): String {
//        val year = timestamp.substring(0, 4).toInt()
        val month = timestamp.substring(5, 7).toInt()
        val dayWithHiph = timestamp.substring(8, 10)
        val monthString = when (month) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> "-"
        }
        return monthString + " " + dayWithHiph
    }

    private fun showQuotes(quotes: QuoteEntity) {
        binding.price.text = DecimalFormat("#,###.########$").format(
            NumbersUtils.roundNumber(
                quotes.price,
                3
            )
        )

        val change = quotes.percentChange24h
        binding.change.text = DecimalFormat("#,###.##%").format(
            NumbersUtils.roundNumber(change / 100.0, 2)
        )
        if (change < 0) binding.change.setTextColor(
            ContextCompat.getColor(
                binding.root.context,
                R.color.lightAccent
            )
        )
        else binding.change.setTextColor(
            ContextCompat.getColor(
                binding.root.context,
                R.color.light
            )
        )

        val volume = "Vol: " + NumbersUtils.formatBigNumber(quotes.dailyVolume)
        binding.volume.text = volume

        val mcap = "MCap: " + NumbersUtils.formatBigNumber(quotes.marketCap)
        binding.mcap.text = mcap
    }
}