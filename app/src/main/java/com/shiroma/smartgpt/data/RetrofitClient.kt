package com.shiroma.smartgpt.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.shiroma.smartgpt.interfaces.ApiService
import com.shiroma.smartgpt.interfaces.Chat
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import java.lang.reflect.Type

@SuppressLint("StaticFieldLeak")
class RetrofitClient(private val context: Context,
                     sslContext: SSLContext,
                     trustManager: X509TrustManager
) {
    private val BASE_URL = "https://192.168.0.100:8443" // Замените на URL вашего сервера

    // Получение токена из SharedPreferences
    private fun getAuthToken(): String? {
        val sharedPref = context.getSharedPreferences("AppPreferences", MODE_PRIVATE)
        return sharedPref.getString("auth_token", null)
    }

    // Добавление токена в заголовки
    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        val token = getAuthToken()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token") // Добавляем токен
        }
        chain.proceed(requestBuilder.build())
    }
    private val gson = GsonBuilder()
        .registerTypeAdapter(
            object : TypeToken<List<Chat>>() {}.type,
            ListChatDeserializer()
        )
        .create()

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .sslSocketFactory(sslContext.socketFactory, trustManager)
        .hostnameVerifier { _, _ -> true }
        .build()


    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}

class ListChatDeserializer : JsonDeserializer<List<Chat>> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): List<Chat> {
        val listType = object : TypeToken<List<Chat>>() {}.type
        return context!!.deserialize(json, listType)
    }
}