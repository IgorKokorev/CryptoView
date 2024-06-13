package dev.kokorev.cryptoview.views.rvadapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.coinpaprika.apiclient.entity.PortfolioPositionDB
import dev.kokorev.cryptoview.databinding.PortfolioCoinItemBinding
import dev.kokorev.cryptoview.views.rvviewholders.PortfolioItemViewHolder


class PortfolioAdapter(
    private val clickListener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val data = mutableListOf<PortfolioPositionDB>()

    interface OnItemClickListener {
        fun click(portfolioPositionDB: PortfolioPositionDB)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PortfolioItemViewHolder(
            PortfolioCoinItemBinding.inflate(
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
            is PortfolioItemViewHolder -> {
                val portfolioCoinDB = data[position]
                holder.setData(portfolioCoinDB, clickListener)
            }
        }
    }

    fun addItems(newList: List<PortfolioPositionDB>) {
        val numbersDiff = Diff(data, newList)
        val diffResult = DiffUtil.calculateDiff(numbersDiff)
        data.clear()
        data.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}
