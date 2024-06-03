package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.data.preferencesBoolean
import dev.kokorev.cryptoview.data.preferencesFloat
import dev.kokorev.cryptoview.data.preferencesInt
import dev.kokorev.cryptoview.data.preferencesLong
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        minMcapIndex = Constants.minMCaps.indexOf(minMcap)
        binding.mcapSlider.value = minMcapIndex.toFloat()
        binding.mcapSlider.valueFrom = 0.0f
        binding.mcapSlider.valueTo = (Constants.minMCaps.size - 1).toFloat()
        setMinMcapText()

        minVolIndex = Constants.minVols.indexOf(minVol)
        binding.volSlider.value = minVolIndex.toFloat()
        binding.volSlider.valueFrom = 0.0f
        binding.volSlider.valueTo = (Constants.minVols.size - 1).toFloat()
        setMinVolText()

        binding.numTopCoinsSlider.valueFrom = Constants.TOP_COINS_FROM.toFloat()
        binding.numTopCoinsSlider.valueTo = Constants.TOP_COINS_TO.toFloat()
        binding.numTopCoinsSlider.value = nTopCoins.toFloat()
        setNumTopCoinsText()

        binding.favoriteChangeCheckbox.isChecked = toCheckFavorites
        
        App.instance.notificationPermission
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.favoriteChangeCheckbox.isChecked = it
            }
            .addTo(autoDisposable)

        binding.favoriteChangeValueSlider.valueFrom = Constants.FAVORITE_CHECK_MIN_CHANGE
        binding.favoriteChangeValueSlider.valueTo = Constants.FAVORITE_CHECK_MAX_CHANGE
        binding.favoriteChangeValueSlider.value = favoriteChange
        binding.favoriteChangeValueSlider.isEnabled = toCheckFavorites
        setFavoriteChangeValueText()

        binding.mcapSlider.addOnChangeListener { slider, fl, b ->
            minMcapIndex = slider.value.toInt()
            minMcap = Constants.minMCaps.get(minMcapIndex)
            setMinMcapText()
        }

        binding.volSlider.addOnChangeListener { slider, fl, b ->
            minVolIndex = slider.value.toInt()
            minVol = Constants.minVols.get(minVolIndex)
            setMinVolText()
        }

        binding.numTopCoinsSlider.addOnChangeListener { slider, value, fromUser ->
            nTopCoins = slider.value.toInt()
            setNumTopCoinsText()
        }

        binding.favoriteChangeValueSlider.addOnChangeListener { slider, value, fromUser ->
            favoriteChange = slider.value
            setFavoriteChangeValueText()
        }

        binding.favoriteChangeCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            // if user wants to get notification and the permission isn't granted
            toCheckFavorites = isChecked
            if (isChecked) (requireActivity() as MainActivity).askNotificationPermission()
            binding.favoriteChangeValueSlider.isEnabled = toCheckFavorites
        }
        return binding.root
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

