package dev.kokorev.cryptoview.views.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import dev.kokorev.cmc_api.entity.cmc_metadata.CmcCoinDataDTO
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.FragmentInfoBinding
import dev.kokorev.cryptoview.databinding.OneColumnItemViewBinding
import dev.kokorev.cryptoview.databinding.TwoColumnItemViewBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.InfoViewModel
import dev.kokorev.cryptoview.views.MainActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class InfoFragment : Fragment() {
    private lateinit var binding: FragmentInfoBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: InfoViewModel by viewModels()
    private var cpDescription = ""
    private var cmcDescription = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoBinding.inflate(layoutInflater)
        val coinPaprikaId = arguments?.getString(Constants.ID) ?: return binding.root
        val symbol = arguments?.getString(Constants.SYMBOL) ?: return binding.root

            viewModel.remoteApi.getCoinPaprikaCoinInfo(coinPaprikaId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    setupCoinPaprikaData(it)
                },
                {t ->
                    Log.d(
                        "InfoFragment",
                        "Error getting data from CoinPaparikaCoinInfo",
                        t
                    )
                })
            .addTo(autoDisposable)

        viewModel.remoteApi.getCmcMetadata(symbol)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val cmcInfo = it.data.get(symbol)?.get(0) // Coin Info from CoinMarketCap
                    if (cmcInfo != null) setupCmcData(cmcInfo!!)
                },
                {t ->
                    Log.d(
                        "InfoFragment",
                        "Error getting data from CmcMetaData",
                        t
                    )
                })
            .addTo(autoDisposable)


        return binding.root
    }

    private fun setupCmcData(cmcInfo: CmcCoinDataDTO) {
        cmcDescription = cmcInfo.description
        setDescription()
    }

    private fun setupCoinPaprikaData(
        cpInfo: CoinDetailsEntity
    ) {
        val coinPaprikaId = cpInfo.id
        val symbol = cpInfo.symbol

        // Coin logo
        Glide.with(binding.root)
            .load(cpInfo.logo)
            .centerCrop()
            .into(binding.logo)

        // Coin name and symbol
        val nameAndSymbol = cpInfo.name + " (" + cpInfo.symbol + ")"
        binding.name.text = nameAndSymbol

        // Launch Chart fragment on click
        binding.chartLink.setOnClickListener {
            (requireActivity() as MainActivity).launchChartFragment(
                coinPaprikaId,
                symbol
            )
        }

        // Coin description from both APIs
        cpDescription = cpInfo.description ?: ""
        setDescription()

        // setup main short info
        setupMainInfo(cpInfo)

        // tags
        val tags = cpInfo.tags
        if (!tags.isNullOrEmpty()) {
            tags.forEach { tag ->
                val itemViewBinding = OneColumnItemViewBinding.inflate(layoutInflater)
                itemViewBinding.value.text = tag.name
                itemViewBinding.root.setOnClickListener {
                    val message =
                        Html.fromHtml(
                            "<font color='#FFFFFF'>Coins: " + tag.coinCounter + "<br>" + "ICOs: " + tag.icoCounter + "\n" + (tag.description
                                ?: "") + "</font>", 0
                        )
                    val alert =
                        AlertDialog.Builder(binding.root.context, R.style.MyDialogTheme)
                            .setTitle(tag.name)
                            .setMessage(message)
                            .setPositiveButton(
                                "Ok",
                                DialogInterface.OnClickListener { dialog, which ->
                                    dialog.cancel()
                                })
                    alert.show()

                }
                binding.tagList.addView(itemViewBinding.root)
            }
        }

        // links
        val links = cpInfo.linksExtended
        if (!links.isNullOrEmpty()) {
            links.forEach {
                val itemViewBinding = TwoColumnItemViewBinding.inflate(layoutInflater)
                itemViewBinding.name.text = camelCaseToText(it.type)
                itemViewBinding.value.text = it.url
                binding.urls.addView(itemViewBinding.root)
            }
        }

        // team
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
    }

    private fun setDescription() {
        val description = if (cpDescription == "") {
            cmcDescription
        } else {
            cpDescription + "\n\n" + cmcDescription
        }
        binding.description.text = description
    }

    private fun setupMainInfo(cpInfo: CoinDetailsEntity) {

        if (cpInfo.openSource != null) {
            val itemViewBinding = TwoColumnItemViewBinding.inflate(layoutInflater)
            itemViewBinding.name.text = "Open source:"
            itemViewBinding.value.text = if(cpInfo.openSource!!) "Yes" else "No"
            binding.infoList.addView(itemViewBinding.root)
        }

        if (cpInfo.developmentStatus != null) {
            val itemViewBinding = TwoColumnItemViewBinding.inflate(layoutInflater)
            itemViewBinding.name.text = "Development status:"
            itemViewBinding.value.text = cpInfo.developmentStatus
            binding.infoList.addView(itemViewBinding.root)
        }

        if (cpInfo.proofType != null) {
            val itemViewBinding = TwoColumnItemViewBinding.inflate(layoutInflater)
            itemViewBinding.name.text = "Proof type:"
            itemViewBinding.value.text = cpInfo.proofType
            binding.infoList.addView(itemViewBinding.root)
        }

        if (cpInfo.organizationStructure != null) {
            val itemViewBinding = TwoColumnItemViewBinding.inflate(layoutInflater)
            itemViewBinding.name.text = "Org structure:"
            itemViewBinding.value.text = cpInfo.organizationStructure
            binding.infoList.addView(itemViewBinding.root)
        }

        if (cpInfo.algorithm != null) {
            val itemViewBinding = TwoColumnItemViewBinding.inflate(layoutInflater)
            itemViewBinding.name.text = "Hash algorithm:"
            itemViewBinding.value.text = cpInfo.algorithm
            binding.infoList.addView(itemViewBinding.root)
        }

        if (cpInfo.startedAt != null) {
            val itemViewBinding = TwoColumnItemViewBinding.inflate(layoutInflater)
            itemViewBinding.name.text = "Started:"
            itemViewBinding.value.text = cpInfo.startedAt!!.substring(0, 10)
            binding.infoList.addView(itemViewBinding.root)
        }

    }

    // Converts camel case name to normal text
    private fun camelCaseToText(s: String): String {
        return s.replaceFirstChar { c -> c.uppercase() }.replace('_', ' ')
    }

}