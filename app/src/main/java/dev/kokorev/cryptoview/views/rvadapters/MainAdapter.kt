package dev.kokorev.cryptoview.views.rvadapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.coinpaprika.apiclient.entity.MoverEntity
import dev.kokorev.cmc_api.entity.cmc_listing.CmcListingData
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.MainCoinItemBinding
import dev.kokorev.cryptoview.views.rvviewholders.MainItemViewHolder


class MainAdapter(private val clickListener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val data = mutableListOf<MoverEntity>()

    interface OnItemClickListener {
        fun click(moverEntity: MoverEntity, position: Int, binding: MainCoinItemBinding)
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

    fun addItems(newList: List<MoverEntity>) {
        val numbersDiff = MainDiff(data, newList)
        val diffResult = DiffUtil.calculateDiff(numbersDiff)
        data.clear()
        data.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}
