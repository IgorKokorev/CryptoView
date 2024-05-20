package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.FragmentAiBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.CoinViewModel
import dev.kokorev.token_metrics_api.entity.AiReportData

// AI reports fragment to show Token Metrics reports
class AiReportFragment : Fragment() {
    private lateinit var binding: FragmentAiBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: CoinViewModel by viewModels<CoinViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    // Boring
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAiBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
    }

    // Boring
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        getAiReport() // here we get the reports from API, convert them and show into the view
        return binding.root
    }

    private fun getAiReport() {
        // In case of an error or empty report
        val emptyReport =
            getString(R.string.no_available_ai_report, viewModel.name, viewModel.symbol)
        viewModel.remoteApi.getAIReport(viewModel.symbol)
            .subscribe{
                val strBuilder = StringBuilder()
                // Selecting an item from the list
                var aiReportData: AiReportData? = findReport(it.data)
                if (aiReportData == null) {
                    strBuilder.append(emptyReport)
                } else {
                    aiReportData.run {
                        strBuilder.append(convertToHtml(getString(R.string.fundamental_report),FUNDAMENTALREPORT))
                        strBuilder.append(convertToHtml(getString(R.string.trader_report), TRADERREPORT))
                        strBuilder.append(convertToHtml(getString(R.string.technology_report), TECHNOLOGYREPORT))
                    }
                }
                binding.content.text =
                    Html.fromHtml(strBuilder.toString(), Html.FROM_HTML_MODE_COMPACT)
            }
            .addTo(autoDisposable)
    }

    // in a list of reports find the one we need
    private fun findReport(aiReportDataList: ArrayList<AiReportData>): AiReportData? {
        if (aiReportDataList.isEmpty()) return null
        aiReportDataList.forEach { data ->
            Log.d(
                "AiFragment",
                "TokenMetrics symbol: ${data.SYMBOL}, name: ${data.TOKENNAME}, id: ${data.TOKENID}"
            )
        }
        var aiReportData: AiReportData? = null
        if (aiReportDataList.size == 1) aiReportData = aiReportDataList.get(0)
        else {
            for (data in aiReportDataList) {
                if (data.TOKENNAME.lowercase() == viewModel.name.lowercase()) {
                    aiReportData = data
                    break
                }
            }
        }
        return aiReportData
    }

    // Creating a html section from TokenMetrics with given header
    private fun convertToHtml(header: String, text: String?): StringBuilder {
        val strBuilder = StringBuilder()
        if (text.isNullOrBlank()) return strBuilder

        val newText = reportTextToHtml(text)
        strBuilder.append("<h2>${header}</h2><br><br>", newText, "<br><br>")

        return strBuilder
    }

    // Convert TokenMetrics ai text report to html
    private fun reportTextToHtml(text: String): String {
        val regex = """(- )?## .+\n""".toRegex()

        val firstIteration = """- ## .+\n""".toRegex()
            .replace(text) { it ->
                "<h4>" + it.value.substring(5) + "</h4>"
            }

        val secondIteration = """## .+\n""".toRegex()
            .replace(firstIteration) { it ->
                "<h4>" + it.value.substring(3) + "</h4>"
            }

        return secondIteration.replace("\n", "<br>")

    }
}