package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.coinpaprika.apiclient.entity.QuoteEntity
import com.highsoft.highcharts.common.HIColor
import com.highsoft.highcharts.common.hichartsclasses.HICSSObject
import com.highsoft.highcharts.common.hichartsclasses.HIChart
import com.highsoft.highcharts.common.hichartsclasses.HICredits
import com.highsoft.highcharts.common.hichartsclasses.HIExporting
import com.highsoft.highcharts.common.hichartsclasses.HILabels
import com.highsoft.highcharts.common.hichartsclasses.HILegend
import com.highsoft.highcharts.common.hichartsclasses.HILine
import com.highsoft.highcharts.common.hichartsclasses.HIMarker
import com.highsoft.highcharts.common.hichartsclasses.HIOptions
import com.highsoft.highcharts.common.hichartsclasses.HITitle
import com.highsoft.highcharts.common.hichartsclasses.HITooltip
import com.highsoft.highcharts.common.hichartsclasses.HIXAxis
import com.highsoft.highcharts.common.hichartsclasses.HIYAxis
import com.highsoft.highcharts.core.HIChartView
import dev.kokorev.coin_paprika_api.entity.TickerTickEntity
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.FragmentChartBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.CoinViewModel
import java.text.DecimalFormat
import java.time.LocalDate
import java.util.Locale

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
    private lateinit var textColorString: String
    private lateinit var chartColorString: String
    private val transparentColor = HIColor.initWithRGBA(0, 0, 0, 0.0)
    private val semitransparentBlackColor = HIColor.initWithRGBA(0, 0, 0, 0.2)
    private lateinit var chartColor: HIColor
    private lateinit var textColor: HIColor
    private lateinit var textThemeStyle: HICSSObject
    private var ticks: List<TickerTickEntity> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentChartBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)

        // initializing chart colors with theme colors
        textColorString =
            Integer.toHexString(ContextCompat.getColor(binding.root.context, R.color.textColor))
                .substring(2)
        chartColorString =
            Integer.toHexString(ContextCompat.getColor(binding.root.context, R.color.base7))
                .substring(2)
        chartColor = HIColor.initWithHexValue(chartColorString)
        textColor = HIColor.initWithHexValue(textColorString)
        textThemeStyle = HICSSObject().apply {
            color = "#" + textColorString
            fontSize = "8pt"
        }

        initChartView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setupIntervalButtons()

        viewModel.remoteApi.getCoinPaprikaTicker(viewModel.coinPaprikaId)
            .subscribe({
                val nameAndSymbol = viewModel.name + " (" + viewModel.symbol + ")"
                binding.symbol.text = nameAndSymbol
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

        viewModel.remoteApi.getCoinPaprikaTickerHistorical(viewModel.coinPaprikaId)
            .subscribe({
                Log.d("ChartFragment", "${it.size} ticks received for the chart")
                ticks = it
                updateChart(MAX_INTERVAL)
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
            updateChart(MAX_INTERVAL)
            Log.d(
                "ChartFragment",
                "Chart refreshed"
            )
        }, 500)

        return binding.root
    }

    private fun setupIntervalButtons() {
        binding.d7.setOnClickListener { updateChart(D7_INTERVAL) }
        binding.m1.setOnClickListener { updateChart(M1_INTERVAL) }
        binding.m3.setOnClickListener { updateChart(M3_INTERVAL) }
        binding.m6.setOnClickListener { updateChart(M6_INTERVAL) }
        binding.ytd.setOnClickListener { updateChart(LocalDate.now().dayOfYear) }
        binding.y1.setOnClickListener { updateChart(Y1_INTERVAL) }
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
            style = textThemeStyle
        }

        val ytitle = HITitle().apply {
            text = ""
        }

        // Y-axis params
        val hiyAxis = HIYAxis().apply {
            labels = yLabels
            title = ytitle
            lineColor = textColor
            lineWidth = 1
            gridLineColor = textColor
            gridLineWidth = 1
            gridLineDashStyle = "Dot"
            tickColor = textColor
            opposite = true
        }

        val xLabels = HILabels().apply {
            enabled = true
            style = textThemeStyle
        }

        val xtitle = HITitle().apply {
            text = ""
        }

        // X-axis params
        val hixAxis = HIXAxis().apply {
            labels = xLabels
            title = xtitle
            lineColor = textColor
            lineWidth = 1
            tickColor = textColor
            tickWidth = 1
        }

        // The chart itself
        hiChart = HIChart().apply {
            type = "line"
            backgroundColor = transparentColor
        }

        // Chart's title
        val whiteColorStyle = HICSSObject().apply {
            color = "#" + textColorString
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
            style = textThemeStyle
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
            tooltip = hiTooltip
        }

        // setting all the options
        hiChartView.options = options
        updateChart(D7_INTERVAL)
    }

    private fun updateChart(interval: Int) {
        // defining data to show on the chart
        val newSeries = HILine().apply {
            data = ArrayList(
                ticks.map { e -> e.price }
                    .takeLast(interval)
            )
            color = chartColor
            name = viewModel.symbol
            marker = HIMarker().apply {
                enabled = false
            }
        }

        hiChartView.options.series = arrayListOf(newSeries)
        hiChartView.redraw()

        // setting x-axis labels
        hiChartView.options.xAxis.get(0).categories = ArrayList(
            ticks.map { tick ->
                convertDate(tick.timestamp) //.substring(5, 10)
            }.takeLast(interval)
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

        Glide.with(binding.root)
            .load(viewModel.cpInfo?.logo)
            .centerCrop()
            .into(binding.logo)

        binding.price.text = formatPrice(quotes.price)
        binding.ath.text = formatPrice(quotes.athPrice)

        showChange(quotes.percentChange1h, binding.change1h)
        showChange(quotes.percentChange12h, binding.change12h)
        showChange(quotes.percentChange24h, binding.change24h)
        showChange(quotes.percentChange7d, binding.change7d)
        showChange(quotes.percentChange30d, binding.change30d)
        showChange(quotes.percentChange1y, binding.change1y)
        showChange(quotes.percentFromPriceAth, binding.changeAth)

        binding.volume.text = NumbersUtils.formatBigNumber(quotes.dailyVolume)
        binding.mcap.text = NumbersUtils.formatBigNumber(quotes.marketCap)
    }

    private fun showChange(change: Double?, changeView: TextView) {
        changeView.text = if (change == null) "-"
        else {
            changeView.setTextColor(
                if (change < 0)
                    ContextCompat.getColor(changeView.context, R.color.red)
                else
                    ContextCompat.getColor(changeView.context, R.color.green)
            )

            "%.3f".format(Locale.ENGLISH, change)
        }
    }

    private fun formatPrice(price: Double?): String =
        if (price == null) "-"
        else DecimalFormat("#,###.########$").format(
            NumbersUtils.roundNumber(
                price,
                3
            )
        )

    companion object {
        const val D7_INTERVAL = 7
        const val M1_INTERVAL = 30
        const val M3_INTERVAL = 91
        const val M6_INTERVAL = 182
        const val Y1_INTERVAL = 364
        const val MAX_INTERVAL = 364
    }
}