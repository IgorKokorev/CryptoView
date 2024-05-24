package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.kokorev.cryptoview.data.Constants
import dev.kokorev.cryptoview.databinding.FragmentSettingsBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.viewModel.SettingsViewModel
import dev.kokorev.cryptoview.views.MainActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.text.DecimalFormat

class SettingsFragment : Fragment() {
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var binding: FragmentSettingsBinding
    private var minMcapIndex: Int = 0
    private var minMcap: Long = Constants.minMCaps.get(0)
    private var minVolIndex: Int = 0
    private var minVol: Long = Constants.minVols.get(0)
    private var numTopCoins = Constants.TOP_COINS_DEFAULT
    private var toCheckFavorites: Boolean = true
    private var favoriteChange: Float = Constants.FAVORITE_CHECK_MAX_CHANGE
    private var autoDisposable = AutoDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        minMcap = viewModel.preferences.getMinMcap()
        minMcapIndex = Constants.minMCaps.indexOf(minMcap)
        binding.mcapSlider.value = minMcapIndex.toFloat()
        binding.mcapSlider.valueFrom = 0.0f
        binding.mcapSlider.valueTo = (Constants.minMCaps.size - 1).toFloat()
        setMinMcapText()

        minVol = viewModel.preferences.getMinVol()
        minVolIndex = Constants.minVols.indexOf(minVol)
        binding.volSlider.value = minVolIndex.toFloat()
        binding.volSlider.valueFrom = 0.0f
        binding.volSlider.valueTo = (Constants.minVols.size - 1).toFloat()
        setMinVolText()

        numTopCoins = viewModel.preferences.getNumTopCoins()
        binding.numTopCoinsSlider.valueFrom = Constants.TOP_COINS_FROM.toFloat()
        binding.numTopCoinsSlider.valueTo = Constants.TOP_COINS_TO.toFloat()
        binding.numTopCoinsSlider.value = numTopCoins.toFloat()
        setNumTopCoinsText()

        toCheckFavorites = viewModel.preferences.toCheckFavorites()
        binding.favoriteChangeCheckbox.isChecked = toCheckFavorites
        viewModel.notificationManager.notificationPermission
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.favoriteChangeCheckbox.isChecked = it
            }
            .addTo(autoDisposable)

        favoriteChange = viewModel.preferences.getFavoriteMinChange()
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
            numTopCoins = slider.value.toInt()
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
        binding.favoriteChangeValueText.text =
            DecimalFormat("0.00%").format(favoriteChange / 100f)
    }

    private fun setNumTopCoinsText() {
        binding.numTopCoinsText.text = numTopCoins.toString()
    }

    private fun setMinVolText() {
        binding.volText.text = NumbersUtils.formatBigNumberShort(minVol)
    }

    private fun setMinMcapText() {
        binding.mcapText.text = NumbersUtils.formatBigNumberShort(minMcap)
    }

    override fun onPause() {
        super.onPause()
        savePreferernces()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savePreferernces()
    }

    private fun savePreferernces() {
        viewModel.preferences.saveMinMcap(minMcap)
        viewModel.preferences.saveMinVol(minVol)
        viewModel.preferences.saveNumTopCoins(numTopCoins)
        viewModel.preferences.saveCheckFaforites(toCheckFavorites)
        viewModel.preferences.saveFavoriteMinChange(favoriteChange)

//        if (toCheckFavorites) App.instance.startFavoriteCheckService()
//        else App.instance.stopFavoriteCheckService()
    }


}

