package dev.kokorev.cryptoview.views.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.viewModel.InfoViewModel
import dev.kokorev.cryptoview.databinding.FragmentInfoBinding
import dev.kokorev.cryptoview.databinding.MainTeamMemberViewBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.addTo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class InfoFragment : Fragment() {
    private lateinit var binding: FragmentInfoBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: InfoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoBinding.inflate(layoutInflater)
        val id = arguments?.getString(Constants.SYMBOL) ?: return binding.root

        viewModel.interactor.getCoinPaprikaCoinInfo(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { coinDetails ->
                Glide.with(binding.root) // container
                    .load(coinDetails.logo) // what picture is loaded
                    .centerCrop()
                    .into(binding.logo)
                val nameAndSymbol = coinDetails.name + " (" + coinDetails.symbol + ")"
                binding.name.text = nameAndSymbol
                binding.description.text = coinDetails.description
                getUrls(
                    coinDetails.links?.website,
                    binding.websiteBlock,
                    binding.websiteLink
                )
                getUrls(
                    coinDetails.links?.reddit,
                    binding.redditBlock,
                    binding.redditLink
                )
                getUrls(coinDetails.links?.facebook, binding.facebookBlock, binding.facebookLink)
                getUrls(
                    coinDetails.links?.youtube,
                    binding.youtubeBlock,
                    binding.youtubeLink
                )
                getUrls(
                    coinDetails.links?.sourceCode,
                    binding.sourceCodeBlock,
                    binding.sourceCodeLink
                )
                getUrls(
                    coinDetails.links?.explorer,
                    binding.explorerBlock,
                    binding.explorerLink
                )
                coinDetails.team?.forEach {
                    val teamMemberBinding = MainTeamMemberViewBinding.inflate(layoutInflater)
                    teamMemberBinding.name.text = it.name
                    teamMemberBinding.position.text = it.role
                    binding.team.addView(teamMemberBinding.root)
                }
            }
            .addTo(autoDisposable)

        return binding.root
    }

    private fun getUrls(
        urls: List<String>?,
        block: View,
        link: TextView,
    ) {

        if (urls == null || urls.isEmpty()) {
            binding.urls.removeView(block)
        } else {
            val builder = StringBuilder()
            urls.forEach {
                builder.append(it, "\n")
            }
            link.text = builder.toString()
        }
    }
}