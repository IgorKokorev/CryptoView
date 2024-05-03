package dev.kokorev.cryptoview.views.rvadapters

import androidx.recyclerview.widget.DiffUtil
import dev.kokorev.room_db.core_api.entity.TopMover

// Calculates difference between 2 film lists for DiffUtil
class MainDiff(val oldList: MutableList<TopMover>, val newList: List<TopMover>) :
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
