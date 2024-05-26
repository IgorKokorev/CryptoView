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

    var searchSorting = SearchSorting.NONE // field for tickers sorting
    var direction = 1 // sorting direction
    
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchSorting = viewModel.preferences.getSearchSorting()
        direction= viewModel.preferences.getSearchSortingDirection()
        setupDataFromViewModel()
        initRecycler()
        setupSorting()
        setUpSearch()

        binding.searchView.setOnClickListener {
            binding.searchView.isIconified = false
        }
    }

    override fun onPause() {
        super.onPause()
        savePreferences()
    }

    override fun onDestroy() {
        super.onDestroy()
        savePreferences()
    }

    private fun savePreferences() {
        viewModel.preferences.saveSearchSorting(sorting = searchSorting)
        viewModel.preferences.saveSearchSortingDirection(direction)
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
    private fun setUpSearch() {
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
            .map { str ->
                tickers.filter { ticker ->
                    ticker.symbol.contains(str, true) || ticker.name.contains(str, true)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { list ->
                tickersToShow = list
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
    private fun sortTickers(toSort: SearchSorting) {
        direction = if (searchSorting == toSort) -direction else 1
        searchSorting = toSort
        tickersToShow = setArrowAndSort(tickersToShow)
    }

    // sorting the list and set the according arrow
    private fun setArrowAndSort(tickersToSort: List<CoinPaprikaTickerDB>): List<CoinPaprikaTickerDB> {
        val iconUp = Icon.createWithResource(context, R.drawable.icon_arrow_up)
        val iconDown = Icon.createWithResource(context, R.drawable.icon_arrow_down)

        clearArrows()

        return if (direction > 0) {
            when (searchSorting) {
                MCAP -> tickersToSort.sortedByDescending {
                    binding.headerMcapArrow.setImageIcon(iconDown)
                    it.marketCap
                }
                ATH -> tickersToSort.sortedByDescending { it.athPrice }
                ATH_CHANGE -> tickersToSort.sortedByDescending {
                    binding.headerAthArrow.setImageIcon(iconDown)
                    it.percentFromPriceAth
                }
                SYMBOL -> tickersToSort.sortedBy {
                    binding.headerSymbolArrow.setImageIcon(iconUp)
                    it.symbol
                }
                NAME -> tickersToSort.sortedBy {
                    binding.headerNameArrow.setImageIcon(iconUp)
                    it.name
                }
                VOLUME -> tickersToSort.sortedByDescending {
                    binding.headerVolumeArrow.setImageIcon(iconDown)
                    it.dailyVolume
                }
                CHANGE24HR -> tickersToSort.sortedByDescending {
                    binding.headerPriceArrow.setImageIcon(iconDown)
                    it.percentChange24h
                }
                PRICE -> tickersToSort.sortedByDescending { it.price }
                else -> tickersToSort
            }
        } else {
            when (searchSorting) {
                MCAP -> tickersToSort.sortedBy {
                    binding.headerMcapArrow.setImageIcon(iconUp)
                    it.marketCap
                }
                ATH -> tickersToSort.sortedBy { it.athPrice }
                ATH_CHANGE -> tickersToSort.sortedBy {
                    binding.headerAthArrow.setImageIcon(iconUp)
                    it.percentFromPriceAth
                }
                SYMBOL -> tickersToSort.sortedByDescending {
                    binding.headerSymbolArrow.setImageIcon(iconDown)
                    it.symbol
                }
                NAME -> tickersToSort.sortedByDescending {
                    binding.headerNameArrow.setImageIcon(iconDown)
                    it.name
                }
                VOLUME -> tickersToSort.sortedBy {
                    binding.headerVolumeArrow.setImageIcon(iconUp)
                    it.dailyVolume
                }
                CHANGE24HR -> tickersToSort.sortedBy {
                    binding.headerPriceArrow.setImageIcon(iconUp)
                    it.percentChange24h
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



