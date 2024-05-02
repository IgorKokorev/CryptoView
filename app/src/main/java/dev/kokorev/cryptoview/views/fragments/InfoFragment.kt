package dev.kokorev.cryptoview.views.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.viewModel.InfoViewModel
import dev.kokorev.cryptoview.databinding.FragmentInfoBinding
import dev.kokorev.cryptoview.databinding.TwoColumnItemViewBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.views.MainActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.stream.Collectors.toList
import kotlin.jvm.optionals.getOrNull

class InfoFragment : Fragment() {
    private lateinit var binding: FragmentInfoBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: InfoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoBinding.inflate(layoutInflater)
        val id = arguments?.getString(Constants.ID) ?: return binding.root
        val symbol = arguments?.getString(Constants.SYMBOL) ?: return binding.root

        // two calls are made to 2 apis and then all the info is used combined
        Observable.zip(
            viewModel.interactor.getCoinPaprikaCoinInfo(id),
            viewModel.interactor.getCmcMetadata(symbol)
        ) { cpInfo, cmcInfo ->
            cpInfo to cmcInfo
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val cpInfo = it.first // Coin Info from CoinPaprika
                    val cmcInfo = it.second.data.get(symbol)?.get(0) // Coin Info from CoinMarketCap

                    // Coin logo
                    Glide.with(binding.root)
                        .load(cpInfo.logo)
                        .centerCrop()
                        .into(binding.logo)

                    // Coin name and symbol
                    val nameAndSymbol = cpInfo.name + " (" + cpInfo.symbol + ")"
                    binding.name.text = nameAndSymbol

                    binding.chartLink.setOnClickListener {
                        (requireActivity() as MainActivity).launchChartFragment(cpInfo.id)
                    }
                    // Coin description from both APIs
                    val description = cmcInfo?.description + "\n\n" + cpInfo.description
                    binding.description.text = description

                    val tags = cpInfo.tags
                    if (!tags.isNullOrEmpty()) {
                        val s = tags.stream()
                            .map {
                                it.name
                            }
                            .reduce { acc, string -> acc + ", " + string }
                            .getOrNull()

                        binding.tags.text = s
                    }

                    val links = cpInfo.linksExtended
                    if (!links.isNullOrEmpty()) {
                        links.forEach {
                            val itemViewBinding = TwoColumnItemViewBinding.inflate(layoutInflater)
                            itemViewBinding.name.text = camelCaseToText(it.type)
                            itemViewBinding.value.text = it.url
                            binding.urls.addView(itemViewBinding.root)
                        }
                    }

                    val team = cpInfo.team
                    if (team.isNullOrEmpty()) {
                        binding.body.removeView(binding.teamTitle)
                    } else {
                        team.forEach {
                            val itemViewBinding = TwoColumnItemViewBinding.inflate(layoutInflater)
                            itemViewBinding.name.text = it.name
                            itemViewBinding.value.text = it.role
                            binding.team.addView(itemViewBinding.root)
                        }
                    }
                },
                {
                    Log.d(
                        "InfoFragment",
                        "Error getting data from CoinPaparikaCoinInfo or CmcMetaData",
                        it
                    )
                })
            .addTo(autoDisposable)

        return binding.root
    }

    // Converts camel case name to normal text
    private fun camelCaseToText(s: String): String {
        return s.replaceFirstChar { c -> c.uppercase() }.replace('_', ' ')
    }

}