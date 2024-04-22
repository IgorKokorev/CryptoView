package dev.kokorev.cryptoview.views.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.kokorev.cryptoview.viewModel.MainViewModel
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.FragmentMainBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private val autoDisposable = AutoDisposable()

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModels<MainViewModel>()

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
    }

    private fun setupDataFromViewModel() {
        viewModel.interactor.getBinanceCurrentAvgPrice("BTCUSDT")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { dto -> binding.text.text = "Symbol: BTCUSDT\nPrice: " + dto.price }
            .addTo(autoDisposable)
    }
}