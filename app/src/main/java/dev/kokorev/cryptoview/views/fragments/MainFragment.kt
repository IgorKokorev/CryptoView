package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.kokorev.cryptoview.databinding.FragmentMainBinding
import dev.kokorev.cryptoview.databinding.MainCoinItemBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.MainViewModel
import dev.kokorev.cryptoview.views.MainActivity
import dev.kokorev.cryptoview.views.rvadapters.MainAdapter
import dev.kokorev.room_db.core_api.entity.TopMover
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: MainViewModel by viewModels()
    private lateinit var mainAdapter: MainAdapter

    private var topMovers: List<TopMover> = listOf()
        set(value) {
            if (field == value) return
            field = value.sortedByDescending { it.percentChange }
            mainAdapter.addItems(field)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDataFromViewModel()
        initRecycler()
    }

    private fun initRecycler() {
        mainAdapter = MainAdapter(object : MainAdapter.OnItemClickListener {
            override fun click(
                topMover: TopMover,
                position: Int,
                binding: MainCoinItemBinding
            ) {
                (requireActivity() as MainActivity).launchInfoFragment(
                    topMover.coinPaprikaId,
                    topMover.symbol
                )
            }
        }).apply {
            addItems(topMovers)
        }
        binding.mainRecycler.adapter = mainAdapter
        // Add item decoration if needed
//        binding.mainRecycler.addItemDecoration(TopSpacingItemDecoration(0))
    }

    private fun setupDataFromViewModel() {
        viewModel.loadTopMovers()
        viewModel.topMovers
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { dto ->
                    topMovers = dto
                    binding.mainRecycler.scheduleLayoutAnimation()
                },
                {
                    Log.d("MainFragment", "Error getting data from CoinPaparikaTop10Movers", it)
                })
            .addTo(autoDisposable)
    }


}