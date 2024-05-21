package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.databinding.FragmentSettingsBinding
import dev.kokorev.cryptoview.viewModel.SettingsViewModel
import java.text.DecimalFormat

class SettingsFragment : Fragment() {
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var binding: FragmentSettingsBinding
    private var minMcap : Long = 0L
    private var minMcapIndex : Int = 0
    private var minVol : Long = 0L
    private var minVolIndex : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        minMcap = viewModel.repository.getMinMcap()
        minMcapIndex = Constants.minMCaps.indexOf(minMcap)
        minVol = viewModel.repository.getMinVol()
        minVolIndex = Constants.minVols.indexOf(minVol)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.mcapText.text = DecimalFormat("#,##0").format(minMcap)
        binding.volText.text = DecimalFormat("#,##0").format(minVol)

        binding.mcapSlider.value = minMcapIndex.toFloat()
        binding.volSlider.value = minVolIndex.toFloat()

        binding.mcapSlider.addOnChangeListener { slider, fl, b ->
            minMcapIndex = slider.value.toInt()
            minMcap = Constants.minMCaps.get(minMcapIndex)
            binding.mcapText.text = DecimalFormat("#,##0").format(minMcap)
//                NumbersUtils.formatBigNumber(Constants.minMCaps.get(index).toDouble())
        }

        binding.volSlider.addOnChangeListener { slider, fl, b ->
            minVolIndex = slider.value.toInt()
            minVol = Constants.minVols.get(minVolIndex)
            binding.volText.text = DecimalFormat("#,##0").format(minVol)
//                NumbersUtils.formatBigNumber(Constants.minMCaps.get(index).toDouble())
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        viewModel.repository.saveMinMcap(minMcap)
        viewModel.repository.saveMinVol(minVol)
    }
}