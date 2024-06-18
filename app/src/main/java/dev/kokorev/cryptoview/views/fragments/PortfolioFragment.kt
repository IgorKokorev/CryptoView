package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.coinpaprika.apiclient.entity.PortfolioPositionDB
import dev.kokorev.cryptoview.databinding.FragmentPortfolioBinding
import dev.kokorev.cryptoview.logd
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.NumbersUtils.formatPrice
import dev.kokorev.cryptoview.utils.NumbersUtils.setChangeView
import dev.kokorev.cryptoview.utils.PortfolioInteractor
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.SavedViewModel
import dev.kokorev.cryptoview.views.MainActivity
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
        portfolioInteractor = PortfolioInteractor(autoDisposable)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDataFromViewModel()
        initRecycler()
        binding.portfolioPerformance.setOnClickListener {
            (requireActivity() as MainActivity).launchPortfolioPerformanceFragment()
        }
    }

    private fun initRecycler() {
        portfolioAdapter = PortfolioAdapter(
            object : PortfolioAdapter.OnItemClickListener {
                override fun click(portfolioPositionDB: PortfolioPositionDB) { // On item click Coin fragment opens
                    portfolioInteractor.startChangingPosition(portfolioPositionDB)
                }
            }).apply {
            addItems(recyclerData)
        }
        binding.portfolioRecycler.adapter = portfolioAdapter
    }

    private fun setupDataFromViewModel() {
        // combine portfolio positions + latest tickers prices
        Observable.combineLatest(viewModel.portfolio, viewModel.tickers) { portfolio, tickers ->
            portfolio.forEach { portfolioCoin ->
                val cpTicker = tickers.find { ticker ->
                    ticker.coinPaprikaId == portfolioCoin.coinPaprikaId
                }
                if (cpTicker != null) {
                    portfolioCoin.priceLastEvaluation = cpTicker.price ?: 0.0
                    portfolioCoin.timeLastEvaluation = System.currentTimeMillis()
                    portfolioCoin.percentChange24hr = cpTicker.percentChange24h ?: 0.0
                }
            }
            portfolio
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ positions ->
                setGlobalData(positions)
                recyclerData = positions
            },
                {
                    logd("Error getting data", it)
                })
            .addTo(autoDisposable)
    }
    
    // calculate and set global portfolio data
    private fun setGlobalData(positions: List<PortfolioPositionDB>) {
        var value = 0.0 // portfolio value atm
        var value24hrBack = 0.0 // portfolio value 24 hours before
        var cost = 0.0 // cost of all the positions
        
        if (positions.isNotEmpty()) {
            value = positions
                .map { coin -> coin.quantity * coin.priceLastEvaluation }
                .reduce { acc, d -> acc + d }
            value24hrBack = positions
                .map { coin -> coin.quantity * coin.priceLastEvaluation * (1.0 - coin.percentChange24hr / 100.0) }
                .reduce { acc, d -> acc + d }
            cost = positions
                .map { coin -> coin.quantity * coin.priceOpen }
                .reduce { acc, d -> acc + d }
        }
        
        val valueStr = formatPrice(value) + "$"
        binding.totalValue.text = valueStr
        
        val pnl = value - cost
        setChangeView(binding.root.context, binding.totalPnl, pnl, "$")
        
        val percentChange = if (cost == 0.0) 0.0 else (pnl / cost) * 100.0
        setChangeView(
            binding.root.context,
            binding.totalPnlPercent,
            percentChange,
            "%"
        )
        
        val pnl24 = value - value24hrBack
        setChangeView(binding.root.context, binding.dailyPnl, pnl24, "$")
        
        val percentChange24 = if (value24hrBack == 0.0) 0.0 else (pnl24 / value24hrBack) * 100.0
        setChangeView(
            binding.root.context,
            binding.dailyPnlPercent,
            percentChange24,
            "%"
        )
    }
}