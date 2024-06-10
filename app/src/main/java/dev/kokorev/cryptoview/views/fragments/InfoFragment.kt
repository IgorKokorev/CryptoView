package dev.kokorev.cryptoview.views.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import com.coinpaprika.apiclient.entity.LinkExtendedEntity
import com.coinpaprika.apiclient.entity.TagEntity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.kokorev.cmc_api.entity.cmc_metadata.CmcCoinDataDTO
import dev.kokorev.cmc_api.entity.cmc_metadata.CmcMetadataDTO
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.FragmentInfoBinding
import dev.kokorev.cryptoview.databinding.LinkItemBinding
import dev.kokorev.cryptoview.databinding.LinkItemStatBinding
import dev.kokorev.cryptoview.databinding.TwoColumnItemViewBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.utils.PortfolioInteractor
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.CoinViewModel

//Shows genegal Information about the coin
class InfoFragment : Fragment() {
    private lateinit var binding: FragmentInfoBinding
    private lateinit var portfolioInteractor: PortfolioInteractor
    private val autoDisposable = AutoDisposable()
    private val viewModel: CoinViewModel by viewModels<CoinViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private var cpDescription = "" // coin description from CoinPaprika
    private var cmcDescription = "" // coin description from CoinMarketCap
    private var isFavorite = false
    private var linkToImage: Map<String, Int> = mutableMapOf()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autoDisposable.bindTo(lifecycle)
        binding = FragmentInfoBinding.inflate(layoutInflater)
        portfolioInteractor = PortfolioInteractor(binding.root, autoDisposable)
        
        linkToImage = mapOf(
            "announcement" to R.drawable.icon_announcement2,
            "blog" to R.drawable.icon_blog2,
            "explorer" to R.drawable.icon_explorer2,
            "facebook" to R.drawable.icon_facebook,
            "reddit" to R.drawable.icon_reddit,
            "slack" to R.drawable.icon_slack,
            "source_code" to R.drawable.icon_github,
            "telegram" to R.drawable.icon_telegram,
            "twitter" to R.drawable.icon_twitter,
            "website" to R.drawable.icon_website3,
            "youtube" to R.drawable.icon_youtube,
            "chat" to R.drawable.icon_chat,
            "discord" to R.drawable.icon_discord,
            "wallet" to R.drawable.icon_wallet,
            "message_board" to R.drawable.icon_message_board,
        )
        
        getCoinPaprikaInfo()
        getCmcInfo()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    // get coin info from CoinMarketCap API and set to view. In fact we need only description
    private fun getCmcInfo() {
        viewModel.getCmcMetadata()
            .doOnSuccess {
                // Coin Info from CoinMarketCap
                findCoinInCmcMetadata(it)
                if (viewModel.cmcInfo != null) setupCmcData(viewModel.cmcInfo!!)
            }
            .onErrorComplete()
            .subscribe()
            .addTo(autoDisposable)
    }

    // get coin info from CoinPaprika API and set to view
    private fun getCoinPaprikaInfo() {
        viewModel.getCoinPaprikaCoinInfo()
            .doOnSuccess {
                viewModel.cpInfo = it
                setupCoinPaprikaData(it)
                viewModel.saveRecent(it)
                setupFavoriteFab(it)
                setupPortfolioFab(it)
            }
            .onErrorComplete()
            .subscribe()
            .addTo(autoDisposable)
    }

    private fun setupPortfolioFab(coin: CoinDetailsEntity) {
        binding.portfolioFab.setOnClickListener {
            Log.d(this.javaClass.simpleName, "Looking for ${coin.symbol} in Portfolio db")

            viewModel.getPortfolioPositionByCPId()
                // If position is found in Portfolio - change it
                .doOnSuccess { portfolioCoinDB ->
                    Log.d(this.javaClass.simpleName, portfolioCoinDB.symbol)
                    portfolioInteractor.changePosition(portfolioCoinDB)
                }
                // if not found - open it
                .doOnComplete {
                    Log.d(this.javaClass.simpleName, "Empty response")
                    portfolioInteractor.openPosition(coin)
                }
                .subscribe()
                .addTo(autoDisposable)

        }
    }

    // select correct coin in data list in CMC info
    private fun findCoinInCmcMetadata(it: CmcMetadataDTO) {
        val list = it.data.get(viewModel.symbol)
        if (list.isNullOrEmpty()) return
        if (list.size == 1) {
            viewModel.cmcInfo = list.get(0)
            return
        }
        list.forEach {
            if (it.name.lowercase() == viewModel.name.lowercase()) {
                viewModel.cmcInfo = it
                return
            }
        }
    }

    private fun setupFavoriteFab(coin: CoinDetailsEntity) {
        binding.favoriteFab.setOnClickListener {
            if (isFavorite) {
                viewModel.deleteFavorite()
            } else {
                viewModel.saveFavorite(coin)
            }
            isFavorite = !isFavorite
            setFavoriteIcon()
        }
    }

    // set 'add to favorites' fab icon depending on status
    private fun setFavoriteIcon() {
        binding.favoriteFab.setImageResource(
            if (isFavorite) R.drawable.icon_star_fill
            else R.drawable.icon_star_empty
        )
    }

    private fun setupCmcData(cmcInfo: CmcCoinDataDTO) {
        cmcDescription = cmcInfo.description
        setDescription()
    }

    private fun setupCoinPaprikaData(cpInfo: CoinDetailsEntity) {
        // check if coin is in favorites
        viewModel.findFavoriteCoinByCoinPaprikaId()
            .subscribe() {
                isFavorite = true
                setFavoriteIcon()
            }
            .addTo(autoDisposable)

        // Coin logo
        Glide.with(binding.root)
            .load(cpInfo.logo)
            .centerCrop()
            .into(binding.logo)

        // Coin name and symbol
        val nameAndSymbol = cpInfo.name + " (" + cpInfo.symbol + ")"
        binding.name.text = nameAndSymbol

        // Coin description from both APIs
        cpDescription = cpInfo.description ?: ""
        setDescription()

        // setup main short info
        setupMainInfo(cpInfo)

        // tags
        val tags = cpInfo.tags
        setTags(tags)

        // links
        val links = cpInfo.linksExtended
        setLinks(links)

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
    
    private fun setTags(tags: List<TagEntity>?) {
        if (!tags.isNullOrEmpty()) {
            tags.forEach { tag ->
                val itemViewBinding = LinkItemBinding.inflate(layoutInflater).apply {
                    name.text = tag.name
                    root.setOnClickListener {
                        val message = "Coins: " + tag.coinCounter + "\n" + "ICOs: " + tag.icoCounter
                        MaterialAlertDialogBuilder(binding.root.context, R.style.CVDialogStyle)
                            .setTitle(tag.name)
                            .setMessage(message)
                            .setPositiveButton("Ok") { dialog, _ ->
                                dialog.cancel()
                            }
                            .show()
                        
                    }
                }
                binding.tagList.addView(itemViewBinding.root)
            }
        }
    }
    
    private fun setLinks(links: List<LinkExtendedEntity>?) {
        if (!links.isNullOrEmpty()) {
            links.forEach {linkEntity ->
                val itemBinding = LinkItemBinding.inflate(layoutInflater).apply {
                    name.text = camelCaseToText(linkEntity.type)
                    logo.setImageResource(
                        linkToImage.get(linkEntity.type) ?: R.drawable.icon_internet
                    )
                    root.setOnClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(linkEntity.url)));
                    }
                }
                linkEntity.stats?.forEach { statName, stat ->
                    val statBinding = LinkItemStatBinding.inflate(layoutInflater)
                    val str = camelCaseToText(statName) + ": " + NumbersUtils.formatBigNumber(stat.toDouble())
                    statBinding.statText.text = str
                    itemBinding.statContainer.addView(statBinding.root)
                }
                binding.urls.addView(itemBinding.root)
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
            itemViewBinding.name.text = getString(R.string.open_source)
            itemViewBinding.value.text = if (cpInfo.openSource!!) getString(R.string.yes)
            else getString(R.string.no)
            binding.infoList.addView(itemViewBinding.root)
        }

        if (cpInfo.developmentStatus != null) {
            val itemViewBinding = TwoColumnItemViewBinding.inflate(layoutInflater)
            itemViewBinding.name.text = getString(R.string.development_status)
            itemViewBinding.value.text = cpInfo.developmentStatus
            binding.infoList.addView(itemViewBinding.root)
        }

        if (cpInfo.proofType != null) {
            val itemViewBinding = TwoColumnItemViewBinding.inflate(layoutInflater)
            itemViewBinding.name.text = getString(R.string.proof_type)
            itemViewBinding.value.text = cpInfo.proofType
            binding.infoList.addView(itemViewBinding.root)
        }

        if (cpInfo.organizationStructure != null) {
            val itemViewBinding = TwoColumnItemViewBinding.inflate(layoutInflater)
            itemViewBinding.name.text = getString(R.string.org_structure)
            itemViewBinding.value.text = cpInfo.organizationStructure
            binding.infoList.addView(itemViewBinding.root)
        }

        if (cpInfo.algorithm != null) {
            val itemViewBinding = TwoColumnItemViewBinding.inflate(layoutInflater)
            itemViewBinding.name.text = getString(R.string.hash_algorithm)
            itemViewBinding.value.text = cpInfo.algorithm
            binding.infoList.addView(itemViewBinding.root)
        }

        if (cpInfo.startedAt != null) {
            val itemViewBinding = TwoColumnItemViewBinding.inflate(layoutInflater)
            itemViewBinding.name.text = getString(R.string.started)
            itemViewBinding.value.text = cpInfo.startedAt!!.substring(0, 10)
            binding.infoList.addView(itemViewBinding.root)
        }

    }

    // Converts camel case name to normal text
    private fun camelCaseToText(s: String): String = s.replaceFirstChar { c -> c.uppercase() }.replace('_', ' ')
}

fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)