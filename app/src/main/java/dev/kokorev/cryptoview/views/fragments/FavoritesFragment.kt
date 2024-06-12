package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.kokorev.cryptoview.data.entity.FavoriteCoin
import dev.kokorev.cryptoview.data.entity.SavedCoin
import dev.kokorev.cryptoview.databinding.FragmentFavoritesBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.SavedViewModel
import dev.kokorev.cryptoview.views.MainActivity
import dev.kokorev.cryptoview.views.rvadapters.SavedAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class FavoritesFragment : Fragment() {
    private lateinit var binding: FragmentFavoritesBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: SavedViewModel by viewModels<SavedViewModel>(
        ownerProducer = { requireParentFragment() }
    )
    private lateinit var savedAdapter: SavedAdapter
    private var recyclerData: List<FavoriteCoin> = listOf()
        set(value) {
            if (field == value) return
            field = value
            savedAdapter.addItems(field)
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
    
    override fun onResume() {
        super.onResume()
    }
    
    private fun initRecycler() {
        savedAdapter = SavedAdapter(
            object : SavedAdapter.OnItemClickListener {
                override fun click(savedCoin: SavedCoin) { // On item click Coin fragment opens
                    (requireActivity() as MainActivity).launchCoinFragment(
                        savedCoin.coinPaprikaId,
                        savedCoin.symbol,
                        savedCoin.name
                    )
                }
            }).apply {
            addItems(recyclerData)
        }
        binding.favoriteRecycler.adapter = savedAdapter
    }

    private fun setupDataFromViewModel() {
        Observable.zip(viewModel.favorites, viewModel.tickers) { favorites, tikers ->
            val ids = favorites.map { favorite -> favorite.coinPaprikaId }
            val filtered = tikers.filter { db -> ids.contains(db.coinPaprikaId) }
            favorites.map { db -> Converter.favoriteCoinDBToFavoriteCoin(db, filtered) }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                recyclerData = it
            }
            .addTo(autoDisposable)
    }
}