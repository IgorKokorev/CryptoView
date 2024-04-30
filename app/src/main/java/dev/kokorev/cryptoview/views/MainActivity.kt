package dev.kokorev.cryptoview.views

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dev.kokorev.cryptoview.views.fragments.ChartFragment
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.views.fragments.FavoritesFragment
import dev.kokorev.cryptoview.views.fragments.InfoFragment
import dev.kokorev.cryptoview.views.fragments.MainFragment
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.views.fragments.SettingsFragment
import dev.kokorev.cryptoview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // To check wth is this
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // To check wtf is this
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initMenuButtons()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_placeholder, MainFragment())
            .addToBackStack(Constants.FRAGMENT_TAG)
            .commit()
    }

    private fun initMenuButtons() {
//        binding.topAppBar.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.settings -> {
//                    val tag = "settings"
//                    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: SettingsFragment()
//                    supportFragmentManager
//                        .beginTransaction()
//                        .replace(R.id.fragment_placeholder, fragment, tag)
//                        .addToBackStack(Constants.FRAGMENT_TAG)
//                        .commit()
//                    Toast.makeText(this, R.string.settings_toast, Toast.LENGTH_SHORT).show()
//                    true
//                }
//
//                else -> false
//            }
//        }

        binding.bottomNavigation.setOnItemSelectedListener {
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
                R.id.info -> {
                    val tag = "info"
                    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: InfoFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_placeholder, fragment, tag)
                        .addToBackStack(Constants.FRAGMENT_TAG)
                        .commit()
                    true
                }
                R.id.chart -> {
                    val tag = "chart"
                    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: ChartFragment()
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

    fun launchInfoFragment(symbol: String) {
        val bundle = Bundle()
        bundle.putString(Constants.SYMBOL, symbol)
        val fragment = InfoFragment()
        fragment.arguments = bundle

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_placeholder, fragment)
            .addToBackStack(Constants.FRAGMENT_TAG)
            .commit()
    }
}