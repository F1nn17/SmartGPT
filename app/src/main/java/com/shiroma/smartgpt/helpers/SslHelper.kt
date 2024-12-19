package com.shiroma.smartgpt.helpers

import android.content.Context
import com.shiroma.smartgpt.R
import com.shiroma.smartgpt.data.RetrofitClient
import com.shiroma.smartgpt.interfaces.ApiService
import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import java.security.SecureRandom
import javax.net.ssl.TrustManagerFactory

class SslHelper(private val context: Context) {
    fun getRetrofit(): ApiService {
        val p12InputStream: InputStream = context.resources.openRawResource(R.raw.keystore)
        val password = "Lena2000".toCharArray()

        // Загружаем .p12 файл в KeyStore
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(p12InputStream, password)

        // Извлекаем сертификат и ключ
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, password)

        // Инициализация TrustManagerFactory (если вы хотите использовать тот же сертификат для проверки доверия)
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        // Получаем KeyManagers и TrustManagers
        val keyManagers = keyManagerFactory.keyManagers
        val trustManagers = trustManagerFactory.trustManagers

        // Убедимся, что получаем X509TrustManager
        val trustManager = trustManagers.first { it is X509TrustManager } as X509TrustManager

        // Настройка SSLContext для подключения через SSL
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(keyManagers, arrayOf(trustManager), SecureRandom())


        // Создаем Retrofit клиент с настроенным OkHttpClient
        return RetrofitClient(sslContext, trustManager).api

    }
}