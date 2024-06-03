package dev.kokorev.cryptoview.views.rvviewholders

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coinpaprika.apiclient.entity.PortfolioCoinDB
import dev.kokorev.cryptoview.databinding.PortfolioCoinItemBinding
import dev.kokorev.cryptoview.utils.NumbersUtils.formatPrice
import dev.kokorev.cryptoview.utils.NumbersUtils.setChangeView
import dev.kokorev.cryptoview.views.rvadapters.PortfolioAdapter

class PortfolioItemViewHolder(val binding: PortfolioCoinItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setData(
        coin: PortfolioCoinDB,
        clickListener: PortfolioAdapter.OnItemClickListener,
        position: Int
    ) {
        Glide.with(binding.root)
            .load(coin.logo)
            .into(binding.logo)
        binding.coinName.text = coin.name
        binding.coinSymbol.text = coin.symbol
        binding.coinPrice.text = formatPrice(coin.priceLastEvaluation)
        val percentChange = (coin.priceLastEvaluation / coin.priceOpen - 1.0) * 100.0
        setChangeView(
            percentChange,
            binding.root.context,
            binding.coinChange,
            "%"
        )
        binding.coinQty.text = formatPrice(coin.quantity)
        binding.coinVal.text = formatPrice(coin.quantity * coin.priceLastEvaluation)

        val pnlNumber = coin.quantity * (coin.priceLastEvaluation - coin.priceOpen)
        setChangeView(pnlNumber, binding.root.context, binding.coinPnl, "")

        binding.root.setOnClickListener {
            clickListener.click(coin)
        }
    }
}
