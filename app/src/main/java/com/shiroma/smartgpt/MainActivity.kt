package com.shiroma.smartgpt

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.shiroma.smartgpt.helpers.SslHelper
import com.shiroma.smartgpt.interfaces.Chat
import com.shiroma.smartgpt.interfaces.ChatRequest
import com.shiroma.smartgpt.interfaces.ChatsRequest
import com.shiroma.smartgpt.view.ChatView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : ComponentActivity() {

    private val chatList = mutableStateListOf<Chat>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Загрузка списка чатов с сервера
        loadChatsFromServer()
        setContent {
            createSideBar(chatList) { chat ->
                openChatView(chat) // Передаём функцию открытия чата
            }
        }
    }
    private var isCreatingChat = false
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun createSideBar(chatList: SnapshotStateList<Chat>, openChat: (Chat) -> ChatView) {
        FrameLayout(this).apply {
            setContent {
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val selectedChat = mutableStateOf(chatList.firstOrNull()?.name ?: "Нет чатов")
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Column {
                                // Заголовок
                                Text(
                                    text = "Чаты",
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(16.dp)
                                )

                                // Список чатов
                                chatList.forEach { chat ->
                                    NavigationDrawerItem(
                                        label = { Text(chat.name ?: "Це Фантом", fontSize = 20.sp) },
                                        selected = selectedChat.value == chat.name,
                                        onClick = {
                                            scope.launch {
                                                selectedChat.value = chat.name
                                                openChat(chat) // Открываем выбранный чат в основном контенте
                                                drawerState.close()
                                            }
                                        },
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                                    )
                                }

                                // Кнопка для создания нового чата
                                Button(
                                    onClick = {
                                        if (!isCreatingChat) {
                                            isCreatingChat = true
                                            createNewChat(scope, drawerState)
                                        }
                                    },
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("Создать новый чат")
                                }
                            }
                        }
                    },
                    content = {
                        // Основное содержимое — открываемый чат
                        val chat = chatList.find { it.name == selectedChat.value }
                        if (chat != null) {
                            AndroidView(
                                factory = { openChatView(chat) },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Выберите или создайте чат", fontSize = 20.sp)
                            }
                        }
                    }
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openChatView(chat: Chat) : ChatView {
        Log.d("OPEN CHAT", "open chatId: ${chat.id} ")
        return ChatView(this, chat.id).apply {
            setChatName(chat.name ?: "Нет имени")
            loadMessageFromServer() // Загрузка сообщений для выбранного чата
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNewChat(scope: CoroutineScope, drawerState: DrawerState) {
        val chatName = "Новый чат ${chatList.size + 1}" // Пример имени
        val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = SslHelper(baseContext).getRetrofit()
                val response = api.createChat(
                    ChatRequest(
                        UUID.fromString(sharedPref.getString("login", null)),
                        chatName
                    )
                )
                withContext(Dispatchers.Main) {
                    if (!chatList.any { it.id == response.id }) { // Предотвращаем дублирование
                        chatList.add(response)
                    }
                    // Закрываем DrawerState в контексте Compose
                    scope.launch {
                        drawerState.close()
                        isCreatingChat = false
                    }
                }

            } catch (e: Exception) {
                showError("Error createNewChat: ${e.message}")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadChatsFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                val api = SslHelper(baseContext).getRetrofit()
                val response = api.getChats(
                    ChatsRequest(UUID.fromString(sharedPref.getString("login", null)))
                )
                withContext(Dispatchers.Main) {
                    chatList.clear()
                    chatList.addAll(response)
                }

            } catch (e: Exception) {
                showError("Error loadChatsFromServer: ${e.message}")
            }
        }
    }

    private suspend fun showError(message: String) {
        Log.d("ERROR", "Error: $message")
        withContext(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

}
