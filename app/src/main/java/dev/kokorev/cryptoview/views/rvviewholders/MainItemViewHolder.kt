package dev.kokorev.cryptoview.views.rvviewholders

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.coinpaprika.apiclient.entity.MoverEntity
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.MainCoinItemBinding
import dev.kokorev.cryptoview.utils.NumbersUtils
import dev.kokorev.cryptoview.views.rvadapters.MainAdapter
import java.text.DecimalFormat

class MainItemViewHolder(val binding: MainCoinItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setData(
        moverEntity: MoverEntity,
        clickListener: MainAdapter.OnItemClickListener,
        position: Int
    ) {
        binding.coinName.text = moverEntity.name
        binding.coinSymbol.text = moverEntity.symbol

        val change = DecimalFormat("#,###.##").format(
            NumbersUtils.roundNumber(
                moverEntity.percentChange,
                2
            )
        ) + "%"
        binding.coinChange.text = change

        if (moverEntity.percentChange < 0) {
            binding.card.setBackgroundResource(R.color.darkAccent)
        }

        binding.root.setOnClickListener {
            clickListener.click(moverEntity, position, binding)
        }
    }
}
