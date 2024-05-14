package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.databinding.FragmentSearchBinding
import dev.kokorev.cryptoview.databinding.SearchCoinItemBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.SearchViewModel
import dev.kokorev.cryptoview.views.MainActivity
import dev.kokorev.cryptoview.views.fragments.LastSorting.ATH
import dev.kokorev.cryptoview.views.fragments.LastSorting.ATH_CHANGE
import dev.kokorev.cryptoview.views.fragments.LastSorting.CHANGE24HR
import dev.kokorev.cryptoview.views.fragments.LastSorting.MCAP
import dev.kokorev.cryptoview.views.fragments.LastSorting.NAME
import dev.kokorev.cryptoview.views.fragments.LastSorting.PRICE
import dev.kokorev.cryptoview.views.fragments.LastSorting.SYMBOL
import dev.kokorev.cryptoview.views.fragments.LastSorting.VOLUME
import dev.kokorev.cryptoview.views.rvadapters.SearchAdapter
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var searchAdapter: SearchAdapter
    private var tickers: List<CoinPaprikaTickerDB> = listOf()
        set(value) {
            if (field == value) return
            field = value.sort(viewModel.sorting, viewModel.direction)
            searchAdapter.addItems(field)
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
        setupDataFromViewModel()
        initRecycler()
        setupSorting()
    }

    // initializing RV
    private fun initRecycler() {
        searchAdapter = SearchAdapter(object : SearchAdapter.OnItemClickListener {
            override fun click(
                coinPaprikaTickerDB: CoinPaprikaTickerDB,
                position: Int,
                binding: SearchCoinItemBinding
            ) {
                (requireActivity() as MainActivity).launchCoinFragment(
                    coinPaprikaTickerDB.coinPaprikaId,
                    coinPaprikaTickerDB.symbol,
                    coinPaprikaTickerDB.name
                )
            }
        })
        searchAdapter.addItems(tickers)
        binding.mainRecycler.adapter = searchAdapter
//        binding.mainRecycler.addItemDecoration(TopSpacingItemDecoration(0))
    }

    //setup viewModel.sorting on click on headers
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

    private fun sortTickers(toSort: LastSorting) {
        viewModel.direction = if (viewModel.sorting == toSort) -viewModel.direction else 1
        viewModel.sorting = toSort
        tickers = tickers.sort(viewModel.sorting, viewModel.direction)
        binding.mainRecycler.layoutManager?.scrollToPosition(0)
    }

    private fun setupDataFromViewModel() {
        viewModel.loadTickers()
        viewModel.cpTickers
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { dto ->
                    tickers = dto.filter { ticker ->
                        (ticker.dailyVolume ?: 0.0) > Constants.MIN_VOLUME &&
                                (ticker.marketCap ?: 0.0) > Constants.MIN_MCAP
                    }
//                        .sortedByDescending { it.dailyVolume }
                },
                {
                    Log.d(
                        "SearchFragment",
                        "Error getting data from local DB CoinPaprikaTicker",
                        it
                    )
                })
            .addTo(autoDisposable)
    }


}

private fun List<CoinPaprikaTickerDB>.sort(
    sorting: LastSorting,
    direction: Int
): List<CoinPaprikaTickerDB> {
    return if (direction > 0) {
        when (sorting) {
            MCAP -> this.sortedByDescending { it.marketCap }
            ATH -> this.sortedByDescending { it.athPrice }
            ATH_CHANGE -> this.sortedByDescending { it.percentFromPriceAth }
            SYMBOL -> this.sortedBy { it.symbol }
            NAME -> this.sortedBy { it.name }
            VOLUME -> this.sortedByDescending { it.dailyVolume }
            CHANGE24HR -> this.sortedByDescending { it.percentChange24h }
            PRICE -> this.sortedByDescending { it.price }
            else -> this
        }
    } else {
        when (sorting) {
            MCAP -> this.sortedBy { it.marketCap }
            ATH -> this.sortedBy { it.athPrice }
            ATH_CHANGE -> this.sortedBy { it.percentFromPriceAth }
            SYMBOL -> this.sortedByDescending { it.symbol }
            NAME -> this.sortedByDescending { it.name }
            VOLUME -> this.sortedBy { it.dailyVolume }
            CHANGE24HR -> this.sortedBy { it.percentChange24h }
            PRICE -> this.sortedBy { it.price }
            else -> this
        }
    }
}


