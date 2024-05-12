package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import dev.kokorev.cryptoview.views.fragments.LastSorting.CHANGE24HR
import dev.kokorev.cryptoview.views.fragments.LastSorting.MCAP
import dev.kokorev.cryptoview.views.fragments.LastSorting.PRICE
import dev.kokorev.cryptoview.views.fragments.LastSorting.SYMBOL
import dev.kokorev.cryptoview.views.fragments.LastSorting.VOLUME
import dev.kokorev.cryptoview.views.rvadapters.SearchAdapter
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTicker
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var searchAdapter: SearchAdapter
    private var tickers: List<CoinPaprikaTicker> = listOf()
        set(value) {
            if (field == value) return
            field = value.sort(viewModel.sorting, viewModel.direction)
            searchAdapter.addItems(field)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autoDisposable.bindTo(lifecycle)
        binding = FragmentSearchBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupDataFromViewModel()
        initRecycler()
        setupSorting()
        return binding.root
    }

    // initializing RV
    private fun initRecycler() {
        searchAdapter = SearchAdapter(object : SearchAdapter.OnItemClickListener {
            override fun click(
                coinPaprikaTicker: CoinPaprikaTicker,
                position: Int,
                binding: SearchCoinItemBinding
            ) {
                (requireActivity() as MainActivity).launchCoinFragment(
                    coinPaprikaTicker.coinPaprikaId,
                    coinPaprikaTicker.symbol,
                    coinPaprikaTicker.name
                )
            }
        })
//        searchAdapter.addItems(tickers)
        binding.mainRecycler.adapter = searchAdapter
//        binding.mainRecycler.addItemDecoration(TopSpacingItemDecoration(0))
    }

    //setup viewModel.sorting on click on headers
    private fun setupSorting() {
        binding.headerMcap.setOnClickListener {
            viewModel.direction = if (viewModel.sorting == MCAP) -viewModel.direction else 1
            viewModel.sorting = MCAP
            tickers = tickers.sort(viewModel.sorting, viewModel.direction)
            binding.mainRecycler.layoutManager?.scrollToPosition(0)
            Toast.makeText(binding.root.context, "viewModel.sorting by MCap", Toast.LENGTH_SHORT).show()
        }

        binding.headerVolume.setOnClickListener {
            viewModel.direction = if (viewModel.sorting == VOLUME) -viewModel.direction else 1
            viewModel.sorting = VOLUME
            tickers = tickers.sort(viewModel.sorting, viewModel.direction)
            binding.mainRecycler.layoutManager?.scrollToPosition(0)
            Toast.makeText(binding.root.context, "viewModel.sorting by Volume", Toast.LENGTH_SHORT).show()
        }

        binding.headerSymbol.setOnClickListener {
            viewModel.direction = if (viewModel.sorting == SYMBOL) -viewModel.direction else 1
            viewModel.sorting = SYMBOL
            tickers = tickers.sort(viewModel.sorting, viewModel.direction)
            binding.mainRecycler.layoutManager?.scrollToPosition(0)
            Toast.makeText(binding.root.context, "viewModel.sorting by Symbol", Toast.LENGTH_SHORT).show()
        }

        binding.headerChange.setOnClickListener {
            viewModel.direction = if (viewModel.sorting == CHANGE24HR) -viewModel.direction else 1
            viewModel.sorting = CHANGE24HR
            tickers = tickers.sort(viewModel.sorting, viewModel.direction)
            binding.mainRecycler.layoutManager?.scrollToPosition(0)
            Toast.makeText(
                binding.root.context,
                "viewModel.sorting by 24hr percent change",
                Toast.LENGTH_SHORT
            ).show()
        }

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
                        .sortedByDescending { it.dailyVolume }
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

private fun List<CoinPaprikaTicker>.sort(
    sorting: LastSorting,
    direction: Int
): List<CoinPaprikaTicker> {
    return  if (direction > 0) {
        when (sorting) {
            MCAP -> this.sortedByDescending { it.marketCap }
            ATH -> this.sortedByDescending { it.athPrice }
            SYMBOL -> this.sortedBy { it.symbol }
            VOLUME -> this.sortedByDescending { it.dailyVolume }
            CHANGE24HR -> this.sortedByDescending { it.percentChange24h }
            PRICE -> this.sortedByDescending { it.price }
            else -> this
        }
    } else {
        when (sorting) {
            MCAP -> this.sortedBy { it.marketCap }
            ATH -> this.sortedBy { it.athPrice }
            SYMBOL -> this.sortedByDescending { it.symbol }
            VOLUME -> this.sortedBy { it.dailyVolume }
            CHANGE24HR -> this.sortedBy { it.percentChange24h }
            PRICE -> this.sortedBy { it.price }
            else -> this
        }
    }
}


