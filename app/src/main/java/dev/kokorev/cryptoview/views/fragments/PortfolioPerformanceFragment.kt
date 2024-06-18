package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.transition.TransitionManager
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.annotations.PlotController
import com.anychart.enums.ScaleTypes
import com.anychart.enums.TooltipDisplayMode
import com.anychart.graphics.vector.SolidFill
import com.anychart.graphics.vector.StrokeLineCap
import com.anychart.graphics.vector.StrokeLineJoin
import com.coinpaprika.apiclient.entity.PortfolioEvaluationDB
import com.coinpaprika.apiclient.entity.PortfolioPositionDB
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_FIRST_LAUNCH_INSTANT
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesInstant
import dev.kokorev.cryptoview.databinding.FragmentPortfolioPerformanceBinding
import dev.kokorev.cryptoview.logd
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.utils.getColorHex
import dev.kokorev.cryptoview.utils.toLocalDate
import dev.kokorev.cryptoview.viewModel.PortfolioPerformanceViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.rx3.rxSingle
import java.lang.Math.pow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PortfolioPerformanceFragment : Fragment() {
    private lateinit var binding: FragmentPortfolioPerformanceBinding
    private val viewModel: PortfolioPerformanceViewModel by viewModels()
    private val autoDisposable = AutoDisposable()
    private var firstLaunchInstant by preferencesInstant(KEY_FIRST_LAUNCH_INSTANT)
    
    // chart to show global crypto market cap change
    lateinit var chart: Cartesian
    val chartReady: BehaviorSubject<Boolean> = BehaviorSubject.create()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentPortfolioPerformanceBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
        
        // setting Portfolio chart
        binding.portfolioChart.visibility = View.GONE
        setupPortfolioChart()
        chartReady
            .onErrorReturn { false }
            .subscribe {
                if (it) getPortfolioEvaluations()
            }
            .addTo(autoDisposable)
        
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
    
    private fun setupPortfolioChart() {
        rxSingle {
            val backgroundColorString = "#" + getColorHex(R.color.base2)
            val backgroundFill = SolidFill(backgroundColorString, 1)
            chart = AnyChart.column().apply {
                background().fill(backgroundFill)
                padding(12, 24, 12, 12)
                xGrid(false)
                xMinorGrid(false)
                yGrid(false)
                yMinorGrid(false)
                credits(false)
                tooltip().displayMode(TooltipDisplayMode.UNION)
                legend(true)
            }
        }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess {
                chartReady.onNext(true)
            }
            .doOnError {
                logd("setupMcapChart error", it)
                binding.portfolioChart.visibility = View.GONE
                
            }
            .onErrorComplete()
            .subscribe()
            .addTo(autoDisposable)
    }
    
    private fun getPortfolioEvaluations() {
        viewModel.portfolioEvaluations
            .doOnSuccess { evaluations ->
                val nonNullData = evaluations.filter { evaluation -> (evaluation.valuation != null) and (evaluation.cumulativePercentChange != null) }
                logd("setupDataFromViewModel received ${evaluations.size} evaluations from them non-null: ${nonNullData.size}")
                // show evaluations on the chart
                addEvaluationsToChart(nonNullData)
                // calculate and show the latest statistics
                showStats(nonNullData)
            }
            .doOnError {
                binding.portfolioChart.visibility = View.GONE
                logd("setupDataFromViewModel", it)
            }
            .onErrorComplete()
            .doOnComplete {
                logd("setupDataFromViewModel. No evaluations found")
            }
            .subscribe()
            .addTo(autoDisposable)
    }
    
    private fun showStats(evaluations: List<PortfolioEvaluationDB>) {
        // combine portfolio positions + latest tickers prices
        Observable.combineLatest(viewModel.portfolio, viewModel.tickers) { portfolio, tickers ->
            portfolio.forEach { portfolioCoin ->
                val cpTicker = tickers.find { ticker ->
                    ticker.coinPaprikaId == portfolioCoin.coinPaprikaId
                }
                if (cpTicker != null) {
                    portfolioCoin.priceLastEvaluation = cpTicker.price ?: 0.0
                    portfolioCoin.timeLastEvaluation = System.currentTimeMillis()
                    portfolioCoin.percentChange24hr = cpTicker.percentChange24h ?: 0.0
                }
            }
            portfolio
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ positions ->
                setStatsDataToViews(evaluations, positions)
            },
                { e ->
                    logd("showStats combining portfolio and tickers", e)
                    
                })
            .addTo(autoDisposable)
    }
    
    private fun setStatsDataToViews(evaluations: List<PortfolioEvaluationDB>, positions: List<PortfolioPositionDB>) {
        // setting data from portfolio positions
        binding.date.text = LocalDate.now().toString()
        binding.numPositions.text = positions.size.toString()
        var valuation = 0.0
        var unrealisedPnl = 0.0
        
        positions.forEach { p ->
            valuation += p.quantity * p.priceLastEvaluation
            unrealisedPnl += p.quantity * (p.priceLastEvaluation - p.priceOpen)
        }
        
        binding.lastValuation.text = NumbersUtils.formatPriceWithCurrency(valuation, "$")
        NumbersUtils.setChangeView(binding.root.context, binding.unrealisedPnl, unrealisedPnl, "$")
        
        // setting data from evaluations
        val lastEvaluation = evaluations.last()
        
        binding.lastEvaluationDate.text = lastEvaluation.date.toString()
        binding.evaluationValuation.text = NumbersUtils.formatPriceWithCurrency(lastEvaluation.valuation, "$")
        
        var realisedPnl = 0.0
        var inflow = 0.0
        var outflow = 0.0
        evaluations.forEach { e ->
            realisedPnl += e.pnl ?: 0.0
            if (e.inflow != null && e.inflow!! > 0.0 ) inflow += e.inflow!!
            else outflow += e.inflow ?: 0.0
        }

        NumbersUtils.setChangeView(binding.root.context, binding.realisedPnl, realisedPnl, "$")
        NumbersUtils.setChangeView(binding.root.context, binding.totalInflow, inflow, "$")
        NumbersUtils.setChangeView(binding.root.context, binding.totalOutflow, outflow, "$")
        NumbersUtils.setChangeView(binding.root.context, binding.flowBalance, inflow + outflow, "$")
        NumbersUtils.setChangeView(binding.root.context, binding.effectiveReturn, (lastEvaluation.cumulativePercentChange ?: 0.0) * 100.0, "%")
        
        val firstLaunchDate = firstLaunchInstant.toLocalDate()
        binding.inceptionDate.text = firstLaunchDate.toString()
        if (lastEvaluation.date.isAfter(firstLaunchDate)) {
            val days = ChronoUnit.DAYS.between(firstLaunchDate, lastEvaluation.date)
            logd("setStatsDataToViews portfolio life = $days days")
            val annualisedReturn = pow((1.0 + (lastEvaluation.cumulativePercentChange ?: 0.0)), 365.25 / days.toDouble()) - 1.0
            NumbersUtils.setChangeView(binding.root.context, binding.annualizedReturn, annualisedReturn * 100.0, "%")
        }
    }
    
    private fun addEvaluationsToChart(evaluations: List<PortfolioEvaluationDB>) {
        rxSingle {
            val chartDataValuation: List<DataEntry> =
                evaluations.map { Converter.portfolioEvaluationToValuationDataEntry(it) }
            val chartDataChange: List<DataEntry> =
                evaluations.map { Converter.portfolioEvaluationToChangeDataEntry(it) }
            val minChange = (evaluations.map { it.cumulativePercentChange ?: 0.0 }.min() + 1.0) * 100.0
            
            val seriesChange = chart.line(chartDataChange)
            seriesChange
                .name("Effective performance")
            val startYfrom = NumbersUtils.formatWithPrecision(minChange * 0.9, 0).replace(',', '.')
            logd("setupMcapChart min Y = ${startYfrom}")
            chart.yScale("{minimum:'${startYfrom}'}")
            val plotController: PlotController = chart.annotations()
            val line = plotController.horizontalLine("{valueAnchor:'100.0'}").apply {
                stroke("#808080", 0.5, "50 0", StrokeLineJoin.BEVEL, StrokeLineCap.SQUARE)
            }
            
            val seriesValue = chart.column(chartDataValuation)
            seriesValue
                .name("Valuation")
                .yScale(ScaleTypes.LINEAR)
            
            val colorAccentString = "#" + getColorHex(R.color.accent4)
            val color = SolidFill(colorAccentString, 1)
            
            seriesValue.fill(color)
            seriesValue.stroke("{thickness:'0'}")
        }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess {
                logd("addEvaluationsToChart success")
                
                binding.portfolioChart.setChart(chart)

                TransitionManager.beginDelayedTransition(binding.portfolioChart)
                binding.portfolioChart.visibility = View.VISIBLE
            }
            .doOnError {
                logd("addEvaluationsToChart error", it)
                binding.portfolioChart.visibility = View.GONE
                
            }
            .onErrorComplete()
            .subscribe()
            .addTo(autoDisposable)
    }
}