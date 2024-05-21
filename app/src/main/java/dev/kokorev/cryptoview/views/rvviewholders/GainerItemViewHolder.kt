package dev.kokorev.cryptoview.views.rvviewholders

import androidx.recyclerview.widget.RecyclerView
import dev.kokorev.cryptoview.data.entity.GainerCoin
import dev.kokorev.cryptoview.databinding.GainerCoinItemBinding
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.utils.NumbersUtils.setPrice
import dev.kokorev.cryptoview.utils.NumbersUtils.setPriceChange
import dev.kokorev.cryptoview.views.rvadapters.GainerAdapter

class GainerItemViewHolder(val binding: GainerCoinItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setData(
        coin: GainerCoin,
        clickListener: GainerAdapter.OnItemClickListener,
        position: Int
    ) {
        binding.coinName.text = coin.name
        binding.coinSymbol.text = coin.symbol
        binding.coinPrice.text = setPrice(coin.price)
        setPriceChange(
            coin.percentChange,
            binding.root.context,
            binding.coinChange
        )
        val volume = NumbersUtils.formatBigNumber(coin.dailyVolume ?: 0.0)
        binding.coinVolume.text = volume

        val mcap = NumbersUtils.formatBigNumber(coin.marketCap ?: 0.0)
        binding.coinMcap.text = mcap
        binding.coinRank.text = coin.rank.toString()

        binding.root.setOnClickListener {
            clickListener.click(coin)
        }
    }
}
