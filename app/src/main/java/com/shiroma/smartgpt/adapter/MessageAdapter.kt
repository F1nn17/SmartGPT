package com.shiroma.smartgpt.adapter

import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shiroma.smartgpt.interfaces.Message
import com.shiroma.smartgpt.interfaces.MessageType

class MessageAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(val messageTextView: TextView) : RecyclerView.ViewHolder(messageTextView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val messageTextView = TextView(parent.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8) // Отступы между сообщениями
            }
            setPadding(16, 8, 16, 8) // Отступы внутри текста
            textSize = 16f
        }
        return MessageViewHolder(messageTextView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        // Устанавливаем текст
        holder.messageTextView.text = message.content

        // Устанавливаем фон в зависимости от типа сообщения
        val background = GradientDrawable().apply {
            cornerRadius = 16f
            when (message.type) {
                MessageType.USER -> {
                    setColor(0xFFE0F7FA.toInt()) // Голубой
                    holder.messageTextView.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                    (holder.messageTextView.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.END // Выравнивание вправо
                }
                MessageType.AI -> {
                    setColor(0xFFFFEBEE.toInt()) // Розовый
                    holder.messageTextView.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                    (holder.messageTextView.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.START // Выравнивание влево
                }
                MessageType.ERROR -> {
                    setColor(0xFFFFFFE0.toInt()) // Жёлтый
                    holder.messageTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    (holder.messageTextView.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.CENTER // Центр
                }
            }
        }
        holder.messageTextView.background = background
        // Устанавливаем чёрный и яркий текст
        holder.messageTextView.setTextColor(0xFF000000.toInt()) // Чёрный цвет текста
    }

    override fun getItemCount(): Int = messages.size
}
