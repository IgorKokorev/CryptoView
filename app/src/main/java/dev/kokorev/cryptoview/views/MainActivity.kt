package dev.kokorev.cryptoview.views

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.data.Constants
import dev.kokorev.cryptoview.data.entity.FavoriteCoin
import dev.kokorev.cryptoview.databinding.ActivityMainBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.ActivityViewModel
import dev.kokorev.cryptoview.views.fragments.AiChatFragment
import dev.kokorev.cryptoview.views.fragments.CoinFragment
import dev.kokorev.cryptoview.views.fragments.MainFragment
import dev.kokorev.cryptoview.views.fragments.SavedFragment
import dev.kokorev.cryptoview.views.fragments.SearchFragment
import dev.kokorev.cryptoview.views.fragments.SettingsFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ActivityViewModel by viewModels()
    private val autoDisposable = AutoDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // To check wth is this
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set portrait orientation while landscape layouts are not ready yet
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        autoDisposable.bindTo(lifecycle)
        setInsets()
        setupOnBackPressed()
        initBottomBarButtons()
        setupProgressBar()

        // If notifications are disabled do not check favorites price change
        if(!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            viewModel.preferences.saveCheckFaforites(false)
        }

        addFragment(MainFragment(), Constants.MAIN_FRAGMENT_TAG)

        val coin = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(Constants.INTENT_EXTRA_FAVORITE_COIN, FavoriteCoin::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(Constants.INTENT_EXTRA_FAVORITE_COIN) as FavoriteCoin?
        }

        if (coin != null) {
            launchCoinFragment(coin.coinPaprikaId, coin.symbol, coin.name)
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

    fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(this.javaClass.simpleName, "Notification permission is already granted")
                viewModel.notificationService.notificationPermission.onNext(true)
            } else {
                Log.d(this.javaClass.simpleName, "Asking for notification permission")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    Constants.NOTIFICATION_PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            Log.d(this.javaClass.simpleName, "onRequestPermissionsResult. Number of permissions = ${permissions.size}, results: ${grantResults.size}")

            for (i in 0 until permissions.size) {
                val permission = permissions[i]
                val grantResult = grantResults[i]

                if (permission == Manifest.permission.POST_NOTIFICATIONS) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Log.d(this.javaClass.simpleName, "Notification permission granted")
                        viewModel.notificationService.notificationPermission.onNext(true)
                    } else {
                        Log.d(this.javaClass.simpleName, "Notification permission rejected")
                        viewModel.notificationService.notificationPermission.onNext(false)
                    }
                }
            }
        }
    }
}