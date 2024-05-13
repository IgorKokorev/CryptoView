package dev.kokorev.cryptoview.views.rvadapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.kokorev.cryptoview.data.entity.SavedCoin
import dev.kokorev.cryptoview.databinding.SavedCoinItemBinding
import dev.kokorev.cryptoview.views.rvviewholders.SavedItemViewHolder


class SavedAdapter(
    private val clickListener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val data = mutableListOf<SavedCoin>()

    interface OnItemClickListener {
        fun click(savedCoin: SavedCoin, position: Int, binding: SavedCoinItemBinding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SavedItemViewHolder(
            SavedCoinItemBinding.inflate(
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
            is SavedItemViewHolder -> {
                val savedCoin = data[position]
                holder.setData(savedCoin, clickListener, position)
            }
        }
    }

    fun addItems(newList: List<SavedCoin>) {
        val numbersDiff = Diff(data, newList)
        val diffResult = DiffUtil.calculateDiff(numbersDiff)
        data.clear()
        data.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}
