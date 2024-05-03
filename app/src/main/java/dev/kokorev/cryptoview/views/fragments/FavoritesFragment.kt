package dev.kokorev.cryptoview.views.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.kokorev.cryptoview.viewModel.FavoritesViewModel
import dev.kokorev.cryptoview.databinding.FragmentFavoritesBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class FavoritesFragment : Fragment() {
    private lateinit var binding: FragmentFavoritesBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: FavoritesViewModel by viewModels()

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

//        setupDataFromViewModel("ETC")
    }

    private fun setupDataFromViewModel(symbol: String) {
        viewModel.remoteApi.getCmcMetadata(symbol)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { dto ->
                binding.text.text = "Symbol: $symbol\nDescription: " + dto.data.get(symbol)?.get(0)?.description
            }
            .addTo(autoDisposable)
    }
}