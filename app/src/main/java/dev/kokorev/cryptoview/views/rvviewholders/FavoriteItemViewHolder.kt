package dev.kokorev.cryptoview.views.rvviewholders

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coinpaprika.apiclient.entity.FavoriteCoin
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.FavoriteCoinItemBinding
import dev.kokorev.cryptoview.databinding.MainCoinItemBinding
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.views.rvadapters.FavoriteAdapter
import dev.kokorev.cryptoview.views.rvadapters.MainAdapter
import dev.kokorev.room_db.core_api.entity.TopMover
import java.text.DecimalFormat

class FavoriteItemViewHolder(val binding: FavoriteCoinItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setData(
        coin: FavoriteCoin,
        clickListener: FavoriteAdapter.OnItemClickListener,
        position: Int
    ) {
        Glide.with(binding.root)
            .load(coin.logo)
            .into(binding.logo)
        binding.coinName.text = coin.name
        binding.coinSymbol.text = coin.symbol
        binding.coinRank.text = coin.rank.toString()
        binding.coinType.text = coin.type



        binding.root.setOnClickListener {
            clickListener.click(coin, position, binding)
        }
    }
}
