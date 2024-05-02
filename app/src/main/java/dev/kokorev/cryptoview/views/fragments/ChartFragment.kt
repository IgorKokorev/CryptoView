package dev.kokorev.cryptoview.views.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.highsoft.highcharts.common.hichartsclasses.HIChart
import com.highsoft.highcharts.common.hichartsclasses.HIColumn
import com.highsoft.highcharts.common.hichartsclasses.HIOptions
import com.highsoft.highcharts.common.hichartsclasses.HITitle
import com.highsoft.highcharts.core.HIChartView
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.databinding.FragmentChartBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.ChartViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.DecimalFormat
import java.text.FieldPosition
import java.text.ParsePosition

import java.text.Format;
import java.time.Instant

class ChartFragment : Fragment() {
    private lateinit var binding: FragmentChartBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: ChartViewModel by viewModels()
    private lateinit var plot : HIChartView
    private val options: HIOptions = HIOptions()
    private val chart = HIChart()
    private val title = HITitle()
    private val series = HIColumn()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentChartBinding.inflate(layoutInflater)

        // initializing empty chart
        plot = binding.plot
        chart.type = "column"
        options.chart = chart
        title.text = "Test chart"
        options.title = title
        series.data = arrayListOf(1.0)
        options.series = arrayListOf(series)
        plot.options = options

        autoDisposable.bindTo(lifecycle)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val id = arguments?.getString(Constants.ID) ?: return binding.root

        viewModel.interactor.getCoinPaprikaTicker(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                binding.symbol.text = it.symbol

                val quotes = it.quotes?.get("USD")

                if (quotes != null) {

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
                            dev.kokorev.cryptoview.R.color.lightAccent
                        )
                    )
                    else binding.change.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            dev.kokorev.cryptoview.R.color.light
                        )
                    )

                    val volume = "Vol: " + NumbersUtils.formatBigNumber(quotes.dailyVolume)
                    binding.volume.text = volume

                    val mcap = "MCap: " + NumbersUtils.formatBigNumber(quotes.marketCap)
                    binding.mcap.text = mcap
                }
            },
                {
                    Log.d(
                        "ChartFragment",
                        "Error getting data from CoinPaparikaTicker",
                        it
                    )
                })
            .addTo(autoDisposable)

        viewModel.interactor.getCoinPaprikaTickerHistorical(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                series.data = arrayListOf(
                    it.asSequence()
                        .map { e -> e.price }
                )
                options.series = arrayListOf(series)
                plot.options = options
                plot.redraw()

            },
                {
                    Log.d(
                        "ChartFragment",
                        "Error getting data from CoinPaparika TickerHistorical",
                        it
                    )
                })
            .addTo(autoDisposable)

        return binding.root
    }
}