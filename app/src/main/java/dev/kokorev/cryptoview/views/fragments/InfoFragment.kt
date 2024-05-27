package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import com.coinpaprika.apiclient.entity.FavoriteCoinDB
import com.coinpaprika.apiclient.entity.PortfolioCoinDB
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.kokorev.cmc_api.entity.cmc_metadata.CmcCoinDataDTO
import dev.kokorev.cmc_api.entity.cmc_metadata.CmcMetadataDTO
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.AlertViewOpenPositionBinding
import dev.kokorev.cryptoview.databinding.FragmentInfoBinding
import dev.kokorev.cryptoview.databinding.OneColumnItemViewBinding
import dev.kokorev.cryptoview.databinding.TwoColumnItemViewBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.CoinViewModel
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB

class InfoFragment : Fragment() {
    private lateinit var binding: FragmentInfoBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: CoinViewModel by viewModels<CoinViewModel>(
        ownerProducer = { requireParentFragment() }
    )
    private var cpDescription = ""
    private var cmcDescription = ""
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoBinding.inflate(layoutInflater)

        setCoinPaprikaInfo()
        setCmcInfo()

        return binding.root
    }

    // get coin info from CoinMarketCap API and set to view. In fact we need only description
    private fun setCmcInfo() {
        viewModel.remoteApi.getCmcMetadata(viewModel.symbol)
            .doOnSuccess {
                // Coin Info from CoinMarketCap
                findCoin(it)
                if (viewModel.cmcInfo != null) setupCmcData(viewModel.cmcInfo!!)
            }
            .subscribe()
            .addTo(autoDisposable)
    }

    // get coin info from CoinPaprika API and set to view
    private fun setCoinPaprikaInfo() {
        viewModel.remoteApi.getCoinPaprikaCoinInfo(viewModel.coinPaprikaId)
            .doOnSuccess {
                viewModel.cpInfo = it
                setupCoinPaprikaData(it)
                val recentCoinDB = Converter.CoinDetailsEntityToRecentCoinDB(it)
                viewModel.repository.addRecent(recentCoinDB)
                setupFavoriteFab(it)
                setupPortfolioFab(it)
            }
            .subscribe()
            .addTo(autoDisposable)
    }

    private fun setupPortfolioFab(coin: CoinDetailsEntity) {
        binding.portfolioFab.setOnClickListener {
            Log.d(this.javaClass.simpleName, "Looking for ${coin.symbol} in Portfolio db")

            viewModel.repository.getPortfolioCoinByCPId(coin.id)
                .doOnSuccess {
                    Log.d(this.javaClass.simpleName, it.symbol)
                    changePosition(it)
                }
                .doOnError {
                    Log.d(this.javaClass.simpleName, it.localizedMessage ?: "No message found")
                }
                .doOnComplete {
                    Log.d(this.javaClass.simpleName, "Empty response")
                    openPosition(coin)
                }
                .subscribe()
                .addTo(autoDisposable)

        }
    }

    private fun openPosition(coin: CoinDetailsEntity) {
        viewModel.repository.getCPTickerById(coin.id)
            .doOnSuccess { ticker ->
                val price = ticker.price ?: 0.0
                Log.d(
                    this.javaClass.simpleName,
                    "Opening position coin: ${coin.symbol}, price: $price"
                )
                askUserQtyToOpenPosition(coin, price)
            }
            .doOnError {
                Log.d(
                    this.javaClass.simpleName,
                    "getPortfolioCoinByCPId error: ${it.localizedMessage}"
                )
            }
            .doOnComplete {
                Snackbar.make(
                    binding.root,
                    "Price for ${coin.symbol} hasn't been found",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .subscribe()
            .addTo(autoDisposable)
    }

    private fun askUserQtyToOpenPosition(
        coin: CoinDetailsEntity,
        price: Double
    ) {
        if (price == 0.0) return
        val inputViewBinding = AlertViewOpenPositionBinding.inflate(layoutInflater)
        inputViewBinding.price.text = NumbersUtils.formatPrice(price)
        inputViewBinding.symbol.text = coin.symbol
        inputViewBinding.input.addTextChangedListener {
            val num = try {
                it.toString().toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
            inputViewBinding.inputValue.text = NumbersUtils.formatPrice(num * price)
        }

        MaterialAlertDialogBuilder(binding.root.context, R.style.CVDialogStyle)
            .setTitle(getString(R.string.portfolio_operation))
            .setView(inputViewBinding.root)
            .setPositiveButton("Ok") { dialog, which ->
                val qty = try {
                    inputViewBinding.input.text.toString().toDouble()
                } catch (e: NumberFormatException) {
                    0.0
                }
                // If 0 is entered - no position created
                if (qty == 0.0) {
                    MaterialAlertDialogBuilder(binding.root.context, R.style.CVDialogStyle)
                        .setTitle(getString(R.string.portfolio_operation))
                        .setMessage(getString(R.string.no_quantity_entered_the_position_isn_t_saved))
                        .setPositiveButton("Ok") { dialogEmptyInput, whichEmptyInput ->
                            dialogEmptyInput.cancel()
                        }
                        .show()
                } else {
                    val portfolioCoinDB: PortfolioCoinDB = Converter.createPortfolioCoin(coin, price, qty)
                    viewModel.repository.savePortfolioCoin(portfolioCoinDB)
                    Log.d(this.javaClass.simpleName, "askUserQtyToOpenPosition added to portfolio: ${portfolioCoinDB}")
                }
                Log.d(this.javaClass.simpleName, "askUserQtyToOpenPosition user input: ${qty}")
                dialog.cancel()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }
            .show()
        return
    }

    private fun changePosition(portfolioCoinDB: PortfolioCoinDB) {
        viewModel.repository.getCPTickerById(portfolioCoinDB.coinPaprikaId)
            .doOnSuccess { ticker ->
                Log.d(
                    this.javaClass.simpleName,
                    "Changing position coin: ${portfolioCoinDB.symbol}, price: ${ticker.price}"
                )

                val qty: Double = askUserQtyToChangePosition(portfolioCoinDB, ticker)

            }
            .doOnError {
                Log.d(
                    this.javaClass.simpleName,
                    "getPortfolioCoinByCPId error: ${it.localizedMessage}"
                )
            }
            .doOnComplete {
                Snackbar.make(
                    binding.root,
                    "Price for ${portfolioCoinDB.symbol} hasn't been found",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .subscribe()
            .addTo(autoDisposable)
    }

    private fun askUserQtyToChangePosition(
        portfolioCoinDB: PortfolioCoinDB,
        ticker: CoinPaprikaTickerDB
    ): Double {

        return 0.0
    }

    // select correct coin in data list in CMC info
    private fun findCoin(it: CmcMetadataDTO) {
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
                viewModel.repository.deleteFavorite(coin.id)
            } else {
                val favoriteCoinDB: FavoriteCoinDB =
                    Converter.CoinDetailsEntityToFavoriteCoinDB(coin)
                viewModel.repository.addFavorite(favoriteCoinDB)
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
        val coinPaprikaId = cpInfo.id

        // check if coin is in favorites
        viewModel.repository.findFavoriteCoinByCoinPaprikaId(coinPaprikaId)
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
        if (!tags.isNullOrEmpty()) {
            tags.forEach { tag ->
                val itemViewBinding = OneColumnItemViewBinding.inflate(layoutInflater)
                itemViewBinding.value.text = tag.name
                itemViewBinding.root.setOnClickListener {
                    val message = "Coins: " + tag.coinCounter + "\n" + "ICOs: " + tag.icoCounter
                    MaterialAlertDialogBuilder(binding.root.context, R.style.CVDialogStyle)
                        .setTitle(tag.name)
                        .setMessage(message)
                        .setPositiveButton("Ok") { dialog, which ->
                            dialog.cancel()
                        }
                        .show()

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
    private fun camelCaseToText(s: String): String {
        return s.replaceFirstChar { c -> c.uppercase() }.replace('_', ' ')
    }

}


fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)