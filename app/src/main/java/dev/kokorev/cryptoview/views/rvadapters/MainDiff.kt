package dev.kokorev.cryptoview.views.rvadapters

import androidx.recyclerview.widget.DiffUtil
import com.coinpaprika.apiclient.entity.MoverEntity
import dev.kokorev.cmc_api.entity.cmc_listing.CmcListingData

// Calculates difference between 2 film lists for DiffUtil
class MainDiff(val oldList: MutableList<MoverEntity>, val newList: List<MoverEntity>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].equals(newList[newItemPosition])
    }
}
