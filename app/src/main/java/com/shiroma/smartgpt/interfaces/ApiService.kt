package com.shiroma.smartgpt.interfaces

import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.*

data class HelloResponse(val message: String)
data class RegisterRequest(val login: UUID, val password: String)
data class RegisterResponse(val token: String)
data class LoginRequest(val login: UUID, val password: String, val token: String)
data class LoginResponse(val token: String?)

interface ApiService {
    @GET("/api/hello")
    fun getHello(): Call<HelloResponse>

    @POST("/api/register")
    fun register(@Body request: RegisterRequest) : Call<RegisterResponse>

    @POST("/api/login")
    fun login(@Body request: LoginRequest) : Call<LoginResponse>
}