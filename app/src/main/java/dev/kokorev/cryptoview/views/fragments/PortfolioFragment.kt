package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.coinpaprika.apiclient.entity.PortfolioCoinDB
import dev.kokorev.cryptoview.databinding.FragmentPortfolioBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.NumbersUtils.setChange
import dev.kokorev.cryptoview.utils.NumbersUtils.setPrice
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.SavedViewModel
import dev.kokorev.cryptoview.views.MainActivity
import dev.kokorev.cryptoview.views.rvadapters.PortfolioAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class PortfolioFragment : Fragment() {
    private lateinit var binding: FragmentPortfolioBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: SavedViewModel by viewModels<SavedViewModel>(
        ownerProducer = { requireParentFragment() }
    )
    private lateinit var portfolioAdapter: PortfolioAdapter
    private var recyclerData: List<PortfolioCoinDB> = listOf()
        set(value) {
            if (field == value) return
            field = value
            portfolioAdapter.addItems(field)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPortfolioBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDataFromViewModel()
        initRecycler()
    }

    private fun initRecycler() {
        portfolioAdapter = PortfolioAdapter(
            object : PortfolioAdapter.OnItemClickListener {
                override fun click(portfolioCoinDB: PortfolioCoinDB) { // On item click Coin fragment opens
                    (requireActivity() as MainActivity).launchCoinFragment(
                        portfolioCoinDB.coinPaprikaId,
                        portfolioCoinDB.symbol,
                        portfolioCoinDB.name
                    )
                }
            }).apply {
            addItems(recyclerData)
        }
        binding.portfolioRecycler.adapter = portfolioAdapter
    }

    private fun setupDataFromViewModel() {
        Observable.zip(viewModel.portfolio, viewModel.tickers) { portfolio, tickers ->
            portfolio.forEach { portfolioCoin ->
                val cpTicker = tickers.find { ticker ->
                    ticker.coinPaprikaId == portfolioCoin.coinPaprikaId
                }
                if (cpTicker != null && cpTicker.price != null) {
                    portfolioCoin.priceLastEvaluation = cpTicker.price!!
                    portfolioCoin.timeLastEvaluation = System.currentTimeMillis()
                }
            }
            portfolio
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list ->
                val value = list
                    .map { coin -> coin.quantity * coin.priceLastEvaluation }
                    .reduce { acc, d -> acc + d }

                val cost = list
                    .map { coin -> coin.quantity * coin.priceOpen }
                    .reduce { acc, d -> acc + d }

                val valueStr = setPrice(value) + "$"
                binding.totalValue.text = valueStr
                setChange(value - cost, binding.root.context, binding.totalPnl, "$")

                recyclerData = list
            },
                {
                    Log.d(this.javaClass.simpleName, "Error getting data", it)
                })
            .addTo(autoDisposable)
    }
}