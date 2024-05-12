package dev.kokorev.cryptoview.views.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coinpaprika.apiclient.entity.FavoriteCoin
import dev.kokorev.cryptoview.databinding.FavoriteCoinItemBinding
import dev.kokorev.cryptoview.viewModel.FavoritesViewModel
import dev.kokorev.cryptoview.databinding.FragmentFavoritesBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.views.MainActivity
import dev.kokorev.cryptoview.views.rvadapters.FavoriteAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class FavoritesFragment : Fragment() {
    private lateinit var binding: FragmentFavoritesBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: FavoritesViewModel by viewModels()
    private lateinit var favoriteAdapter: FavoriteAdapter
    private var recyclerData: List<FavoriteCoin> = listOf()
        set(value) {
            if (field == value) return
            field = value
            favoriteAdapter.addItems(field)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDataFromViewModel()
        initRecycler()
    }
    private fun initRecycler() {
        favoriteAdapter = FavoriteAdapter(object : FavoriteAdapter.OnItemClickListener {
            override fun click(
                coin: FavoriteCoin,
                position: Int,
                binding: FavoriteCoinItemBinding
            ) { // On item click Coin fragment opens
                (requireActivity() as MainActivity).launchCoinFragment(
                    coin.coinPaprikaId,
                    coin.symbol,
                    coin.name
                )
            }
        }).apply {
            addItems(recyclerData)
        }
        binding.favoriteRecycler.adapter = favoriteAdapter
    }

    private fun setupDataFromViewModel() {
        viewModel.favorites
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { dto ->
                    recyclerData = dto
                    binding.favoriteRecycler.scheduleLayoutAnimation()
                },
                {
                    Log.d("MainFragment", "Error getting data from CoinPaparikaTop10Movers", it)
                })
            .addTo(autoDisposable)
    }
}