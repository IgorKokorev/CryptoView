package dev.kokorev.cryptoview.views.fragments

import android.content.ComponentName
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.transition.TransitionManager
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.FragmentAiReportsBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.CoinViewModel
import dev.kokorev.token_metrics_api.entity.AiReportData

// AI reports fragment to show Token Metrics reports
class AiReportFragment : Fragment() {
    private lateinit var binding: FragmentAiReportsBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: CoinViewModel by viewModels<CoinViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    // Boring
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAiReportsBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
    }

    // Boring
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupReportsClickListeners()
        getAiReport() // here we get the reports from API, convert them and show into the view
        return binding.root
    }

    private fun setupReportsClickListeners() {
        binding.fundamentalContainer.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.fundamentalContainer)
            binding.fundamentalText.visibility =
                if (binding.fundamentalText.visibility == View.GONE) View.VISIBLE
                else View.GONE
        }

        binding.traderContainer.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.traderContainer)
            binding.traderText.visibility =
                if (binding.traderText.visibility == View.GONE) View.VISIBLE
                else View.GONE
        }

        binding.technologyContainer.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.technologyContainer)
            binding.technologyText.visibility =
                if (binding.technologyText.visibility == View.GONE) View.VISIBLE
                else View.GONE
        }
    }

    private fun getAiReport() {
        viewModel.remoteApi.getAIReport(viewModel.symbol)
            .subscribe {
                // Selecting an item from the list
                var aiReportData: AiReportData? = findReport(it.data)
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
        if (aiReportDataList.size == 1) aiReportData = aiReportDataList.get(0)
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
            .replace(text) { it ->
                "<h4>" + it.value.substring(5) + "</h4>"
            }

        val secondIteration = """## .+\n""".toRegex()
            .replace(firstIteration) { it ->
                "<h4>" + it.value.substring(3) + "</h4>"
            }

        return "<br>" + secondIteration.replace("\n", "<br>")
    }
}