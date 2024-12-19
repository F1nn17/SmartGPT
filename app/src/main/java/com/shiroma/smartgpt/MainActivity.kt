package com.shiroma.smartgpt

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.shiroma.smartgpt.helpers.SslHelper
import com.shiroma.smartgpt.interfaces.Chat
import com.shiroma.smartgpt.interfaces.ChatCreate
import com.shiroma.smartgpt.interfaces.ChatDelete
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val chatList = mutableListOf<Chat>() // Список чатов
    private lateinit var chatContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        loadChatsFromServer()
    }

    private fun setupUI() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Плашка "Выберите чат"
        val headerTextView = TextView(this).apply {
            text = "Выберите чат"
            textSize = 24f
            setTextColor(resources.getColor(android.R.color.black)) // Чёрный текст
            setBackgroundColor(resources.getColor(android.R.color.darker_gray)) // Серый фон
            setPadding(16, 16, 16, 16) // Отступы
            gravity = Gravity.CENTER // Центрирование текста
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        chatContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f // Растягиваем контейнер на всё свободное пространство
            )
        }

        val scrollView = ScrollView(this).apply {
            addView(chatContainer)
        }

        val createChatButton = Button(this).apply {
            text = "Создать чат"
            setBackgroundColor(resources.getColor(android.R.color.holo_green_dark)) // Зелёный цвет
            setTextColor(resources.getColor(android.R.color.white)) // Белый текст
            setOnClickListener { createChat() }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16) // Отступы
            }
        }

        mainLayout.addView(headerTextView) // Добавляем плашку
        mainLayout.addView(scrollView) // Контейнер для чатов
        mainLayout.addView(createChatButton) // Кнопка внизу

        setContentView(mainLayout)
    }

    private fun loadChatsFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = SslHelper(baseContext).getRetrofit()
                val response = api.getChats()
                withContext(Dispatchers.Main) {
                    chatList.clear()
                    chatList.addAll(response)
                    updateChatList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateChatList() {
        chatContainer.removeAllViews()
        chatList.forEach { chat ->
            val chatItem = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 8) // Отступы между чатами
                }
            }

            val chatNameButton = Button(this@MainActivity).apply {
                text = chat.name
                setBackgroundColor(resources.getColor(android.R.color.transparent)) // Прозрачный фон
                setTextColor(resources.getColor(android.R.color.black)) // Чёрный текст
                setOnClickListener { openChat(chat) }
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f // Растягиваем на всё доступное пространство
                )
            }

            val deleteButton = Button(this@MainActivity).apply {
                text = "Удалить"
                setBackgroundColor(resources.getColor(android.R.color.holo_red_dark)) // Красный цвет
                setTextColor(resources.getColor(android.R.color.white)) // Белый текст
                setOnClickListener { deleteChat(chat) }
            }

            chatItem.addView(chatNameButton)
            chatItem.addView(deleteButton)

            chatContainer.addView(chatItem)
        }
    }

    private fun createChat() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = SslHelper(baseContext).getRetrofit()
                val newChat = api.createChat(chatRequest = ChatCreate("Новый чат ${chatList.size + 1}"))
                withContext(Dispatchers.Main) {
                    chatList.add(newChat)
                    updateChatList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun deleteChat(chat: Chat) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = SslHelper(baseContext).getRetrofit()
                api.deleteChat(chatDelete = ChatDelete(chat.id))
                withContext(Dispatchers.Main) {
                    chatList.remove(chat)
                    updateChatList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun openChat(chat: Chat) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("chat_id", chat.id)
            putExtra("chat_name", chat.name)
        }
        startActivity(intent)
    }
}

