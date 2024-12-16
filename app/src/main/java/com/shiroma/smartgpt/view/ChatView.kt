package com.shiroma.smartgpt.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.Gravity
import android.widget.*
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

@SuppressLint("ViewConstructor")
@RequiresApi(Build.VERSION_CODES.O)
class ChatView(context: Context, private val chatId: Long) : LinearLayout(context) {
    private val chatNameTextView: TextView
    private val messageRecyclerView: RecyclerView
    private val messageInput: EditText
    private val sendButton: Button
    private val messageList = mutableListOf<Message>() // Список сообщений
    private val messageAdapter: MessageAdapter

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )

        // Заголовок чата
        chatNameTextView = TextView(context).apply {
            textSize = 20f
            gravity = Gravity.CENTER
        }
        addView(chatNameTextView)

        // RecyclerView для сообщений
        messageRecyclerView = RecyclerView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            layoutManager = LinearLayoutManager(context)
        }
        messageAdapter = MessageAdapter(messageList)
        messageRecyclerView.adapter = messageAdapter
        addView(messageRecyclerView)

        // Поле ввода и кнопка отправки
        val inputLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }

        messageInput = EditText(context).apply {
            layoutParams = LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            )
            hint = "Введите сообщение..."
        }
        inputLayout.addView(messageInput)

        sendButton = Button(context).apply {
            text = "Отправить"
        }
        inputLayout.addView(sendButton)

        addView(inputLayout)

        // Обработчик нажатия на кнопку отправки
        sendButton.setOnClickListener {
            val userMessage = messageInput.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                sendMessage(userMessage)
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun loadMessageFromServer(){
        CoroutineScope(Dispatchers.IO).launch {
            val api = SslHelper(context).getRetrofit()
            val response = api.getMessages(chatId)
            if (response.isNotEmpty()){
                withContext(Dispatchers.Main) {
                    messageList.clear()
                    messageList.addAll(response)
                    messageAdapter.notifyDataSetChanged()
                    messageRecyclerView.scrollToPosition(messageList.size - 1) // Скроллим к последнему сообщению
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun setChatName(chatName: String) {
        chatNameTextView.text = "Чат: $chatName"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessage(userMessage: String) {
        // Добавляем сообщение от пользователя в список
        messageList.add(Message(messageList.size.toLong()+1,userMessage, chatId,LocalDateTime.now().toString(), MessageType.USER))
        messageAdapter.notifyItemInserted(messageList.size - 1)
        messageRecyclerView.scrollToPosition(messageList.size - 1)

        messageInput.text.clear()

        // Отправляем сообщение на сервер для ответа
        fetchOpenAIResponse(userMessage)
    }

    private fun fetchOpenAIResponse(userMessage: String) {
        CoroutineScope(Dispatchers.Main).launch {
            // Здесь будет запрос к серверу OpenAI
            val api = SslHelper(context).getRetrofit()
            val response = api.sendMessage(chatId, ChatRequestDTO(MessageType.USER, userMessage))
            val aiMessage = response.message
            messageList.add(Message(messageList.size.toLong()+1, aiMessage,chatId, LocalDateTime.now().toString(),MessageType.AI))
            messageAdapter.notifyItemInserted(messageList.size - 1)
            messageRecyclerView.scrollToPosition(messageList.size - 1)
        }
    }
}
