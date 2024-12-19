package com.shiroma.smartgpt.interfaces

import retrofit2.http.GET
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class Chat(val id: Long, val name: String, val messageIds : List<Long>)
data class Message(val id: Long, val content: String,val chatId : Long, val timestamp: String, val type: MessageType)
data class ChatCreate(val name: String)
data class ChatDelete(val chatId: Long)
data class ChatRequestDTO(val type : MessageType, val message: String)

enum class MessageType {
    USER, AI, ERROR
}

interface ApiService {

    @GET("/api/chats")
    suspend fun getChats(): List<Chat>

    @POST("/api/chat/create")
    suspend fun createChat(@Body chatRequest: ChatCreate): Chat

    @POST("/api/chat/delete")
    suspend fun deleteChat(@Body chatDelete: ChatDelete) : Response<Void>

    @GET("/api/chats/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: Long): List<Message>

    @POST("/api/chats/{chatId}/messages")
    suspend fun sendMessage(@Path("chatId") chatId: Long, @Body request: ChatRequestDTO): Message
}