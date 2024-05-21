package dev.kokorev.cryptoview.views

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.coinpaprika.apiclient.entity.CoinType
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.ActivityMainBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.ActivityViewModel
import dev.kokorev.cryptoview.views.fragments.AiChatFragment
import dev.kokorev.cryptoview.views.fragments.CoinFragment
import dev.kokorev.cryptoview.views.fragments.MainFragment
import dev.kokorev.cryptoview.views.fragments.SavedFragment
import dev.kokorev.cryptoview.views.fragments.SearchFragment
import dev.kokorev.cryptoview.views.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ActivityViewModel by viewModels()
    private val autoDisposable = AutoDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // To check wth is this
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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



        autoDisposable.bindTo(lifecycle)

        setupOnBackPressed()
        setupApp()
//        initMenuButtons()
        initBottomBarButtons()
        setupProgressBar()

        addFragment(MainFragment(), Constants.MAIN_FRAGMENT_TAG)

    }

    private fun setupProgressBar() {
        viewModel.remoteApi.progressBarState
            .onErrorComplete()
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
                    Log.d(
                        "MainActivity",
                        "onBackPressedToastCallback setting onBackPressedExitCallback.isEnabled = false"
                    )
                    onBackPressedExitCallback.isEnabled = false
                }, Constants.BACK_CLICK_TIME_INTERVAL)
            }
        }

        // if only 1 fragment left in the backstack we enable the "Toast" callback
        supportFragmentManager.addOnBackStackChangedListener {
            onBackPressedToastCallback.isEnabled = supportFragmentManager.backStackEntryCount <= 1
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedToastCallback)
        onBackPressedDispatcher.addCallback(this, onBackPressedExitCallback)
    }

    /*    private fun initMenuButtons() {

            binding.topAppBar.setOnMenuItemClickListener {
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
                        Toast.makeText(this, R.string.settings_toast, Toast.LENGTH_SHORT).show()
                        true
                    }

                    else -> false
                }
            }
        }*/

    fun launchCoinFragment(coinPaprikaId: String, symbol: String, name: String) {
        val bundle = Bundle()
        bundle.putString(Constants.COIN_PAPRIKA_ID, coinPaprikaId)
        bundle.putString(Constants.COIN_SYMBOL, symbol)
        bundle.putString(Constants.COIN_NAME, name)
        val fragment = CoinFragment()
        fragment.arguments = bundle
        replaceFragment(fragment, Constants.COIN_FRAGMENT_TAG)
    }

    fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_placeholder, fragment)
            .addToBackStack(tag)
            .commit()
    }

    fun addFragment(fragment: Fragment, tag: String) {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_placeholder, fragment)
            .addToBackStack(tag)
            .commit()
    }

    fun setupApp() {
        val lastAppUpdateTime = viewModel.preferences.getLastAppUpdateTime()
        val currentTime = System.currentTimeMillis()
        if (lastAppUpdateTime + Constants.APP_UPDATE_INTERVAL < currentTime) {
            Log.d(this.localClassName, "Setting Application data")
            viewModel.preferences.saveLastAppUpdateTime()
            updateBinanceInfo()
            updateCoinPaprikaTickers()
            updateCoinPaprikaAllCoins()
        }
    }

    private fun updateCoinPaprikaAllCoins() {
        viewModel.remoteApi.getCoinPaprikaAllCoins()
            .subscribe {
                Log.d("MainActivity", "SetupApp: total CoinPaprika coins: ${it.size}")
                Log.d("MainActivity", "SetupApp: total CoinPaprika ranked coins: ${
                    it.filter { c -> c.rank > 0 }
                        .size
                }")
                Log.d("MainActivity", "SetupApp: total CoinPaprika active coins: ${
                    it.filter { c -> c.isActive }
                        .size
                }")
                Log.d("MainActivity", "SetupApp: total CoinPaprika ranked \"coins\": ${
                    it.filter { c -> c.rank > 0 && c.type == CoinType.Coin }
                        .size
                }")
                Log.d("MainActivity", "SetupApp: total CoinPaprika ranked \"tokens\": ${
                    it.filter { c -> c.rank > 0 && c.type == CoinType.Token }
                        .size
                }")
            }
            .addTo(autoDisposable)
    }

    fun updateCoinPaprikaTickers() {
        val lastAppUpdateTime = viewModel.preferences.getLastCpTickersCallTime()
        val currentTime = System.currentTimeMillis()
        if (lastAppUpdateTime + Constants.CP_TICKERS_UPDATE_INTERVAL < currentTime) {
            viewModel.remoteApi.getCoinPaprikaTickers()
                .subscribe { list ->
                    Log.d(
                        this.localClassName,
                        "Total number of CoinPaprika tickers received: ${list.size}"
                    )
                    val minMcap = Constants.minMCaps.get(0)
                    val minVol = Constants.minVols.get(0)
                    val tickers = list
                        .filter { ticker ->
                            val quote = ticker.quotes?.get("USD")
                            if (quote == null) false
                            else {
                                ticker.rank > 0 &&
                                        quote.marketCap >= minMcap &&
                                        quote.dailyVolume >= minVol
                            }
                        }
                        .map { dto -> Converter.dtoToCoinPaprikaTicker(dto) }
                        .toList()
                    Log.d(
                        this.localClassName,
                        "Total number of CoinPaprika tickers saved: ${tickers.size}"
                    )
                    viewModel.repository.addCoinPaprikaTickers(tickers)
                }
                .addTo(autoDisposable)
        }
    }

    private fun updateBinanceInfo() {
        Log.d(this.localClassName, "Start updating Binance data")
        viewModel.remoteApi.getBinanceInfo()
            .subscribe {
                val symbols = it.binanceSymbolDTOS.asSequence()
                    .map { dto -> Converter.dtoToBinanceSymbol(dto) }
                    .toList()
                viewModel.repository.addBinanceSymbols(symbols)
                Log.d(
                    "MainActivity",
                    "SetupApp: ${symbols.size} symbols were read from API and added to db"
                )
            }
            .addTo(autoDisposable)
    }
}