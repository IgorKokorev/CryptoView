package dev.kokorev.cryptoview.views.rvadapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.kokorev.cryptoview.databinding.SearchCoinItemBinding
import dev.kokorev.cryptoview.views.rvviewholders.SearchItemViewHolder
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB


class SearchAdapter(private val clickListener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val data = mutableListOf<CoinPaprikaTickerDB>()

    interface OnItemClickListener {
        fun click(coinPaprikaTickerDB: CoinPaprikaTickerDB)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SearchItemViewHolder(SearchCoinItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchItemViewHolder -> {
                val coinPaprikaTicker = data[position]
                holder.setData(coinPaprikaTicker, clickListener, position)
            }
        }
    }

    fun addItems(newList: List<CoinPaprikaTickerDB>) {
        val numbersDiff = Diff(data, newList)
        val diffResult = DiffUtil.calculateDiff(numbersDiff)
        data.clear()
        data.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}
