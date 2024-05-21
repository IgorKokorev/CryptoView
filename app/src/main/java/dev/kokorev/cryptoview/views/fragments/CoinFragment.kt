package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.FragmentCoinBinding
import dev.kokorev.cryptoview.viewModel.CoinViewModel

class CoinFragment : Fragment() {
    private lateinit var binding: FragmentCoinBinding
    private val viewModel: CoinViewModel by viewModels<CoinViewModel>()
    private lateinit var adapter: CoinPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentCoinBinding.inflate(layoutInflater)

        // Setting ViewPager adapter and Tab
        adapter = CoinPagerAdapter(this, arguments)
        binding.coinPager.adapter = adapter
        TabLayoutMediator(binding.coinTab, binding.coinPager) { tab, position ->
            when (position) {
                0 ->  tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.icon_info, null)
                1 ->  tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.icon_chart, null)
                2 ->  tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.svg_report, null)
                else -> {}
            }
        }.attach()
        binding.coinPager.isSaveEnabled = false // To avoid exceptions on back pressed

        // Saving args to common VM to reuse by all sub-fragments
        viewModel.coinPaprikaId = arguments?.getString(Constants.COIN_PAPRIKA_ID) ?: ""
        viewModel.symbol = arguments?.getString(Constants.COIN_SYMBOL) ?: ""
        viewModel.name = arguments?.getString(Constants.COIN_NAME) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

}

// ViewPager adapter
class CoinPagerAdapter(fragment: Fragment, private val args: Bundle?): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            1 -> ChartFragment()
            2 -> AiReportFragment()
            else -> InfoFragment()
        }
        fragment.arguments = args
        return fragment
    }
}
