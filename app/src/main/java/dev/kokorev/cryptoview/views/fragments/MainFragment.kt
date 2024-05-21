package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.kokorev.cryptoview.data.entity.GainerCoin
import dev.kokorev.cryptoview.databinding.FragmentMainBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.MainViewModel
import dev.kokorev.cryptoview.views.MainActivity
import dev.kokorev.cryptoview.views.rvadapters.GainerAdapter
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB

/**
 * Main fragment which is started once app starts. Shows Global info about Crypto markets
 */
class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: MainViewModel by viewModels()
    private lateinit var gainerAdapter: GainerAdapter
    private lateinit var sorting: TickerPriceSorting
    private var gainersLimit = 10

    // Top movers - list of 10 gainers and losers for the last 24 hours
    private var topMovers: List<GainerCoin> = listOf()
        set(value) {
            if (field == value) return
            field = value.sortedByDescending { it.percentChange }
            gainerAdapter.addItems(field)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sorting = viewModel.repository.getMainPriceSorting()
        gainersLimit = viewModel.preferences.getNumGainers()

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
        initRecycler()
        setIntervalListeners()
    }

    override fun onPause() {
        super.onPause()
        viewModel.preferences.saveMainPriceSorting(sorting)
    }
    private fun setIntervalListeners() {
        binding.sortingH1.setOnClickListener {
            sorting = TickerPriceSorting.H1
            setupDataFromViewModel()
        }
        binding.sortingD1.setOnClickListener {
            sorting = TickerPriceSorting.H24
            setupDataFromViewModel()
        }
        binding.sortingW1.setOnClickListener {
            sorting = TickerPriceSorting.D7
            setupDataFromViewModel()
        }
        binding.sortingM1.setOnClickListener {
            sorting = TickerPriceSorting.D30
            setupDataFromViewModel()
        }
        binding.sortingY1.setOnClickListener {
            sorting = TickerPriceSorting.Y1
            setupDataFromViewModel()
        }
        binding.sortingAth.setOnClickListener {
            sorting = TickerPriceSorting.ATH
            setupDataFromViewModel()
        }
    }

    override fun onResume() {
        super.onResume()
        setupDataFromViewModel()
    }

    private fun initRecycler() {
        gainerAdapter = GainerAdapter(object : GainerAdapter.OnItemClickListener {
            override fun click(gainerCoin: GainerCoin) {
                // On item click Coin fragment opens
                (requireActivity() as MainActivity).launchCoinFragment(
                    gainerCoin.coinPaprikaId,
                    gainerCoin.symbol,
                    gainerCoin.name
                )
            }
        }).apply {
            addItems(topMovers)
        }
        binding.mainRecycler.adapter = gainerAdapter
    }

    private fun setupDataFromViewModel() {
        /*        Observable.zip(
                    viewModel.gainers,
                    viewModel.losers,
                    BiFunction { t1, t2 ->
                        val gainerCoins = t1.map { ticker -> Converter.cpTickerDBToGainerCoin(ticker) }
                            .filter { coin -> coin.percentChange!! > 0 }
                        val loserCoins = t2.map { ticker -> Converter.cpTickerDBToGainerCoin(ticker) }
                            .filter { coin -> coin.percentChange!! < 0 }
                        gainerCoins + loserCoins
                    }
                )
                    .subscribe {  list ->
                        topMovers = list
                    }
                    .addTo(autoDisposable)*/

        viewModel.cpTickers
            .subscribe { list ->
                val gainers = findGainers(list)
                val losers = findLosers(list)
                topMovers = gainers + losers
            }
            .addTo(autoDisposable)
    }

    private fun findLosers(list: List<CoinPaprikaTickerDB>) =
        list
            .sortedBy { ticker ->
                when (sorting) {
                    TickerPriceSorting.H1 -> ticker.percentChange1h
                    TickerPriceSorting.H24 -> ticker.percentChange24h
                    TickerPriceSorting.D7 -> ticker.percentChange7d
                    TickerPriceSorting.D30 -> ticker.percentChange30d
                    TickerPriceSorting.Y1 -> ticker.percentChange1y
                    TickerPriceSorting.ATH -> ticker.percentFromPriceAth
                }
            }
            .take(gainersLimit)
            .map { ticker -> Converter.cpTickerDBToGainerCoin(ticker, sorting) }
            .filter { coin -> coin.percentChange!! < 0 }
            .reversed()

    private fun findGainers(list: List<CoinPaprikaTickerDB>) =
        list
            .sortedByDescending { ticker ->
                when (sorting) {
                    TickerPriceSorting.H1 -> ticker.percentChange1h
                    TickerPriceSorting.H24 -> ticker.percentChange24h
                    TickerPriceSorting.D7 -> ticker.percentChange7d
                    TickerPriceSorting.D30 -> ticker.percentChange30d
                    TickerPriceSorting.Y1 -> ticker.percentChange1y
                    TickerPriceSorting.ATH -> ticker.percentFromPriceAth
                }
            }
            .take(gainersLimit)
            .map { ticker -> Converter.cpTickerDBToGainerCoin(ticker, sorting) }
            .filter { coin -> sorting == TickerPriceSorting.ATH || coin.percentChange!! > 0 }
}

