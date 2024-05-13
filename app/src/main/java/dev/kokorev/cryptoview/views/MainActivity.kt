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
import dev.kokorev.cryptoview.views.fragments.CoinFragment
import dev.kokorev.cryptoview.views.fragments.MainFragment
import dev.kokorev.cryptoview.views.fragments.SavedFragment
import dev.kokorev.cryptoview.views.fragments.SearchFragment
import dev.kokorev.cryptoview.views.fragments.SettingsFragment
import io.reactivex.rxjava3.schedulers.Schedulers

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
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        autoDisposable.bindTo(lifecycle)

        setupOnBackPressed()
        setupApp()
        initMenuButtons()

        addFragment(MainFragment(), Constants.MAIN_FRAGMENT_TAG)

    }

    private fun setupOnBackPressed() {
        val onBackPressedExitCallback = object : OnBackPressedCallback(enabled = false) {
            override fun handleOnBackPressed() {
                Log.d("MainActivity", "onBackPressedExitCallback")
                finish()
            }
        }

        val onBackPressedToastCallback = object : OnBackPressedCallback(enabled = false) {
            override fun handleOnBackPressed() {
                Toast.makeText(
                    binding.root.context,
                    getString(R.string.double_tap_toast),
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.d(
                    "MainActivity",
                    "onBackPressedToastCallback setting onBackPressedExitCallback.isEnabled = true"
                )
                onBackPressedExitCallback.isEnabled = true
                Handler(Looper.getMainLooper()).postDelayed({
                    Log.d(
                        "MainActivity",
                        "onBackPressedToastCallback setting onBackPressedExitCallback.isEnabled = false"
                    )
                    onBackPressedExitCallback.isEnabled = false
                }, Constants.BACK_CLICK_TIME_INTERVAL)
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            Log.d("MainActivity",
                "BackStackEntryCount = " + supportFragmentManager.backStackEntryCount + ". Fragments:")
            supportFragmentManager.fragments.forEach {
                Log.d("MainActivity", "Tag: " + it.tag + ", " + it.toString())
            }
            onBackPressedToastCallback.isEnabled = supportFragmentManager.backStackEntryCount <= 1
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedToastCallback)
        onBackPressedDispatcher.addCallback(this, onBackPressedExitCallback)
    }

    private fun initMenuButtons() {

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
    }

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
        val lastAppUpdateTime = viewModel.repository.getLastAppUpdateTime()
        val currentTime = System.currentTimeMillis()
        if (lastAppUpdateTime + Constants.APP_UPDATE_INTERVAL < currentTime) {
            Log.d(this.localClassName, "Setting Application data")
            viewModel.repository.setLastAppUpdateTime()
            updateBinanceInfo()
            updateCoinPaprikaTickers()
            updateCoinPaprikaAllCoins()
        }
    }

    private fun updateCoinPaprikaAllCoins() {
        viewModel.remoteApi.getCoinPaprikaAllCoins()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
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
        viewModel.remoteApi.getCoinPaprikaTickers()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                val tickers = it
                    .map { dto -> Converter.dtoToCoinPaprikaTicker(dto) }
                    .toList()
                viewModel.repository.addCoinPaprikaTickers(tickers)
            }
            .addTo(autoDisposable)
    }

    private fun updateBinanceInfo() {
        viewModel.remoteApi.getBinanceInfo()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
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