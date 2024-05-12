package dev.kokorev.cryptoview.views.rvviewholders

import android.widget.TextView
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
    private lateinit var ticker: CoinPaprikaTicker

    fun setData(
        coinPaprikaTicker: CoinPaprikaTicker,
        clickListener: SearchAdapter.OnItemClickListener,
        position: Int
    ) {
        ticker = coinPaprikaTicker
        binding.coinSymbol.text = ticker.symbol
        binding.coinName.text = ticker.name
        binding.coinPrice.text = setPrice(ticker.price)
        binding.coinAth.text = setPrice(ticker.athPrice)
        setPriceChange(ticker.percentChange24h, binding.coinChange)
        setPriceChange(ticker.percentFromPriceAth, binding.coinAthChange)

        val volume = NumbersUtils.formatBigNumber(ticker.dailyVolume ?: 0.0)
        binding.coinVolume.text = volume

        val mcap = NumbersUtils.formatBigNumber(ticker.marketCap ?: 0.0)
        binding.coinMcap.text = mcap

        binding.root.setOnClickListener {
            clickListener.click(ticker, position, binding)
        }
    }

    private fun setPrice(price: Double?): String {
        return if (price == null) "-"
        else DecimalFormat("#,###.########").format(
            NumbersUtils.roundNumber(
                price,
                3
            )
        )
    }

    private fun setPriceChange(percentChange: Double?, view: TextView) {
        val percentChange = percentChange ?: 0.0
        var change = DecimalFormat("#,##0.0").format(
            NumbersUtils.roundNumber(
                percentChange,
                2
            )
        ) + "%"
        if (percentChange < 0) {
            view.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.lightAccent
                )
            )
        } else if (percentChange > 0) {
            change = '+' + change
            view.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.light
                )
            )
        }
        view.text = change
    }
}
