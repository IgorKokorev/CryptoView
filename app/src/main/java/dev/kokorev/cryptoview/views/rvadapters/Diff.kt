package dev.kokorev.cryptoview.views.rvadapters

import androidx.recyclerview.widget.DiffUtil

// Calculates difference between 2 film lists for DiffUtil
class Diff<T: Any>(val oldList: MutableList<T>, val newList: List<T>) :
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
