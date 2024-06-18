package dev.kokorev.cryptoview.views

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.coinpaprika.apiclient.entity.PortfolioEvaluationDB
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.COIN_ACTION
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.backgroundService.AlarmScheduler
import dev.kokorev.cryptoview.backgroundService.BinanceLoaderWorker
import dev.kokorev.cryptoview.backgroundService.TickersLoaderWorker
import dev.kokorev.cryptoview.data.entity.FavoriteCoin
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_FIRST_LAUNCH_INSTANT
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_PORTFOLIO_NOTIFICATION_TIME
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_TO_CHECK_FAVORITES
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_TO_NOTIFY_PORTFOLIO
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesBoolean
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesInstant
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesInt
import dev.kokorev.cryptoview.databinding.ActivityMainBinding
import dev.kokorev.cryptoview.logd
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.NumbersUtils.getPortfolioNotificationMillis
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.utils.toInstant
import dev.kokorev.cryptoview.utils.toLocalDate
import dev.kokorev.cryptoview.viewModel.ActivityViewModel
import dev.kokorev.cryptoview.views.fragments.AiChatFragment
import dev.kokorev.cryptoview.views.fragments.BinanceFragment
import dev.kokorev.cryptoview.views.fragments.CoinFragment
import dev.kokorev.cryptoview.views.fragments.MainFragment
import dev.kokorev.cryptoview.views.fragments.PortfolioPerformanceFragment
import dev.kokorev.cryptoview.views.fragments.SavedFragment
import dev.kokorev.cryptoview.views.fragments.SearchFragment
import dev.kokorev.cryptoview.views.fragments.SettingsFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.time.Instant
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ActivityViewModel by viewModels()
    private val autoDisposable = AutoDisposable()
    private var firstLaunchInstant by preferencesInstant(KEY_FIRST_LAUNCH_INSTANT)
    
    private var toCheckFavorites: Boolean by preferencesBoolean(KEY_TO_CHECK_FAVORITES)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // To check wth is this
        binding = ActivityMainBinding.inflate(layoutInflater)
        App.instance.activityBinding = binding
        autoDisposable.bindTo(lifecycle)
        setContentView(binding.root)

        // Set portrait orientation while landscape layouts are not ready yet
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        
        if (firstLaunchInstant == Instant.ofEpochMilli(0L)) setupFirstLaunch()
        
        setInsets()
        setupOnBackPressed()
        initBottomBarButtons()
        setupProgressBar()
        startScheduledTasks()
        startWorks()
        
        // If notifications are disabled do not check favorites price change
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            toCheckFavorites = false
        }
        
        addFragment(MainFragment(), Constants.MAIN_FRAGMENT_TAG)
        
        handleIntent()
    }
    
    // setup app when it started for the first time
    private fun setupFirstLaunch() {
        val now = Instant.now()
        
        viewModel.portfolioEvaluations
            .doOnSuccess { evaluations ->
                logd("setupFirstLaunch. Found ${evaluations.size} evaluations")
                if (evaluations.isEmpty()) createFirstEvaluation(now)
                else {
                    val firstDate = evaluations[0].date
                    logd("First date of saved evaluations = $firstDate")
                    firstLaunchInstant = firstDate.minusDays(1).toInstant()
                }
            }
            .doOnComplete {
                logd("setupFirstLaunch. No portfolio evaluations found")
                createFirstEvaluation(now)
            }
            .doOnError {
                logd("setupFirstLaunch. Error", it)
            }
            .onErrorComplete()
            .subscribe()
            .addTo(autoDisposable)
        
    }
    
    // creating the very first zero portfolio evaluation from yesterday
    private fun createFirstEvaluation(instant: Instant) {
        
        val date = instant.toLocalDate().minusDays(1)
        logd("createFirstEvaluation. creating the very first portfolio evaluation. Now is $instant, evaluation date is $date")
        
        val evaluation = PortfolioEvaluationDB(
            date = date,
            valuation = 0.0,
            inflow = 0.0,
            change = 0.0,
            percentChange = 0.0,
            cumulativeChange = 0.0,
            cumulativePercentChange = 0.0,
            positions = 0,
            pnl = 0.0
        )
        viewModel.savePortfolioEvaluation(evaluation)
        firstLaunchInstant = instant
    }
    
    private fun handleIntent() {
        if (intent == null || intent.action == null) return
        
        val action = intent.action!!
        if (action.startsWith(COIN_ACTION)) {
            val coin = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Constants.INTENT_EXTRA_FAVORITE_COIN, FavoriteCoin::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Constants.INTENT_EXTRA_FAVORITE_COIN) as FavoriteCoin?
            }
            
            if (coin != null) {
                launchCoinFragment(coin.coinPaprikaId, coin.symbol, coin.name, true)
            }
        }
    }
    
    private fun setInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            // System Bars' Insets
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            //  System Bars' and Keyboard's insets combined
            val systemBarsIMEInsets =
                insets.getInsets(WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.ime())
            // We use the combined bottom inset of the System Bars and Keyboard to move the view so it doesn't get covered up by the keyboard
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }
    
    private fun setupProgressBar() {
        viewModel.remoteApi.progressBarState
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturnItem(false)
            .subscribe {
                binding.progressBar.isVisible = it
            }
            .addTo(autoDisposable)
    }
    
    private fun initBottomBarButtons() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.main -> {
                    val tag = Constants.MAIN_FRAGMENT_TAG
                    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: MainFragment()
                    replaceFragment(fragment, tag)
                    true
                }
                
                R.id.favorites -> {
                    val tag = Constants.FAVORITES_FRAGMENT_TAG
                    val fragment =
                        supportFragmentManager.findFragmentByTag(tag) ?: SavedFragment()
                    replaceFragment(fragment, tag)
                    true
                }
                
                R.id.search -> {
                    val tag = Constants.SEARCH_FRAGMENT_TAG
                    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: SearchFragment()
                    replaceFragment(fragment, tag)
                    true
                }
                
                R.id.chat -> {
                    val tag = Constants.CHAT_FRAGMENT_TAG
                    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: AiChatFragment()
                    replaceFragment(fragment, tag)
                    true
                }
                
                R.id.settings -> {
                    val tag = Constants.SETTINGS_FRAGMENT_TAG
                    val fragment =
                        supportFragmentManager.findFragmentByTag(tag) ?: SettingsFragment()
                    replaceFragment(fragment, tag)
                    true
                }
                
                else -> false
            }
        }
    }
    
    private fun setupOnBackPressed() {
        // if enabled the callback finishes the app
        val onBackPressedExitCallback = object : OnBackPressedCallback(enabled = false) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        
        // if enabled the callback shows the toast and enables the exit callback for the defined in constants time
        val onBackPressedToastCallback = object : OnBackPressedCallback(enabled = false) {
            override fun handleOnBackPressed() {
                // Show the toast
                Toast.makeText(
                    binding.root.context,
                    getString(R.string.double_tap_toast),
                    Toast.LENGTH_SHORT
                )
                    .show()
                // Enable exit callback
                onBackPressedExitCallback.isEnabled = true
                // Disable it after the interval
                Handler(Looper.getMainLooper()).postDelayed({
                    onBackPressedExitCallback.isEnabled = false
                }, Constants.BACK_CLICK_TIME_MILLIS)
            }
        }
        
        // if only 1 fragment left in the backstack we enable the "Toast" callback
        supportFragmentManager.addOnBackStackChangedListener {
            onBackPressedToastCallback.isEnabled = supportFragmentManager.backStackEntryCount <= 1
        }
        
        onBackPressedDispatcher.addCallback(this, onBackPressedToastCallback)
        onBackPressedDispatcher.addCallback(this, onBackPressedExitCallback)
    }
    
    fun launchBinanceFragment(symbol: String) {
        val bundle = Bundle()
        bundle.putString(Constants.COIN_SYMBOL, symbol)
        val fragment = BinanceFragment()
        fragment.arguments = bundle
        replaceFragment(fragment, Constants.BINANCE_FRAGMENT_TAG)
    }
    
    fun launchCoinFragment(coinPaprikaId: String, symbol: String, name: String, toOpenChart: Boolean = false) {
        val bundle = Bundle()
        bundle.putString(Constants.COIN_PAPRIKA_ID, coinPaprikaId)
        bundle.putString(Constants.COIN_SYMBOL, symbol)
        bundle.putString(Constants.COIN_NAME, name)
        bundle.putBoolean(Constants.TO_OPEN_CHART, toOpenChart)
        val fragment = CoinFragment()
        fragment.arguments = bundle
        replaceFragment(fragment, Constants.COIN_FRAGMENT_TAG)
    }
    
    fun launchPortfolioPerformanceFragment() {
        replaceFragment(PortfolioPerformanceFragment(), Constants.COIN_FRAGMENT_TAG)
    }
    
    // weird but chaining warns about not committing transaction
    fun replaceFragment(fragment: Fragment, tag: String) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.fragment_placeholder, fragment)
        transaction.addToBackStack(tag)
        transaction.commit()
    }
    
    fun addFragment(fragment: Fragment, tag: String) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragment_placeholder, fragment)
        transaction.addToBackStack(tag)
        transaction.commit()
    }
    
    // check if app has notification permission and if not - ask permission
    fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                App.instance.notificationPermission.onNext(true)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    Constants.NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
    
    // handle results of permission requests
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            
            for (i in 0 until permissions.size) {
                val permission = permissions[i]
                val grantResult = grantResults[i]
                
                if (permission == Manifest.permission.POST_NOTIFICATIONS) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        App.instance.notificationPermission.onNext(true)
                    } else {
                        App.instance.notificationPermission.onNext(false)
                    }
                }
            }
        }
    }
    
    // on app start schedule all the background tasks
    private fun startScheduledTasks() {
        // start periodic portfolio evaluation task
        viewModel.alarmScheduler.schedule(AlarmScheduler.portfolioEvaluationData.apply {
            time = System.currentTimeMillis()
        })
        
        // if user selected to be notified about portfolio state - schedule notification
        val toNotifyPortfolio: Boolean by preferencesBoolean(KEY_TO_NOTIFY_PORTFOLIO)
        if (toNotifyPortfolio) {
            val portfolioNotificationTime: Int by preferencesInt(KEY_PORTFOLIO_NOTIFICATION_TIME)
            val notificationTime = getPortfolioNotificationMillis(portfolioNotificationTime)
            viewModel.alarmScheduler.schedule(AlarmScheduler.portfolioNotificationData.apply {
                time = notificationTime
            })
        }
    }
    
    // on app start run background works
    private fun startWorks() {
        val workManager = WorkManager.getInstance(this)
        
        // periodic work request to load CoinPaprika tickers and check Favorites
        val tickerLoaderWorkRequest =
            PeriodicWorkRequestBuilder<TickersLoaderWorker>(15, TimeUnit.MINUTES)
                .addTag(Constants.TICKER_LOADER_TAG)
                .build()
        workManager
            .enqueueUniquePeriodicWork(
                Constants.TICKER_LOADER_WORK,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                tickerLoaderWorkRequest
            )
        
        // periodic work request to download and save Binance symbols
        val binanceLoaderWorkRequest =
            PeriodicWorkRequestBuilder<BinanceLoaderWorker>(15, TimeUnit.MINUTES)
                .addTag(Constants.BINANCE_LOADER_TAG)
                .build()
        workManager
            .enqueueUniquePeriodicWork(
                Constants.BINANCE_LOADER_WORK,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                binanceLoaderWorkRequest
            )
    }
}