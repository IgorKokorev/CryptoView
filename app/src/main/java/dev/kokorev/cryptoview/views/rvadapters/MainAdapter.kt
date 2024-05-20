package dev.kokorev.cryptoview.views.rvadapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.kokorev.cryptoview.databinding.MainCoinItemBinding
import dev.kokorev.cryptoview.views.rvviewholders.MainItemViewHolder
import dev.kokorev.room_db.core_api.entity.TopMoverDB


class MainAdapter(private val clickListener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val data = mutableListOf<TopMoverDB>()

    interface OnItemClickListener {
        fun click(topMoverDB: TopMoverDB)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MainItemViewHolder(MainCoinItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MainItemViewHolder -> {
                val moverEntity = data[position]
                holder.setData(moverEntity, clickListener, position)
            }
        }
    }

    fun addItems(newList: List<TopMoverDB>) {
        val numbersDiff = Diff(data, newList)
        val diffResult = DiffUtil.calculateDiff(numbersDiff)
        data.clear()
        data.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}
