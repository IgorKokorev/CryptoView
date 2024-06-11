package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
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
import dev.kokorev.cryptoview.data.entity.GainerCoin
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_MAIN_PRICE_SORTING
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_NUM_TOP_COINS
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_TM_SENTIMENT_CALL_TIME
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesInstant
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesInt
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesMainPriceSorting
import dev.kokorev.cryptoview.databinding.FragmentMainBinding
import dev.kokorev.cryptoview.logd
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.MainViewModel
import dev.kokorev.cryptoview.views.MainActivity
import dev.kokorev.cryptoview.views.rvadapters.TopMoverAdapter
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import dev.kokorev.token_metrics_api.entity.TMSentiment
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset


/**
 * Main fragment which is started once app starts. Shows Global info about Crypto markets
 */
class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: MainViewModel by viewModels()
    private lateinit var topMoverAdapter: TopMoverAdapter
    private var sortingBS: BehaviorSubject<MainPriceSorting> = BehaviorSubject.create()
    
    private var nTopCoins: Int by preferencesInt(KEY_NUM_TOP_COINS)
    private var sorting: MainPriceSorting by preferencesMainPriceSorting(KEY_MAIN_PRICE_SORTING)
    private var tmSentimentTime: Instant by preferencesInstant(KEY_TM_SENTIMENT_CALL_TIME)

    // Map TokenMetrics sentiment grades to colors
    private val tmGradeToColor: Map<String, Int> = mapOf(
        "very negative" to ContextCompat.getColor(App.instance.applicationContext, R.color.red),
        "negative" to ContextCompat.getColor(App.instance.applicationContext, R.color.redFaded),
        "neutral" to ContextCompat.getColor(App.instance.applicationContext, R.color.textColor),
        "positive" to ContextCompat.getColor(App.instance.applicationContext, R.color.greenFaded),
        "very positive" to ContextCompat.getColor(App.instance.applicationContext, R.color.green),
    )

    private val defaultTextColor =
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
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater)
        
        initRecycler()
        setIntervalListeners()
        setupSentimentsClickListeners()
        
        sortingBS.subscribe {
            sorting = it
            getTopMovers()
            highlightIcon()
        }
            .addTo(autoDisposable)
        
        sortingBS.onNext(sorting)
        
        return binding.root
    }

    // highlight active sorting button
    private fun highlightIcon() {
        binding.sortingH1.background = ResourcesCompat.getDrawable(
            resources,
            if (sorting == MainPriceSorting.H1) R.drawable.rounded_rectangle_accent
            else R.drawable.rounded_rectangle_base, null
        )
        binding.sortingD1.background = ResourcesCompat.getDrawable(
            resources,
            if (sorting == MainPriceSorting.H24) R.drawable.rounded_rectangle_accent
            else R.drawable.rounded_rectangle_base, null
        )
        binding.sortingW1.background = ResourcesCompat.getDrawable(
            resources,
            if (sorting == MainPriceSorting.D7) R.drawable.rounded_rectangle_accent
            else R.drawable.rounded_rectangle_base, null
        )
        binding.sortingM1.background = ResourcesCompat.getDrawable(
            resources,
            if (sorting == MainPriceSorting.D30) R.drawable.rounded_rectangle_accent
            else R.drawable.rounded_rectangle_base, null
        )
        binding.sortingY1.background = ResourcesCompat.getDrawable(
            resources,
            if (sorting == MainPriceSorting.Y1) R.drawable.rounded_rectangle_accent
            else R.drawable.rounded_rectangle_base, null
        )
        binding.sortingAth.background = ResourcesCompat.getDrawable(
            resources,
            if (sorting == MainPriceSorting.ATH) R.drawable.rounded_rectangle_accent
            else R.drawable.rounded_rectangle_base, null
        )
    }

    private fun setIntervalListeners() {
        binding.sortingH1.setOnClickListener {
            sortingBS.onNext(MainPriceSorting.H1)
        }
        binding.sortingD1.setOnClickListener {
            sortingBS.onNext(MainPriceSorting.H24)
        }
        binding.sortingW1.setOnClickListener {
            sortingBS.onNext(MainPriceSorting.D7)
        }
        binding.sortingM1.setOnClickListener {
            sortingBS.onNext(MainPriceSorting.D30)
        }
        binding.sortingY1.setOnClickListener {
            sortingBS.onNext(MainPriceSorting.Y1)
        }
        binding.sortingAth.setOnClickListener {
            sortingBS.onNext(MainPriceSorting.ATH)
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

    // Expand sentiment text on click
    private fun setupSentimentsClickListeners() {
        binding.newsContainer.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.newsContainer)
            binding.newsText.visibility =
                if (binding.newsText.visibility == View.GONE) View.VISIBLE
                else View.GONE
            binding.redditText.visibility = View.GONE
            binding.twitterText.visibility = View.GONE
        }

        binding.redditContainer.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.redditContainer)
            binding.redditText.visibility =
                if (binding.redditText.visibility == View.GONE) View.VISIBLE
                else View.GONE
            binding.newsText.visibility = View.GONE
            binding.twitterText.visibility = View.GONE
        }

        binding.twitterContainer.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.twitterContainer)
            binding.twitterText.visibility =
                if (binding.twitterText.visibility == View.GONE) View.VISIBLE
                else View.GONE
            binding.newsText.visibility = View.GONE
            binding.redditText.visibility = View.GONE
        }
    }

    private fun setupDataFromViewModel() {
        getSentiment()
        getTopMovers()
    }

    // Get top movers for RV
    private fun getTopMovers() {
        viewModel.cpTickers
            .subscribe { list ->
                val gainers = findGainers(list)
                val losers = findLosers(list)
                topMovers = gainers + losers
            }
            .addTo(autoDisposable)
    }

    // Get Sentiment from API if more than 1 hour have passed since the last saved sentiment. Othewise get it from cache
    private fun getSentiment() {
        val time = Instant.now()
        if (time.isAfter(tmSentimentTime.plusSeconds( 67 * 60 /* 67 minutes */))) {
            viewModel.getSentimentFromApi()
                .doOnSuccess {
                    logd("getSentiment success")
                    if (it.success && it.data.isNotEmpty()) {
                        val data = it.data.get(0)
                        viewModel.cacheTMSentiment(data)
                        setSentimentData(data)
                    }
                }
                .doOnComplete {
                    logd("getSentiment complete")
                    setSentimentFromCache()
                }
                .doOnError {
                    logd("getSentiment error", it)
                    setSentimentFromCache()
                }
                .subscribe()
                .addTo(autoDisposable)
        } else {
            setSentimentFromCache()
        }
    }
    
    private fun setSentimentFromCache() {
        viewModel.getCachedTMSentiment()
            .doOnSuccess {
                logd("setSentimentFromCache success")
                setSentimentData(it)
            }
            .doOnComplete {
                logd("setSentimentFromCache complete")
                tmSentimentTime = Instant.now().minusSeconds(120 * 60 /* couple of hours back */)
            }
            .doOnError {
                logd("setSentimentFromCache error")
                tmSentimentTime = Instant.now().minusSeconds(120 * 60 /* couple of hours back */)
            }
            .onErrorComplete()
            .subscribe()
            .addTo(autoDisposable)
    }
    
    // Show sentiment data
    private fun setSentimentData(data: TMSentiment) {
        // DateTime of the sentiment
        if (data.dateTime != null) setDateTime(data.dateTime!!)

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
    
    // Show last sentiment date and time and save last saved sentiment time if needed
    private fun setDateTime(timeString: String) {
        // Time in Sentiment
        val ldt = LocalDateTime.parse(timeString.replace(' ', 'T'))
        
        val instant = ldt.toInstant(ZoneOffset.UTC)
        if (instant.isAfter(tmSentimentTime)) {
            tmSentimentTime = instant
        }
        
        // converting UTC to local time
        val localTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val localTimeStr = localTime.toString().replace('T', ' ')
        
        binding.sentimentDate.text = localTimeStr
    }
    
    private fun findLosers(list: List<CoinPaprikaTickerDB>) =
        list
            .sortedBy { ticker ->
                when (sorting) {
                    MainPriceSorting.H1 -> ticker.percentChange1h
                    MainPriceSorting.H24 -> ticker.percentChange24h
                    MainPriceSorting.D7 -> ticker.percentChange7d
                    MainPriceSorting.D30 -> ticker.percentChange30d
                    MainPriceSorting.Y1 -> ticker.percentChange1y
                    MainPriceSorting.ATH -> ticker.percentFromPriceAth
                }
            }
            .take(nTopCoins)
            .map { ticker -> Converter.cpTickerDBToGainerCoin(ticker, sorting) }
            .filter { coin -> coin.percentChange!! < 0 }
            .reversed()

    private fun findGainers(list: List<CoinPaprikaTickerDB>) =
        list
            .sortedByDescending { ticker ->
                when (sorting) {
                    MainPriceSorting.H1 -> ticker.percentChange1h
                    MainPriceSorting.H24 -> ticker.percentChange24h
                    MainPriceSorting.D7 -> ticker.percentChange7d
                    MainPriceSorting.D30 -> ticker.percentChange30d
                    MainPriceSorting.Y1 -> ticker.percentChange1y
                    MainPriceSorting.ATH -> ticker.percentFromPriceAth
                }
            }
            .take(nTopCoins)
            .map { ticker -> Converter.cpTickerDBToGainerCoin(ticker, sorting) }
            .filter { coin -> sorting == MainPriceSorting.ATH || coin.percentChange!! > 0 }

}

