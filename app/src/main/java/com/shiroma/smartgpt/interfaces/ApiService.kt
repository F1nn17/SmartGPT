package com.shiroma.smartgpt.interfaces

import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class HelloResponse(val message: String)
data class RegisterRequest(val login: UUID, val password: String)
data class RegisterResponse(val token: String)
data class LoginRequest(val login: UUID, val password: String, val token: String)
data class LoginResponse(val token: String?)
data class PaymentRequest(val login: UUID, val amount: BigDecimal)
data class Chat(val id: Long, val name: String, val userId : Long, val messageIds : List<Long>)
data class Message(val id: Long, val content: String,val chatId : Long, val timestamp: String, val type: MessageType)
data class ChatRequest(val login: UUID, val name: String)
data class ChatsRequest(val login: UUID)

data class ChatRequestDTO(val type : MessageType, val message: String)
data class ChatResponseDTO(val message: String)

enum class MessageType {
    USER, AI, ERROR
}

interface ApiService {

    @POST("/api/register")
    fun register(@Body request: RegisterRequest) : Call<RegisterResponse>

    @POST("/api/login")
    fun login(@Body request: LoginRequest) : Call<LoginResponse>

    @POST("/api/payment")
    fun payment(@Body request: PaymentRequest) : Call<HelloResponse>

    @POST("/api/chats")
    suspend fun getChats(@Body request: ChatsRequest): List<Chat>

    @POST("/api/chat/create")
    suspend fun createChat(@Body chatRequest: ChatRequest): Chat

    @GET("/api/chats/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: Long): List<Message>

    @POST("/api/chats/{chatId}/messages")
    suspend fun sendMessage(@Path("chatId") chatId: Long, @Body request: ChatRequestDTO): ChatResponseDTO
}