package dev.kokorev.cryptoview.views.rvviewholders

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coinpaprika.apiclient.entity.PortfolioCoinDB
import dev.kokorev.cryptoview.databinding.PortfolioCoinItemBinding
import dev.kokorev.cryptoview.utils.NumbersUtils.setChange
import dev.kokorev.cryptoview.utils.NumbersUtils.setPrice
import dev.kokorev.cryptoview.views.rvadapters.PortfolioAdapter
import java.text.DecimalFormat

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
        binding.coinPrice.text = setPrice(coin.priceLastEvaluation)
        val percentChange = (coin.priceLastEvaluation / coin.priceOpen - 1.0) * 100.0
        setChange(
            percentChange,
            binding.root.context,
            binding.coinChange,
            "%"
        )
        val decimalFormat = DecimalFormat.getInstance(binding.root.context.resources.configuration.locales[0])
        binding.coinQty.text = decimalFormat.format(coin.quantity)
        binding.coinVal.text = setPrice(coin.quantity * coin.priceLastEvaluation)

        val pnlNumber = coin.quantity * (coin.priceLastEvaluation - coin.priceOpen)
        setChange(pnlNumber, binding.root.context, binding.coinPnl, "")
/*
        var pnlString = setPrice(pnlNumber)
        val view = binding.coinPnl
        if (pnlNumber < 0) {
            view.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.red
                )
            )
        } else if (pnlNumber > 0) {
            pnlString = '+' + pnlString
            view.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.green
                )
            )
        }
        view.text = pnlString
*/

        binding.root.setOnClickListener {
            clickListener.click(coin)
        }
    }
}
