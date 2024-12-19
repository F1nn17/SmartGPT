package com.shiroma.smartgpt.adapter

import android.graphics.Rect
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
                FrameLayout.LayoutParams.MATCH_PARENT,
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

        // Устанавливаем текст сообщения
        holder.messageTextView.text = message.content

        // Устанавливаем фон в зависимости от типа сообщения
        val background = GradientDrawable().apply {
            cornerRadius = 16f
            when (message.type) {
                MessageType.USER -> {
                    setColor(0xFFE0F7FA.toInt()) // Голубой фон
                    holder.messageTextView.gravity = Gravity.END // Текст вправо
                }
                MessageType.AI -> {
                    setColor(0xFFFFEBEE.toInt()) // Розовый фон
                    holder.messageTextView.gravity = Gravity.START // Текст влево
                }
                MessageType.ERROR -> {
                    setColor(0xFFFFFFE0.toInt()) // Жёлтый фон
                    holder.messageTextView.gravity = Gravity.CENTER // Центр
                }
            }
        }
        holder.messageTextView.background = background

        // Устанавливаем чёрный цвет текста
        holder.messageTextView.setTextColor(0xFF000000.toInt())
    }

    class MessageItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) return

            val message = (parent.adapter as MessageAdapter).messages[position]

            when (message.type) {
                MessageType.USER -> outRect.set(space, space, 0, space) // Отступы слева
                MessageType.AI -> outRect.set(0, space, space, space) // Отступы справа
                MessageType.ERROR -> outRect.set(space, space, space, space) // Отступы со всех сторон
            }
        }
    }



    override fun getItemCount(): Int = messages.size
}
