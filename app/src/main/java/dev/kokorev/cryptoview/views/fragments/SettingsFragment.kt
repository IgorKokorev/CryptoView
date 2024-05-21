package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.databinding.FragmentSettingsBinding
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.viewModel.SettingsViewModel

class SettingsFragment : Fragment() {
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var binding: FragmentSettingsBinding
    private var minMcapIndex : Int = 0
    private var minMcap : Long = Constants.minMCaps.get(0)
    private var minVolIndex : Int = 0
    private var minVol : Long = Constants.minVols.get(0)
    private var numTopCoins = Constants.TOP_COINS_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSettingsBinding.inflate(layoutInflater)

        minMcap = viewModel.preferences.getMinMcap()
        minMcapIndex = Constants.minMCaps.indexOf(minMcap)
        minVol = viewModel.preferences.getMinVol()
        minVolIndex = Constants.minVols.indexOf(minVol)
        numTopCoins = viewModel.preferences.getNumTopCoins()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.mcapText.text = NumbersUtils.formatBigNumberShort(minMcap)
        binding.volText.text = NumbersUtils.formatBigNumberShort(minVol)
        binding.numTopCoinsText.text = numTopCoins.toString()


        binding.mcapSlider.value = minMcapIndex.toFloat()
        binding.volSlider.value = minVolIndex.toFloat()
        binding.numTopCoinsSlider.valueFrom = Constants.TOP_COINS_FROM.toFloat()
        binding.numTopCoinsSlider.valueTo = Constants.TOP_COINS_TO.toFloat()
        binding.numTopCoinsSlider.value = numTopCoins.toFloat()

        binding.mcapSlider.addOnChangeListener { slider, fl, b ->
            minMcapIndex = slider.value.toInt()
            minMcap = Constants.minMCaps.get(minMcapIndex)
            binding.mcapText.text = NumbersUtils.formatBigNumberShort(minMcap)
        }

        binding.volSlider.addOnChangeListener { slider, fl, b ->
            minVolIndex = slider.value.toInt()
            minVol = Constants.minVols.get(minVolIndex)
            binding.volText.text = NumbersUtils.formatBigNumberShort(minVol)
        }

        binding.numTopCoinsSlider.addOnChangeListener { slider, value, fromUser ->
            numTopCoins = slider.value.toInt()
            binding.numTopCoinsText.text = numTopCoins.toString()
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        viewModel.preferences.saveMinMcap(minMcap)
        viewModel.preferences.saveMinVol(minVol)
        viewModel.preferences.saveNumTopCoins(numTopCoins)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.preferences.saveMinMcap(minMcap)
        viewModel.preferences.saveMinVol(minVol)
        viewModel.preferences.saveNumTopCoins(numTopCoins)
    }
}