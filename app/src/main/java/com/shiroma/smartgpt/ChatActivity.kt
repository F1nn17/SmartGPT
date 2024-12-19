package com.shiroma.smartgpt

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shiroma.smartgpt.adapter.MessageAdapter
import com.shiroma.smartgpt.helpers.SslHelper
import com.shiroma.smartgpt.interfaces.ChatRequestDTO
import com.shiroma.smartgpt.interfaces.Message
import com.shiroma.smartgpt.interfaces.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime


class ChatActivity : ComponentActivity() {
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private var chatId: Long = -1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatId = intent.getLongExtra("chat_id", -1)
        val chatName = intent.getStringExtra("chat_name") ?: "Чат"
        // Инициализация адаптера
        messageAdapter = MessageAdapter(messages)
        setupUI(chatName)
        loadMessagesFromServer()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUI(chatName: String) {

        val menuButton = Button(this).apply {
            text = "Меню"
            setOnClickListener { finish() }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.START
            }
        }

        val recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
            addItemDecoration(MessageAdapter.MessageItemDecoration(16)) // Устанавливаем отступы в пикселях
        }

        val messageInput = EditText(this).apply {
            hint = "Введите сообщение"
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // Растягиваем на оставшееся пространство
            ).apply {
                setMargins(0, 0, 16, 64) // Отступ справа и снизу
            }
        }

        val sendButton = Button(this).apply {
            text = "Отправить"
            setBackgroundColor(resources.getColor(android.R.color.holo_green_dark)) // Зелёный цвет
            setTextColor(resources.getColor(android.R.color.white)) // Белый текст
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 0, 16, 64) // Отступы по бокам и снизу
            }
            setOnClickListener {
                val content = messageInput.text.toString()
                messageInput.text = null
                val timestamp = LocalDateTime.now().toString()
                messages.add(Message((messages.size+1).toLong(), content, chatId, timestamp, MessageType.USER))
                messageAdapter.notifyItemInserted(messages.size - 1)
                sendMessage(content)
            }
        }

        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(messageInput)
            addView(sendButton)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            addView(menuButton)
            addView(recyclerView, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f // Растягиваем на всё оставшееся пространство
            ))
            addView(inputLayout, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }

        setContentView(mainLayout)
    }


    private fun loadMessagesFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = SslHelper(baseContext).getRetrofit()
                val response = api.getMessages(chatId)
                withContext(Dispatchers.Main) {
                    messages.clear()
                    messages.addAll(response)
                    messageAdapter.notifyDataSetChanged() // Уведомляем адаптер об изменениях
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendMessage(content: String) {
        if (content.isBlank()) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = SslHelper(baseContext).getRetrofit()
                val newMessage = api.sendMessage(chatId, ChatRequestDTO(MessageType.USER, content))
                withContext(Dispatchers.Main) {
                    messages.add(newMessage)
                    messageAdapter.notifyItemInserted(messages.size - 1) // Уведомляем адаптер об изменении
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}