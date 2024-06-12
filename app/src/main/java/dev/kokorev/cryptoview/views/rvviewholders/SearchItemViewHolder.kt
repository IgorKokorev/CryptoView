package dev.kokorev.cryptoview.views.rvviewholders

import androidx.recyclerview.widget.RecyclerView
import dev.kokorev.cryptoview.databinding.SearchCoinItemBinding
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.utils.NumbersUtils.formatPrice
import dev.kokorev.cryptoview.utils.NumbersUtils.setChangeView
import dev.kokorev.cryptoview.views.rvadapters.SearchAdapter
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB

class SearchItemViewHolder(val binding: SearchCoinItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private lateinit var ticker: CoinPaprikaTickerDB

    fun setData(
        coinPaprikaTickerDB: CoinPaprikaTickerDB,
        clickListener: SearchAdapter.OnItemClickListener,
    ) {
        ticker = coinPaprikaTickerDB
        binding.coinSymbol.text = ticker.symbol
        binding.coinName.text = ticker.name
        binding.coinPrice.text = formatPrice(ticker.price)
        binding.coinAth.text = formatPrice(ticker.athPrice)
        setChangeView(binding.root.context, binding.coinChange, ticker.percentChange24h, "%")
        setChangeView(binding.root.context, binding.coinAthChange, ticker.percentFromPriceAth, "%")

        val volume = NumbersUtils.formatBigNumber(ticker.dailyVolume ?: 0.0)
        binding.coinVolume.text = volume

        val mcap = NumbersUtils.formatBigNumber(ticker.marketCap ?: 0.0)
        binding.coinMcap.text = mcap

        binding.root.setOnClickListener {
            clickListener.click(ticker)
        }
    }

}
