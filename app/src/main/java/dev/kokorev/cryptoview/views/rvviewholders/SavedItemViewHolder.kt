package dev.kokorev.cryptoview.views.rvviewholders

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.kokorev.cryptoview.data.entity.SavedCoin
import dev.kokorev.cryptoview.databinding.SavedCoinItemBinding
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.utils.NumbersUtils.setPrice
import dev.kokorev.cryptoview.utils.NumbersUtils.setPriceChange
import dev.kokorev.cryptoview.views.rvadapters.SavedAdapter

class SavedItemViewHolder(val binding: SavedCoinItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setData(
        coin: SavedCoin,
        clickListener: SavedAdapter.OnItemClickListener,
        position: Int
    ) {
        Glide.with(binding.root)
            .load(coin.logo)
            .into(binding.logo)
        binding.coinName.text = coin.name
        binding.coinSymbol.text = coin.symbol
        binding.coinPrice.text = setPrice(coin.price)
        setPriceChange(
            coin.percentChange24h,
            binding.root.context,
            binding.coinChange
        )
        val volume = NumbersUtils.formatBigNumber(coin.dailyVolume ?: 0.0)
        binding.coinVolume.text = volume

        val mcap = NumbersUtils.formatBigNumber(coin.marketCap ?: 0.0)
        binding.coinMcap.text = mcap
        binding.coinRank.text = coin.rank.toString()
        binding.coinType.text = coin.type?.get(0)?.uppercase()

        binding.root.setOnClickListener {
            clickListener.click(coin)
        }
    }
}
