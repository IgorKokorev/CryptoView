package dev.kokorev.cryptoview.views.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coinpaprika.apiclient.entity.MoverEntity
import dev.kokorev.cmc_api.entity.cmc_listing.CmcListingData
import dev.kokorev.cryptoview.viewModel.MainViewModel
import dev.kokorev.cryptoview.databinding.FragmentMainBinding
import dev.kokorev.cryptoview.databinding.MainCoinItemBinding
import dev.kokorev.cryptoview.utils.AutoDisposable
import dev.kokorev.cryptoview.utils.NumbersUtils.roundNumber
import dev.kokorev.cryptoview.utils.addTo
import dev.kokorev.cryptoview.views.MainActivity
import dev.kokorev.cryptoview.views.rvadapters.MainAdapter
import dev.kokorev.cryptoview.views.rvadapters.TopSpacingItemDecoration
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.DecimalFormat

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private val autoDisposable = AutoDisposable()
    private val viewModel: MainViewModel by viewModels()
    private lateinit var mainAdapter: MainAdapter
    private var cmcListingLatest: List<MoverEntity> = listOf()
        set(value) {
            if (field == value) return
            field = value.sortedByDescending { it.percentChange }
            mainAdapter.addItems(field)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDataFromViewModel()
        initRecycler()
    }

    private fun initRecycler() {
        mainAdapter = MainAdapter(object : MainAdapter.OnItemClickListener {
            override fun click(
                moverEntity: MoverEntity,
                position: Int,
                binding: MainCoinItemBinding
            ) {
                (requireActivity() as MainActivity).launchInfoFragment(
                    moverEntity.id,
                    moverEntity.symbol
                )
            }
        })
        mainAdapter.addItems(cmcListingLatest)
        binding.mainRecycler.adapter = mainAdapter
        binding.mainRecycler.addItemDecoration(TopSpacingItemDecoration(0))
    }

    private fun setupDataFromViewModel() {
        viewModel.interactor.getCoinPaprikaTop10Movers()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { dto ->
                    cmcListingLatest = dto.gainers + dto.losers
                    binding.mainRecycler.scheduleLayoutAnimation()
                },
                {
                    Log.d("MainFragment", "Error getting data from CoinPaparikaTop10Movers", it)
                })
            .addTo(autoDisposable)
    }
}