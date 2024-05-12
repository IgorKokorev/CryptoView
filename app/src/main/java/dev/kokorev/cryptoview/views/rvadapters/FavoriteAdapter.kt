package dev.kokorev.cryptoview.views.rvadapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.coinpaprika.apiclient.entity.FavoriteCoin
import dev.kokorev.cryptoview.databinding.FavoriteCoinItemBinding
import dev.kokorev.cryptoview.databinding.MainCoinItemBinding
import dev.kokorev.cryptoview.views.rvviewholders.FavoriteItemViewHolder
import dev.kokorev.cryptoview.views.rvviewholders.MainItemViewHolder
import dev.kokorev.room_db.core_api.entity.TopMover


class FavoriteAdapter(private val clickListener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val data = mutableListOf<FavoriteCoin>()

    interface OnItemClickListener {
        fun click(favoriteCoin: FavoriteCoin, position: Int, binding: FavoriteCoinItemBinding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FavoriteItemViewHolder(FavoriteCoinItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FavoriteItemViewHolder -> {
                val favoriteCoin = data[position]
                holder.setData(favoriteCoin, clickListener, position)
            }
        }
    }

    fun addItems(newList: List<FavoriteCoin>) {
        val numbersDiff = Diff(data, newList)
        val diffResult = DiffUtil.calculateDiff(numbersDiff)
        data.clear()
        data.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}
