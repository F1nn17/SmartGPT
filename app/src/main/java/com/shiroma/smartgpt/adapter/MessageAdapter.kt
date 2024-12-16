package com.shiroma.smartgpt.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shiroma.smartgpt.interfaces.Message
import com.shiroma.smartgpt.interfaces.MessageType

class MessageAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message.content

        // Изменяем стиль текста в зависимости от типа сообщения
        holder.messageTextView.textAlignment = when (message.type) {
            MessageType.USER -> View.TEXT_ALIGNMENT_VIEW_END
            MessageType.AI -> View.TEXT_ALIGNMENT_VIEW_START
            MessageType.ERROR -> View.TEXT_ALIGNMENT_CENTER
        }
    }

    override fun getItemCount(): Int = messages.size
}
