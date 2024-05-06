package dev.kokorev.cryptoview.views

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.coinpaprika.apiclient.entity.CoinType
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.ActivityMainBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.ConvertData
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.ActivityViewModel
import dev.kokorev.cryptoview.views.fragments.ChartFragment
import dev.kokorev.cryptoview.views.fragments.FavoritesFragment
import dev.kokorev.cryptoview.views.fragments.InfoFragment
import dev.kokorev.cryptoview.views.fragments.MainFragment
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

        autoDisposable.bindTo(lifecycle)

        // To check wtf is this
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupApp()
        initMenuButtons()
        replaceFragment(MainFragment())

    }

    private fun initMenuButtons() {

        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.home -> {
                    val tag = "home"
                    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: MainFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_placeholder, fragment, tag)
                        .addToBackStack(Constants.FRAGMENT_TAG)
                        .commit()
                    true
                }
                R.id.favorites -> {
                    val tag = "favorites"
                    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: FavoritesFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_placeholder, fragment, tag)
                        .addToBackStack(Constants.FRAGMENT_TAG)
                        .commit()
                    true
                }

                R.id.search -> {
                    val tag = "search"
                    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: SearchFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_placeholder, fragment, tag)
                        .addToBackStack(Constants.FRAGMENT_TAG)
                        .commit()
                    true
                }
                R.id.settings -> {
                    val tag = "settings"
                    val fragment =
                        supportFragmentManager.findFragmentByTag(tag) ?: SettingsFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_placeholder, fragment, tag)
                        .addToBackStack(Constants.FRAGMENT_TAG)
                        .commit()
                    Toast.makeText(this, R.string.settings_toast, Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }
    }

    fun launchInfoFragment(coinPaprikaId: String, symbol: String) {
        val bundle = Bundle()
        bundle.putString(Constants.ID, coinPaprikaId)
        bundle.putString(Constants.SYMBOL, symbol)
        val fragment = InfoFragment()
        fragment.arguments = bundle
        replaceFragment(fragment)
    }

    fun launchChartFragment(coinPaprikaId:  String, symbol: String) {
        val bundle = Bundle()
        bundle.putString(Constants.ID, coinPaprikaId)
        bundle.putString(Constants.SYMBOL, symbol)
        val fragment = ChartFragment()
        fragment.arguments = bundle
        replaceFragment(fragment)
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_placeholder, fragment)
            .addToBackStack(Constants.FRAGMENT_TAG)
            .commit()
    }

    fun setupApp() {
        viewModel.remoteApi.getBinanceInfo()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                val symbols = it.binanceSymbolDTOS.asSequence()
                    .map { dto -> ConvertData.dtoToBinanceSymbol(dto) }
                    .toList()
                viewModel.repository.addBinanceSymbols(symbols)
                Log.d("MainActivity", "SetupApp: ${symbols.size} symbols were read from API and added to db")
            }
            .addTo(autoDisposable)


        viewModel.remoteApi.getCoinPaprikaTickers()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                val tickers = it
                    .map { dto -> ConvertData.dtoToCoinPaprikaTicker(dto) }
                    .toList()
                viewModel.repository.addCoinPaprikaTickers(tickers)
            }
            .addTo(autoDisposable)

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
}