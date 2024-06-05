package dev.kokorev.cryptoview.views.fragments

import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesInt
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesSearchSorting
import dev.kokorev.cryptoview.databinding.FragmentSearchBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.SearchViewModel
import dev.kokorev.cryptoview.views.MainActivity
import dev.kokorev.cryptoview.views.fragments.SearchSorting.ATH
import dev.kokorev.cryptoview.views.fragments.SearchSorting.ATH_CHANGE
import dev.kokorev.cryptoview.views.fragments.SearchSorting.CHANGE24HR
import dev.kokorev.cryptoview.views.fragments.SearchSorting.MCAP
import dev.kokorev.cryptoview.views.fragments.SearchSorting.NAME
import dev.kokorev.cryptoview.views.fragments.SearchSorting.PRICE
import dev.kokorev.cryptoview.views.fragments.SearchSorting.SYMBOL
import dev.kokorev.cryptoview.views.fragments.SearchSorting.VOLUME
import dev.kokorev.cryptoview.views.rvadapters.SearchAdapter
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var searchAdapter: SearchAdapter
    private var tickers: List<CoinPaprikaTickerDB> = listOf()
    
    private var sortingDirection: Int by preferencesInt("searchSortingDirection")
    private var sorting: SearchSorting by preferencesSearchSorting("searchSorting")

    private var tickersToShow: List<CoinPaprikaTickerDB> = listOf()
        set(value) {
            if (field == value) return
            field = value
            searchAdapter.addItems(field)
            binding.mainRecycler.layoutManager?.scrollToPosition(0) // otherwise we can be in the middle of the list after sorting
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autoDisposable.bindTo(lifecycle)

        binding = FragmentSearchBinding.inflate(layoutInflater)

        setupDataFromViewModel()

        initRecycler()
        setupSorting()
        setupSearch()

        binding.searchView.setOnClickListener {
            binding.searchView.isIconified = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    // initializing RV
    private fun initRecycler() {
        searchAdapter = SearchAdapter(object : SearchAdapter.OnItemClickListener {
            override fun click(coinPaprikaTickerDB: CoinPaprikaTickerDB) {
                (requireActivity() as MainActivity).launchCoinFragment(
                    coinPaprikaTickerDB.coinPaprikaId,
                    coinPaprikaTickerDB.symbol,
                    coinPaprikaTickerDB.name
                )
            }
        })
        binding.mainRecycler.adapter = searchAdapter
    }

    //setup sorting on click on headers
    private fun setupSorting() {
        binding.headerMcap.setOnClickListener { sortTickers(MCAP) }
        binding.headerVolume.setOnClickListener { sortTickers(VOLUME) }
        binding.headerSymbol.setOnClickListener { sortTickers(SYMBOL) }
        binding.headerName.setOnClickListener { sortTickers(NAME) }
        binding.headerPrice.setOnClickListener { sortTickers(CHANGE24HR) }
        binding.headerChange.setOnClickListener { sortTickers(CHANGE24HR) }
        binding.headerAthChange.setOnClickListener { sortTickers(ATH_CHANGE) }
        binding.headerAth.setOnClickListener { sortTickers(ATH_CHANGE) }
    }

    // Search coins logic. Search request is sent only after 1 seconds of no input
    private fun setupSearch() {
        Observable.create<String> {
            binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = true

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrBlank()) {
                        tickersToShow = tickers
                        return true
                    }
                    it.onNext(newText)
                    return true
                }
            })
        }
            .debounce(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { str ->
                tickersToShow = tickers.filter { ticker ->
                    ticker.symbol.contains(str, true) || ticker.name.contains(str, true)
                }
            }
            .addTo(autoDisposable)

    }

    // remove all the arrows indicating sorting
    private fun clearArrows() {
        binding.apply {
            headerSymbolArrow.setImageIcon(null)
            headerNameArrow.setImageIcon(null)
            headerPriceArrow.setImageIcon(null)
            headerAthArrow.setImageIcon(null)
            headerVolumeArrow.setImageIcon(null)
            headerMcapArrow.setImageIcon(null)
        }
    }

    // check if the sorting field was clicked first time
    private fun sortTickers(newSorting: SearchSorting) {
        sortingDirection = if (sorting == newSorting) -sortingDirection else 1
        sorting = newSorting
        tickersToShow = setArrowAndSort(tickers)
    }

    // sorting the list and set the according arrow
    private fun setArrowAndSort(tickersToSort: List<CoinPaprikaTickerDB>): List<CoinPaprikaTickerDB> {
        val iconUp = Icon.createWithResource(context, R.drawable.icon_arrow_up)
        val iconDown = Icon.createWithResource(context, R.drawable.icon_arrow_down)

        clearArrows()

        return if (sortingDirection > 0) {
            when (sorting) {
                MCAP -> {
                    binding.headerMcapArrow.setImageIcon(iconDown)
                    tickersToSort.sortedByDescending { it.marketCap }
                }
                ATH -> tickersToSort.sortedByDescending { it.athPrice }
                ATH_CHANGE -> {
                    binding.headerAthArrow.setImageIcon(iconDown)
                    tickersToSort.sortedByDescending { it.percentFromPriceAth }
                }
                SYMBOL -> {
                    binding.headerSymbolArrow.setImageIcon(iconUp)
                    tickersToSort.sortedBy { it.symbol }
                }
                NAME -> {
                    binding.headerNameArrow.setImageIcon(iconUp)
                    tickersToSort.sortedBy { it.name }
                }
                VOLUME -> {
                    binding.headerVolumeArrow.setImageIcon(iconDown)
                    tickersToSort.sortedByDescending { it.dailyVolume }
                }
                CHANGE24HR -> {
                    binding.headerPriceArrow.setImageIcon(iconDown)
                    tickersToSort.sortedByDescending { it.percentChange24h }
                }
                PRICE -> tickersToSort.sortedByDescending { it.price }
                else -> tickersToSort
            }
        } else {
            when (sorting) {
                MCAP -> {
                    binding.headerMcapArrow.setImageIcon(iconUp)
                    tickersToSort.sortedBy { it.marketCap }
                }
                ATH -> tickersToSort.sortedBy { it.athPrice }
                ATH_CHANGE -> {
                    binding.headerAthArrow.setImageIcon(iconUp)
                    tickersToSort.sortedBy { it.percentFromPriceAth }
                }
                SYMBOL -> {
                    binding.headerSymbolArrow.setImageIcon(iconDown)
                    tickersToSort.sortedByDescending { it.symbol }
                }
                NAME -> {
                    binding.headerNameArrow.setImageIcon(iconDown)
                    tickersToSort.sortedByDescending { it.name }
                }
                VOLUME -> {
                    binding.headerVolumeArrow.setImageIcon(iconUp)
                    tickersToSort.sortedBy { it.dailyVolume }
                }
                CHANGE24HR -> {
                    binding.headerPriceArrow.setImageIcon(iconUp)
                    tickersToSort.sortedBy { it.percentChange24h }
                }
                PRICE -> tickersToSort.sortedBy { it.price }
                else -> tickersToSort
            }
        }
    }

    private fun  setupDataFromViewModel() {
        viewModel.allTickers
            .subscribe()
                { dto ->
                    tickers = setArrowAndSort(dto)
                    tickersToShow = tickers
                }
            .addTo(autoDisposable)
    }
}



