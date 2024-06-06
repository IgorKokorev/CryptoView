package dev.kokorev.cryptoview.views.fragments

import android.icu.text.DecimalFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.data.sharedPreferences.FAVORITE_CHECK_MAX_CHANGE
import dev.kokorev.cryptoview.data.sharedPreferences.FAVORITE_CHECK_MIN_CHANGE
import dev.kokorev.cryptoview.data.sharedPreferences.MIN_MCAPS
import dev.kokorev.cryptoview.data.sharedPreferences.MIN_VOLS
import dev.kokorev.cryptoview.data.sharedPreferences.PORTFOLIO_NOTIFICATION_TIME_MAX
import dev.kokorev.cryptoview.data.sharedPreferences.PORTFOLIO_NOTIFICATION_TIME_MIN
import dev.kokorev.cryptoview.data.sharedPreferences.TOP_COINS_FROM
import dev.kokorev.cryptoview.data.sharedPreferences.TOP_COINS_TO
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesBoolean
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesFloat
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesInt
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesLong
import dev.kokorev.cryptoview.databinding.FragmentSettingsBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.SettingsViewModel
import dev.kokorev.cryptoview.views.MainActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class SettingsFragment : Fragment() {
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var binding: FragmentSettingsBinding
    private var autoDisposable = AutoDisposable()

    private var minMcapIndex: Int = 0
    private var minVolIndex: Int = 0
    private var minMcap: Long by preferencesLong("minMcap")
    private var minVol: Long by preferencesLong("minVol")
    private var nTopCoins: Int by preferencesInt("nTopCoins")
    private var toCheckFavorites: Boolean by preferencesBoolean("toCheckFavorites")
    private var favoriteChange: Float by preferencesFloat("favoriteChange")
    private var toNotifyPortfolio: Boolean by preferencesBoolean("toNotifyPortfolio")
    private var portfolioNotificationTime: Float by preferencesFloat("portfolioNotificationTime")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setMinMCapSelector()
        setMinVolSelector()
        setNumTopCoinsSelector()
        setFavoriteCoinsNotificationSelector()
        setPortfolioNotificationSelector()

        return binding.root
    }
    
    private fun setPortfolioNotificationSelector() {
        binding.portfolioCheckbox.isChecked = toNotifyPortfolio
        binding.portfolioTimeSlider.valueFrom = PORTFOLIO_NOTIFICATION_TIME_MIN
        binding.portfolioTimeSlider.valueTo = PORTFOLIO_NOTIFICATION_TIME_MAX
        binding.portfolioTimeSlider.value = portfolioNotificationTime
        binding.portfolioTimeSlider.isEnabled = toNotifyPortfolio
        setPortfolioNotificationTimeText()
        binding.portfolioTimeSlider.addOnChangeListener { _, value, _ ->
            portfolioNotificationTime = value
            setPortfolioNotificationTimeText()
        }
        binding.portfolioCheckbox.setOnCheckedChangeListener { _, isChecked ->
            // if user wants to get notification and the permission isn't granted
            toNotifyPortfolio = isChecked
            binding.portfolioTimeSlider.isEnabled = toNotifyPortfolio
        }
    }
    
    private fun setPortfolioNotificationTimeText() {
        val hour = portfolioNotificationTime.toInt()
        val minute = ((portfolioNotificationTime - hour.toFloat()) * 60).toInt()
        val str = DecimalFormat("00").format(hour) + ":" + DecimalFormat("00").format(minute)
        binding.portfolioTimeText.text = str
    }
    
    private fun setFavoriteCoinsNotificationSelector() {
        binding.favoriteChangeCheckbox.isChecked = toCheckFavorites
        
        App.instance.notificationPermission
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.favoriteChangeCheckbox.isChecked = it
            }
            .addTo(autoDisposable)
        
        binding.favoriteChangeValueSlider.valueFrom = FAVORITE_CHECK_MIN_CHANGE
        binding.favoriteChangeValueSlider.valueTo = FAVORITE_CHECK_MAX_CHANGE
        binding.favoriteChangeValueSlider.value = favoriteChange
        binding.favoriteChangeValueSlider.isEnabled = toCheckFavorites
        setFavoriteChangeValueText()
        
        
        binding.favoriteChangeValueSlider.addOnChangeListener { _, value, _ ->
            favoriteChange = value
            setFavoriteChangeValueText()
        }
        
        binding.favoriteChangeCheckbox.setOnCheckedChangeListener { _, isChecked ->
            // if user wants to get notification and the permission isn't granted
            toCheckFavorites = isChecked
            if (isChecked) (requireActivity() as MainActivity).askNotificationPermission()
            binding.favoriteChangeValueSlider.isEnabled = toCheckFavorites
        }
    }
    
    private fun setNumTopCoinsSelector() {
        binding.numTopCoinsSlider.valueFrom = TOP_COINS_FROM.toFloat()
        binding.numTopCoinsSlider.valueTo = TOP_COINS_TO.toFloat()
        binding.numTopCoinsSlider.value = nTopCoins.toFloat()
        setNumTopCoinsText()
        binding.numTopCoinsSlider.addOnChangeListener { _, value, _ ->
            nTopCoins = value.toInt()
            setNumTopCoinsText()
        }
    }
    
    private fun setMinVolSelector() {
        minVolIndex = MIN_VOLS.indexOf(minVol)
        binding.volSlider.value = minVolIndex.toFloat()
        binding.volSlider.valueFrom = 0.0f
        binding.volSlider.valueTo = (MIN_VOLS.size - 1).toFloat()
        setMinVolText()
        binding.volSlider.addOnChangeListener { _, value, _ ->
            minVolIndex = value.toInt()
            minVol = MIN_VOLS.get(minVolIndex)
            setMinVolText()
        }
    }
    
    // Min MCap to show coins selector settings
    private fun setMinMCapSelector() {
        minMcapIndex = MIN_MCAPS.indexOf(minMcap)
        binding.mcapSlider.value = minMcapIndex.toFloat()
        binding.mcapSlider.valueFrom = 0.0f
        binding.mcapSlider.valueTo = (MIN_MCAPS.size - 1).toFloat()
        setMinMcapText()
        binding.mcapSlider.addOnChangeListener { _, value, _ ->
            minMcapIndex = value.toInt()
            minMcap = MIN_MCAPS.get(minMcapIndex)
            setMinMcapText()
        }
    }
    
    private fun setFavoriteChangeValueText() {
        val str = NumbersUtils.formatWithPrecision(favoriteChange.toDouble(), 0) + "%"
        binding.favoriteChangeValueText.text = str
    }

    private fun setNumTopCoinsText() {
        binding.numTopCoinsText.text = nTopCoins.toString()
    }

    private fun setMinVolText() {
        binding.volText.text = NumbersUtils.formatBigNumberShort(minVol)
    }

    private fun setMinMcapText() {
        binding.mcapText.text = NumbersUtils.formatBigNumberShort(minMcap)
    }
}

