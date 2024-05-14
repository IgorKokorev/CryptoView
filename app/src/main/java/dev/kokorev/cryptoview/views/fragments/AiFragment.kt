package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.kokorev.cryptoview.databinding.FragmentAiBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.CoinViewModel
import dev.kokorev.token_metrics_api.entity.AiReportData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class AiFragment : Fragment() {
    private lateinit var binding: FragmentAiBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: CoinViewModel by viewModels<CoinViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAiBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        getAiReport()
        return binding.root
    }

    private fun getAiReport() {
        val emptyReport = "No available AI reports for ${viewModel.name}(${viewModel.symbol})"
        viewModel.remoteApi.getAIReport(viewModel.symbol)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val strBuilder = StringBuilder()
                val aiReportDataList = it.data
                Log.d(
                    "AiFragment",
                    "Success: ${it.success}. Message: ${it.message}. Number of coins with symbol ${viewModel.symbol}, name ${viewModel.name} = ${aiReportDataList.size}"
                )
                if (aiReportDataList.isEmpty()) {
                    binding.content.text = emptyReport
                } else {
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
                    if (aiReportData == null) {
                        strBuilder.append(emptyReport)
                        Log.d(
                            "AiFragment",
                            "Fetching CoinPaprika simbol: " + viewModel.symbol + " with name: " + viewModel.name + " failed. \nTokenMetrics data:"
                        )
                    } else {
                        aiReportData.apply {
                            strBuilder.append(convertToHtml("FUNDAMENTAL REPORT", FUNDAMENTALREPORT))
                            strBuilder.append(convertToHtml("TRADER REPORT", TRADERREPORT))
                            strBuilder.append(convertToHtml("TECHNOLOGY REPORT:", TECHNOLOGYREPORT))
                        }
                    }
                    binding.content.text =
                        Html.fromHtml(strBuilder.toString(), Html.FROM_HTML_MODE_COMPACT)
                }
            },
                {
                    Log.d(
                        "AiFragment",
                        "Request failed"
                    )
                    binding.content.text = emptyReport
                })
            .addTo(autoDisposable)
    }

    // Creating a html section from TokenMetrics with given header
    private fun convertToHtml(header: String, text: String?): StringBuilder {
        val strBuilder = StringBuilder()
        if (text.isNullOrBlank()) return strBuilder

        val newText = reportTextToHtml(text)
        strBuilder.append("<h3>${header}:</h3><br><br>", newText, "<br><br>")

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