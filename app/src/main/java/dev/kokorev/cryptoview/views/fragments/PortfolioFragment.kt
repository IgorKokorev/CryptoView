package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.coinpaprika.apiclient.entity.PortfolioPositionDB
import dev.kokorev.cryptoview.databinding.FragmentPortfolioBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.NumbersUtils.formatPrice
import dev.kokorev.cryptoview.utils.NumbersUtils.setChangeView
import dev.kokorev.cryptoview.utils.PortfolioInteractor
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.SavedViewModel
import dev.kokorev.cryptoview.views.rvadapters.PortfolioAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class PortfolioFragment : Fragment() {
    private lateinit var binding: FragmentPortfolioBinding
    private lateinit var portfolioInteractor: PortfolioInteractor
    private val autoDisposable = AutoDisposable()
    private val viewModel: SavedViewModel by viewModels<SavedViewModel>(
        ownerProducer = { requireParentFragment() }
    )
    private lateinit var portfolioAdapter: PortfolioAdapter
    private var recyclerData: List<PortfolioPositionDB> = listOf()
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
    ): View {
        binding = FragmentPortfolioBinding.inflate(layoutInflater)
        portfolioInteractor = PortfolioInteractor(binding.root, autoDisposable)
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
                override fun click(portfolioPositionDB: PortfolioPositionDB) { // On item click Coin fragment opens
                    portfolioInteractor.changePosition(portfolioPositionDB)
                }
            }).apply {
            addItems(recyclerData)
        }
        binding.portfolioRecycler.adapter = portfolioAdapter
    }

    private fun setupDataFromViewModel() {
        Observable.combineLatest(viewModel.portfolio, viewModel.tickers) { portfolio, tickers ->
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
                
                val value = if (list.isNotEmpty()) {
                    list
                        .map { coin -> coin.quantity * coin.priceLastEvaluation }
                        .reduce { acc, d -> acc + d }
                } else 0.0

                val cost = if (list.isNotEmpty()) {
                    list
                        .map { coin -> coin.quantity * coin.priceOpen }
                        .reduce { acc, d -> acc + d }
                } else 0.0

                val valueStr = formatPrice(value) + "$"
                binding.totalValue.text = valueStr

                val pnl = value - cost
                setChangeView(pnl, binding.root.context, binding.totalPnl, "$")

                val percentChange = if (cost == 0.0) 0.0 else (pnl / cost) * 100.0
                setChangeView(
                    percentChange,
                    binding.root.context,
                    binding.totalPnlPercent,
                    "%"
                )

                recyclerData = list
            },
                {
                    Log.d(this.javaClass.simpleName, "Error getting data", it)
                })
            .addTo(autoDisposable)
    }
}