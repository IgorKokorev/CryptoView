package dev.kokorev.cryptoview.views.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.data.Constants
import dev.kokorev.cryptoview.databinding.FragmentCoinBinding
import dev.kokorev.cryptoview.viewModel.CoinViewModel
import kotlin.reflect.full.createInstance

class CoinFragment : Fragment() {
    private lateinit var binding: FragmentCoinBinding
    private val viewModel: CoinViewModel by viewModels<CoinViewModel>()
    private lateinit var adapter: CoinPagerAdapter
    // fragments - elements of the ViewPager
    private val fragments = listOf(
        InfoFragment::class,
        ChartFragment::class,
        BinanceFragment::class,
        AiReportFragment::class,
    )
    // icons for every tab/fragment. Should be initialised after the context is available
    private lateinit var icons: List<Drawable?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentCoinBinding.inflate(layoutInflater)

        // Saving args to common ViewModel to reuse by all sub-fragments
        viewModel.coinPaprikaId = arguments?.getString(Constants.COIN_PAPRIKA_ID) ?: ""
        viewModel.symbol = arguments?.getString(Constants.COIN_SYMBOL) ?: ""
        viewModel.name = arguments?.getString(Constants.COIN_NAME) ?: ""

        // icons for every Tab
        icons = listOf(
            ResourcesCompat.getDrawable(resources, R.drawable.icon_info, null),
            ResourcesCompat.getDrawable(resources, R.drawable.icon_chart, null),
            ResourcesCompat.getDrawable(resources, R.drawable.binance_logo, null),
            ResourcesCompat.getDrawable(resources, R.drawable.svg_report, null),
        )

        // Setting ViewPager adapter and Tab
        adapter = CoinPagerAdapter(this, arguments)
        binding.coinPager.adapter = adapter
        TabLayoutMediator(binding.coinTab, binding.coinPager) { tab, position ->
            tab.icon = icons.get(position)
        }.attach()
        binding.coinPager.isSaveEnabled = false // To avoid exceptions on back pressed
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    // ViewPager adapter
    inner class CoinPagerAdapter(fragment: Fragment, private val args: Bundle?) :
        FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment {
            val fragment = fragments.get(position).createInstance()
            fragment.arguments = args
            return fragment
        }
    }
}

