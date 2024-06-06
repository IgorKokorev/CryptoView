package dev.kokorev.cryptoview.views.rvviewholders

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.data.entity.SavedCoin
import dev.kokorev.cryptoview.databinding.SavedCoinItemBinding
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.utils.NumbersUtils.formatPrice
import dev.kokorev.cryptoview.utils.NumbersUtils.setChangeView
import dev.kokorev.cryptoview.views.rvadapters.SavedAdapter

class SavedItemViewHolder(val binding: SavedCoinItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setData(
        coin: SavedCoin,
        clickListener: SavedAdapter.OnItemClickListener,
    ) {
        Glide.with(binding.root)
            .load(coin.logo)
            .into(binding.logo)
        binding.coinName.text = coin.name
        binding.coinSymbol.text = coin.symbol
        binding.coinPrice.text = formatPrice(coin.price)
        setChangeView(
            coin.percentChange,
            binding.root.context,
            binding.coinChange,
            "%"
        )
        val volume = NumbersUtils.formatBigNumber(coin.dailyVolume ?: 0.0)
        binding.coinVolume.text = volume

        val mcap = NumbersUtils.formatBigNumber(coin.marketCap ?: 0.0)
        binding.coinMcap.text = mcap
        binding.coinRank.text = coin.rank.toString()
        val type = coin.type?.get(0)?.uppercase()
        binding.coinType.text = type
        if (type == "T") binding.coinType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.tokenColor))

        binding.root.setOnClickListener {
            clickListener.click(coin)
        }
    }
}
