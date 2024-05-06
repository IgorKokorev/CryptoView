package dev.kokorev.cryptoview.views.rvviewholders

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.SearchCoinItemBinding
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.views.rvadapters.SearchAdapter
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTicker
import java.text.DecimalFormat

class SearchItemViewHolder(val binding: SearchCoinItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun setData(
        coinPaprikaTicker: CoinPaprikaTicker,
        clickListener: SearchAdapter.OnItemClickListener,
        position: Int
    ) {
        binding.coinSymbol.text = coinPaprikaTicker.symbol

        val priceStr = if (coinPaprikaTicker.price == null) "-"
        else DecimalFormat("#,###.########").format(
            NumbersUtils.roundNumber(
                coinPaprikaTicker.price!!,
                3
            )
        )
        binding.coinPrice.text = priceStr

        val percentChange = coinPaprikaTicker.percentChange24h ?: 0.0
        val change = DecimalFormat("#,##0.0").format(
            NumbersUtils.roundNumber(
                percentChange,
                2
            )
        ) + "%"
        binding.coinChange.text = change


        val athStr = if (coinPaprikaTicker.athPrice == null) "-"
        else DecimalFormat("#,###.########").format(
            NumbersUtils.roundNumber(
                coinPaprikaTicker.athPrice!!,
                3
            )
        )
        binding.coinAth.text = athStr

        if (percentChange < 0) {
            binding.coinChange.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.lightAccent
                )
            )
        } else {
            binding.coinChange.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.light
                )
            )
        }

        val volume = NumbersUtils.formatBigNumber(coinPaprikaTicker.dailyVolume ?: 0.0)
        binding.coinVolume.text = volume

        val mcap = NumbersUtils.formatBigNumber(coinPaprikaTicker.marketCap ?: 0.0)
        binding.coinMcap.text = mcap

        binding.root.setOnClickListener {
            clickListener.click(coinPaprikaTicker, position, binding)
        }
    }
}
