package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.transition.TransitionManager
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.data.Constants
import dev.kokorev.cryptoview.data.entity.GainerCoin
import dev.kokorev.cryptoview.databinding.FragmentMainBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.MainViewModel
import dev.kokorev.cryptoview.views.MainActivity
import dev.kokorev.cryptoview.views.rvadapters.TopMoverAdapter
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import dev.kokorev.token_metrics_api.entity.TMSentiment
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Main fragment which is started once app starts. Shows Global info about Crypto markets
 */
class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: MainViewModel by viewModels()
    private lateinit var topMoverAdapter: TopMoverAdapter
    private lateinit var sorting: TickerPriceSorting
    private var numTopCoins = Constants.TOP_COINS_DEFAULT
    private var sortingBS: BehaviorSubject<TickerPriceSorting> = BehaviorSubject.create()

    // Map TokenMetrics sentiment grades to colors
    val tmGradeToColor: Map<String, Int> = mapOf(
        "very negative" to ContextCompat.getColor(App.instance.applicationContext, R.color.red),
        "negative" to ContextCompat.getColor(App.instance.applicationContext, R.color.red),
        "neutral" to ContextCompat.getColor(App.instance.applicationContext, R.color.textColor),
        "positive" to ContextCompat.getColor(App.instance.applicationContext, R.color.green),
        "very positive" to ContextCompat.getColor(App.instance.applicationContext, R.color.green),
    )

    val defaultTextColor =
        ContextCompat.getColor(App.instance.applicationContext, R.color.textColor)

    // Top movers - list of 10 gainers and losers for the last 24 hours
    private var topMovers: List<GainerCoin> = listOf()
        set(value) {
            if (field == value) return
            field = value.sortedByDescending { it.percentChange }
            topMoverAdapter.addItems(field)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sorting = viewModel.preferences.getMainPriceSorting()
        numTopCoins = viewModel.preferences.getNumTopCoins()

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
        setupSentimentsClickListeners()

        sortingBS.subscribe {
            sorting = it
            getTickers()
            highlightIcon()
        }
            .addTo(autoDisposable)
        sortingBS.onNext(sorting)
    }

    private fun highlightIcon() {
        binding.sortingH1.background = ResourcesCompat.getDrawable(
            resources,
            if (sorting == TickerPriceSorting.H1) R.drawable.rounded_rectangle_accent
            else R.drawable.rounded_rectangle_base, null
        )
        binding.sortingD1.background = ResourcesCompat.getDrawable(
            resources,
            if (sorting == TickerPriceSorting.H24) R.drawable.rounded_rectangle_accent
            else R.drawable.rounded_rectangle_base, null
        )
        binding.sortingW1.background = ResourcesCompat.getDrawable(
            resources,
            if (sorting == TickerPriceSorting.D7) R.drawable.rounded_rectangle_accent
            else R.drawable.rounded_rectangle_base, null
        )
        binding.sortingM1.background = ResourcesCompat.getDrawable(
            resources,
            if (sorting == TickerPriceSorting.D30) R.drawable.rounded_rectangle_accent
            else R.drawable.rounded_rectangle_base, null
        )
        binding.sortingY1.background = ResourcesCompat.getDrawable(
            resources,
            if (sorting == TickerPriceSorting.Y1) R.drawable.rounded_rectangle_accent
            else R.drawable.rounded_rectangle_base, null
        )
        binding.sortingAth.background = ResourcesCompat.getDrawable(
            resources,
            if (sorting == TickerPriceSorting.ATH) R.drawable.rounded_rectangle_accent
            else R.drawable.rounded_rectangle_base, null
        )
    }

    override fun onPause() {
        super.onPause()
        viewModel.preferences.saveMainPriceSorting(sorting)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.preferences.saveMainPriceSorting(sorting)
    }

    private fun setIntervalListeners() {
        binding.sortingH1.setOnClickListener {
            sortingBS.onNext(TickerPriceSorting.H1)
        }
        binding.sortingD1.setOnClickListener {
            sortingBS.onNext(TickerPriceSorting.H24)
        }
        binding.sortingW1.setOnClickListener {
            sortingBS.onNext(TickerPriceSorting.D7)
        }
        binding.sortingM1.setOnClickListener {
            sortingBS.onNext(TickerPriceSorting.D30)
        }
        binding.sortingY1.setOnClickListener {
            sortingBS.onNext(TickerPriceSorting.Y1)
        }
        binding.sortingAth.setOnClickListener {
            sortingBS.onNext(TickerPriceSorting.ATH)
        }
    }

    override fun onResume() {
        super.onResume()
        setupDataFromViewModel()
    }

    private fun initRecycler() {
        topMoverAdapter = TopMoverAdapter(object : TopMoverAdapter.OnItemClickListener {
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
        binding.mainRecycler.adapter = topMoverAdapter
    }

    private fun setupSentimentsClickListeners() {
        binding.newsContainer.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.newsContainer)
            binding.newsText.visibility =
                if (binding.newsText.visibility == View.GONE) View.VISIBLE
                else View.GONE
        }

        binding.redditContainer.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.redditContainer)
            binding.redditText.visibility =
                if (binding.redditText.visibility == View.GONE) View.VISIBLE
                else View.GONE
        }

        binding.twitterContainer.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.twitterContainer)
            binding.twitterText.visibility =
                if (binding.twitterText.visibility == View.GONE) View.VISIBLE
                else View.GONE
        }
    }

    private fun setupDataFromViewModel() {
        getSentiment()
        getTickers()
    }

    private fun getTickers() {
        viewModel.cpTickers
            .subscribe { list ->
                val gainers = findGainers(list)
                val losers = findLosers(list)
                topMovers = gainers + losers
            }
            .addTo(autoDisposable)
    }

    private fun getSentiment() {
        val time = LocalDateTime.now(ZoneOffset.UTC)
        Log.d(this.javaClass.simpleName, "getSentiment. Time: ${time}")
        if (time > viewModel.preferences.getTMSentimentLastCall().plusHours(1)) {
            viewModel.remoteApi.getSentiment()
                .subscribe {
                    if (it.success && !it.data.isNullOrEmpty()) {
                        val data = it.data.get(0)
                        viewModel.cacheManager.saveTMSentiment(data)
                        viewModel.preferences.saveTMSentimentLastCall(time)
                        setViewData(data)
                    }
                }
                .addTo(autoDisposable)
        } else {
            val data = viewModel.cacheManager.getTMSentiment()
            if (data == null) {
                viewModel.preferences.saveTMSentimentLastCall(time.withYear(time.year - 1))
            } else {
                setViewData(data)
            }
        }
    }

    private fun setViewData(data: TMSentiment) {

        // DateTime of the sentiment
        binding.sentimentDate.text = data.dateTime?.take(16)

        // Market sentiment grade
        binding.marketSentimentGrade.text = data.marketSentimentGrade.toString()
        binding.marketSentimentGrade.setTextColor(
            tmGradeToColor.get(data.marketSentimentLabel) ?: defaultTextColor
        )
        binding.marketSentimentLabel.text = data.marketSentimentLabel
        binding.marketSentimentLabel.setTextColor(
            tmGradeToColor.get(data.marketSentimentLabel) ?: defaultTextColor
        )

        // News sentiment grade
        binding.newsSentimentGrade.text = data.newsSentimentGrade.toString()
        binding.newsSentimentGrade.setTextColor(
            tmGradeToColor.get(data.newsSentimentLabel) ?: defaultTextColor
        )
        binding.newsSentimentLabel.text = data.newsSentimentLabel
        binding.newsSentimentLabel.setTextColor(
            tmGradeToColor.get(data.newsSentimentLabel) ?: defaultTextColor
        )
        binding.newsText.text = data.newsSummary

        // News sentiment grade
        binding.redditSentimentGrade.text = data.redditSentimentGrade.toString()
        binding.redditSentimentGrade.setTextColor(
            tmGradeToColor.get(data.redditSentimentLabel) ?: defaultTextColor
        )
        binding.redditSentimentLabel.text = data.redditSentimentLabel
        binding.redditSentimentLabel.setTextColor(
            tmGradeToColor.get(data.redditSentimentLabel) ?: defaultTextColor
        )
        binding.redditText.text = data.redditSummary

        // News sentiment grade
        binding.twitterSentimentGrade.text = data.twitterSentimentGrade.toString()
        binding.twitterSentimentGrade.setTextColor(
            tmGradeToColor.get(data.twitterSentimentLabel) ?: defaultTextColor
        )
        binding.twitterSentimentLabel.text = data.twitterSentimentLabel
        binding.twitterSentimentLabel.setTextColor(
            tmGradeToColor.get(data.twitterSentimentLabel) ?: defaultTextColor
        )
        binding.twitterText.text = data.twitterSummary
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
            .take(numTopCoins)
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
            .take(numTopCoins)
            .map { ticker -> Converter.cpTickerDBToGainerCoin(ticker, sorting) }
            .filter { coin -> sorting == TickerPriceSorting.ATH || coin.percentChange!! > 0 }

}

