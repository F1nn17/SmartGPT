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
class RetrofitClient(sslContext: SSLContext,
                     trustManager: X509TrustManager
) {
    private val BASE_URL = "https://192.168.200.208:8443" // Замените на URL вашего сервера


    private val gson = GsonBuilder()
        .registerTypeAdapter(
            object : TypeToken<List<Chat>>() {}.type,
            ListChatDeserializer()
        )
        .create()

    private val client = OkHttpClient.Builder()
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