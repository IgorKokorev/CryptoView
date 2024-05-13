package dev.kokorev.cryptoview.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dev.kokorev.cryptoview.databinding.FragmentSavedBinding
import dev.kokorev.cryptoview.viewModel.SavedViewModel

class SavedFragment : Fragment() {
    private lateinit var binding: FragmentSavedBinding
    private lateinit var adapter: SavedPagerAdapter
    private val viewModel: SavedViewModel by viewModels<SavedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSavedBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SavedPagerAdapter(this, arguments)
        binding.savedPager.adapter = adapter
        TabLayoutMediator(binding.savedTab, binding.savedPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Favorites"
//                    tab.icon = resources.getDrawable(R.drawable.icon_info, null)

                }
                1 -> {
                    tab.text = "Recent"
//                    tab.icon = resources.getDrawable(R.drawable.icon_chart, null)
                }
                else -> {

                }
            }
        }.attach()
    }
}


class SavedPagerAdapter(fragment: Fragment, val args: Bundle?): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            1 -> RecentFragment()
            else -> FavoritesFragment()
        }
        fragment.arguments = args
        return fragment
    }

}
