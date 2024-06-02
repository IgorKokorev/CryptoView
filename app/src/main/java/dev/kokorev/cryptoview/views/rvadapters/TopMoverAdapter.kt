package dev.kokorev.cryptoview.views.rvadapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.kokorev.cryptoview.data.entity.GainerCoin
import dev.kokorev.cryptoview.databinding.GainerCoinItemBinding
import dev.kokorev.cryptoview.views.rvviewholders.TopMoverItemViewHolder


class TopMoverAdapter(
    private val clickListener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val data = mutableListOf<GainerCoin>()

    interface OnItemClickListener {
        fun click(gainerCoin: GainerCoin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TopMoverItemViewHolder(
            GainerCoinItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TopMoverItemViewHolder -> {
                val gainerCoin = data[position]
                holder.setData(gainerCoin, clickListener, position)
            }
        }
    }

    fun addItems(newList: List<GainerCoin>) {
        val numbersDiff = Diff(data, newList)
        val diffResult = DiffUtil.calculateDiff(numbersDiff)
        data.clear()
        data.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}
