package dev.kokorev.cryptoview.views.rvviewholders

import androidx.recyclerview.widget.RecyclerView
import com.coinpaprika.apiclient.entity.MessageDB
import dev.kokorev.cryptoview.databinding.ChatOutItemBinding
import dev.kokorev.cryptoview.views.rvadapters.ChatAdapter
import java.text.SimpleDateFormat
import java.util.Locale

class ChatOutItemViewHolder(val binding: ChatOutItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun setData(
        message: MessageDB,
        clickListener: ChatAdapter.OnItemClickListener,
    ) {
        binding.message.text = message.message
        binding.name.text = message.name
        val format = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.ENGLISH)
        binding.time.text = format.format(message.time)
        binding.root.setOnLongClickListener {
            clickListener.click(message)
            true
        }
    }
}


