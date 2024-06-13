package dev.kokorev.cryptoview.views.rvadapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.coinpaprika.apiclient.entity.MessageDB
import com.coinpaprika.apiclient.entity.MessageType
import dev.kokorev.cryptoview.databinding.ChatInItemBinding
import dev.kokorev.cryptoview.databinding.ChatOutItemBinding
import dev.kokorev.cryptoview.views.rvviewholders.ChatInItemViewHolder
import dev.kokorev.cryptoview.views.rvviewholders.ChatOutItemViewHolder


class ChatAdapter(
    private val clickListener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val data = mutableListOf<MessageDB>()
    private val IN_MESSAGE = MessageType.IN.value
    private val OUT_MESSAGE = MessageType.OUT.value
    
    interface OnItemClickListener {
        fun click(messageDB: MessageDB)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            IN_MESSAGE -> ChatInItemViewHolder(
                ChatInItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            
            else -> ChatOutItemViewHolder(
                ChatOutItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }
    
    override fun getItemCount(): Int {
        return data.size
    }
    
    override fun getItemViewType(position: Int): Int {
        return data[position].type.value
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ChatInItemViewHolder -> {
                val messageDB = data[position]
                holder.setData(messageDB, clickListener)
            }
            
            is ChatOutItemViewHolder -> {
                val messageDB = data[position]
                holder.setData(messageDB, clickListener)
            }
        }
    }
    
    fun addItems(newList: List<MessageDB>) {
        val numbersDiff = Diff(data, newList)
        val diffResult = DiffUtil.calculateDiff(numbersDiff)
        data.clear()
        data.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}
