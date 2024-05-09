package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CoinPagerAdapter(this, arguments)
        binding.coinPager.adapter = adapter
        TabLayoutMediator(binding.coinTab, binding.coinPager) { tab, position ->
            when (position) {
                0 -> {
//                    tab.text = "Info"
                    tab.icon = resources.getDrawable(R.drawable.icon_info, null)

                }
                1 -> {
//                    tab.text = "Chart"
                    tab.icon = resources.getDrawable(R.drawable.icon_chart, null)
                }
                else -> {

                }
            }
            Log.d("CoinFragment", "Setting Coin ViewPager TabLayout. PaddingTop = ${tab.view.paddingTop}. PaddingBottom = ${tab.view.paddingBottom}. MarginTop = ${tab.view.marginTop}. MarginBottom = ${tab.view.marginBottom}. Height = ${tab.view.height}.")
        }.attach()
    }
}

class CoinPagerAdapter(fragment: Fragment, val args: Bundle?): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = if (position == 1) ChartFragment()
        else InfoFragment()
        fragment.arguments = args
        return fragment
    }

}
