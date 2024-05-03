package dev.kokorev.cryptoview.views.rvviewholders

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.MainCoinItemBinding
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.views.rvadapters.MainAdapter
import dev.kokorev.room_db.core_api.entity.TopMover
import java.text.DecimalFormat

class MainItemViewHolder(val binding: MainCoinItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setData(
        topMover: TopMover,
        clickListener: MainAdapter.OnItemClickListener,
        position: Int
    ) {
        binding.coinName.text = topMover.name
        binding.coinSymbol.text = topMover.symbol

        val change = DecimalFormat("#,###.##").format(
            NumbersUtils.roundNumber(
                topMover.percentChange,
                2
            )
        ) + "%"
        binding.coinChange.text = change

        if (topMover.percentChange < 0) {
            binding.coinChange.setTextColor(ContextCompat.getColor(binding.root.context, R.color.lightAccent))
        } else {
            binding.coinChange.setTextColor(ContextCompat.getColor(binding.root.context, R.color.light))
        }

        binding.root.setOnClickListener {
            clickListener.click(topMover, position, binding)
        }
    }
}
