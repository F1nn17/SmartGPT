package com.shiroma.smartgpt.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shiroma.smartgpt.interfaces.Chat

class ChatListAdapter(
    private val chatList: MutableList<Chat>,
    private val onChatSelected: (Chat) -> Unit // Коллбэк для выбора чата
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatNameTextView: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.chatNameTextView.text = chat.name // Устанавливаем имя чата
        holder.itemView.setOnClickListener {
            onChatSelected(chat) // Вызываем коллбэк при клике
        }
    }

    fun getCount() : Int {
        return chatList.size
    }

    override fun getItemCount(): Int = chatList.size

    // Метод для обновления данных в адаптере
    @SuppressLint("NotifyDataSetChanged")
    fun updateChatList(newChatList: List<Chat>) {
        chatList.clear()
        chatList.addAll(newChatList)
        notifyDataSetChanged() // Сообщить адаптеру, что данные изменились
    }
}