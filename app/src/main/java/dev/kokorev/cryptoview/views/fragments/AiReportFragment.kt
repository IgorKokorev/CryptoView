package dev.kokorev.cryptoview.views.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.FragmentAiReportsBinding
import dev.kokorev.cryptoview.databinding.PricePredictionItemBinding
import dev.kokorev.cryptoview.databinding.SimpleTextViewBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.CoinViewModel
import dev.kokorev.token_metrics_api.entity.AiReportData
import dev.kokorev.token_metrics_api.entity.TMPricePredictionData

// AI reports fragment to show Token Metrics reports
class AiReportFragment : Fragment() {
    private lateinit var binding: FragmentAiReportsBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: CoinViewModel by viewModels<CoinViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAiReportsBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
        setupReportsClickListeners() // expand on click
        getPricePrediction()
        getAiReport() // here we get the reports from API, convert them and show into the view
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
    
    private fun getPricePrediction() {
        viewModel.getPricePrediction()
            .doOnSuccess {
                if (it.success && it.data.isNotEmpty()) {
                    setPrediction(it.data[0])
                }
            }
            .doOnError {
                Log.d(this.javaClass.simpleName, "Error getting price prediction for ${viewModel.symbol}, message: ${it.localizedMessage}, ${it.stackTrace}")
            }
            .doOnComplete {
                val textViewBinding = SimpleTextViewBinding.inflate(layoutInflater)
                textViewBinding.root.text = getString(R.string.price_prediction_not_found_for, viewModel.symbol)
                binding.predictionContainer.addView(
                    textViewBinding.root
                )
                Snackbar.make(binding.root,
                    getString(R.string.price_prediction_not_found_for, viewModel.symbol), Snackbar.LENGTH_SHORT).show()
            }
            .subscribe()
            .addTo(autoDisposable)
    }
    
    private fun setPrediction(prediction: TMPricePredictionData) {
        binding.date.text = prediction.date
        binding.coinName.text = prediction.tokenName
        prediction.forecastForNext7Days.forEach { (key, value) ->
            val itemBinding = PricePredictionItemBinding.inflate(layoutInflater)
            itemBinding.name.text = key
            val priceStr = NumbersUtils.formatPriceUSD(value)
            itemBinding.value.text = priceStr
            binding.predictionContainer.addView(itemBinding.root)
        }
        val yield = prediction.predictedReturns7d
        if (yield != null) {
            val yieldBinding = PricePredictionItemBinding.inflate(layoutInflater)
            NumbersUtils.setChangeView(yield * 100.0, binding.root.context, yieldBinding.value, "%")
            yieldBinding.name.text = getString(R.string.predicted_return_in_7_days)
            yieldBinding.name.setTypeface(null, Typeface.BOLD)
            yieldBinding.value.setTypeface(null, Typeface.BOLD)
            binding.predictionContainer.addView(yieldBinding.root)
        }
    }
    
    private fun setupReportsClickListeners() {
        binding.fundamentalContainer.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.fundamentalContainer)
            binding.fundamentalText.visibility =
                if (binding.fundamentalText.visibility == View.GONE) View.VISIBLE
                else View.GONE
            binding.traderText.visibility = View.GONE
            binding.technologyText.visibility = View.GONE
        }

        binding.traderContainer.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.traderContainer)
            binding.traderText.visibility =
                if (binding.traderText.visibility == View.GONE) View.VISIBLE
                else View.GONE
            binding.fundamentalText.visibility = View.GONE
            binding.technologyText.visibility = View.GONE
        }

        binding.technologyContainer.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.technologyContainer)
            binding.technologyText.visibility =
                if (binding.technologyText.visibility == View.GONE) View.VISIBLE
                else View.GONE
            binding.fundamentalText.visibility = View.GONE
            binding.traderText.visibility = View.GONE
        }
    }

    private fun getAiReport() {
        viewModel.getAIReport()
            .subscribe {
                // Selecting an item from the list
                val aiReportData: AiReportData? = findReport(it.data)
                if (aiReportData != null) {
                    val fundamentalReportHTML =
                        Html.fromHtml(
                            reportTextToHtml(
                                aiReportData.fundamentalReport
                                    ?: getString(R.string.no_report_found)
                            ), Html.FROM_HTML_MODE_COMPACT
                        )
                    binding.fundamentalText.text = fundamentalReportHTML

                    val traderReportHTML =
                        Html.fromHtml(
                            reportTextToHtml(
                                aiReportData.traderReport
                                    ?: getString(R.string.no_report_found)
                            ), Html.FROM_HTML_MODE_COMPACT
                        )
                    binding.traderText.text = traderReportHTML

                    val technologyReportHTML =
                        Html.fromHtml(
                            reportTextToHtml(
                                aiReportData.technologyReport
                                    ?: getString(R.string.no_report_found)
                            ), Html.FROM_HTML_MODE_COMPACT
                        )
                    binding.technologyText.text = technologyReportHTML
                }
            }
            .addTo(autoDisposable)
    }

    // in a list of reports find the one we need
    private fun findReport(aiReportDataList: ArrayList<AiReportData>): AiReportData? {
        if (aiReportDataList.isEmpty()) return null
        var aiReportData: AiReportData? = null
        if (aiReportDataList.size == 1) aiReportData = aiReportDataList[0]
        else {
            for (data in aiReportDataList) {
                if (data.tokenName.lowercase() == viewModel.name.lowercase()) {
                    aiReportData = data
                    break
                }
            }
        }
        return aiReportData
    }

    // Convert TokenMetrics ai text report to html
    private fun reportTextToHtml(text: String): String {
        val firstIteration = """- ## .+\n""".toRegex()
            .replace(text) {
                "<h4>" + it.value.substring(5) + "</h4>"
            }

        val secondIteration = """## .+\n""".toRegex()
            .replace(firstIteration) {
                "<h4>" + it.value.substring(3) + "</h4>"
            }

        return "<br>" + secondIteration.replace("\n", "<br>")
    }
}