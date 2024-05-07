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
import dev.kokorev.cryptoview.views.rvadapters.SearchAdapter
import dev.kokorev.cryptoview.views.rvadapters.TopSpacingItemDecoration
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
            field = value.sortedByDescending { it.dailyVolume }
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
        return binding.root
    }

    private fun initRecycler() {
        searchAdapter = SearchAdapter(object : SearchAdapter.OnItemClickListener {
            override fun click(
                coinPaprikaTicker: CoinPaprikaTicker,
                position: Int,
                binding: SearchCoinItemBinding
            ) {
                (requireActivity() as MainActivity).launchInfoFragment(
                    coinPaprikaTicker.coinPaprikaId,
                    coinPaprikaTicker.symbol
                )
            }
        })
        searchAdapter.addItems(tickers)
        binding.mainRecycler.adapter = searchAdapter
        binding.mainRecycler.addItemDecoration(TopSpacingItemDecoration(0))
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