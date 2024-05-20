package dev.kokorev.cryptoview.views.rvviewholders

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.MainCoinItemBinding
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.views.rvadapters.MainAdapter
import dev.kokorev.room_db.core_api.entity.TopMoverDB
import java.text.DecimalFormat

class MainItemViewHolder(val binding: MainCoinItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setData(
        topMoverDB: TopMoverDB,
        clickListener: MainAdapter.OnItemClickListener,
        position: Int
    ) {
        binding.coinName.text = topMoverDB.name
        binding.coinSymbol.text = topMoverDB.symbol

        val change = DecimalFormat("#,###.##").format(
            NumbersUtils.roundNumber(
                topMoverDB.percentChange,
                2
            )
        ) + "%"
        binding.coinChange.text = change

        if (topMoverDB.percentChange < 0) {
            binding.coinChange.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
        } else {
            binding.coinChange.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
        }

        binding.root.setOnClickListener {
            clickListener.click(topMoverDB)
        }
    }
}
