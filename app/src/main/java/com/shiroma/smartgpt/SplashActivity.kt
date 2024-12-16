package com.shiroma.smartgpt

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.Intent
import android.widget.Toast
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random
import com.shiroma.smartgpt.helpers.SslHelper
import com.shiroma.smartgpt.interfaces.LoginRequest
import com.shiroma.smartgpt.interfaces.PaymentRequest
import com.shiroma.smartgpt.interfaces.RegisterRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Создаём корневой контейнер
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.WHITE) // Задаём белый фон
        }

        // Добавляем ProgressBar
        val progressBar = ProgressBar(this).apply {
            isIndeterminate = true
        }
        rootLayout.addView(progressBar)

        // Устанавливаем корневой контейнер как layout
        setContentView(rootLayout)

        // Проверяем, сохранён ли логин и токен
        val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val savedLogin = sharedPref.getString("login", null)
        val savedPassword = sharedPref.getString("password", null)
        val savedToken = sharedPref.getString("auth_token", null)

        CoroutineScope(Dispatchers.IO).launch {
            if (!savedLogin.isNullOrEmpty() && !savedToken.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
                // Если логин и токен есть, выполняем запрос на вход
                loginUser(sharedPref, UUID.fromString(savedLogin), savedPassword, savedToken)
            } else {
                // Если логина нет, выполняем регистрацию
                registerUser(sharedPref)
            }
        }

    }

    private suspend fun loginUser(
        sharedPref: android.content.SharedPreferences,
        login: UUID,
        password: String,
        token: String
    ) {
        try {
            val api = SslHelper(baseContext).getRetrofit()
            val response = api.login(LoginRequest(login, password, token)).execute()// Метод логина в API
            if (response.isSuccessful) {
                // Переход к основной активности
                withContext(Dispatchers.Main) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            } else if (response.code() == 418) {
                val authToken = response.body()?.token
                sharedPref.edit().apply {
                    putString("auth_token", authToken)
                    apply()
                }
                loginUser(sharedPref, login, password, token)
            } else {
                showError("Login failed: ${response.code()}. Re-registering...")
                registerUser(sharedPref) // Если вход не удался, повторяем регистрацию
            }
        } catch (e: Exception) {
            showError("Error: ${e.message}")
        }
    }

    private suspend fun registerUser(sharedPref: android.content.SharedPreferences) {
        // Генерация UUID и пароля
        val login = UUID.randomUUID()
        val password = UUID.randomUUID().toString().take(8) // Случайный пароль длиной 8 символов

        // Отправка данных на сервер
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = SslHelper(baseContext).getRetrofit()
                val request = RegisterRequest(login, password)
                val response = api.register(request).execute()
                if (response.isSuccessful) {
                    val authToken = response.body()?.token
                    if (!authToken.isNullOrEmpty()) {
                        // Сохранение токена
                        // Сохранение логина и токена
                        sharedPref.edit().apply {
                            putString("login", login.toString())
                            putString("password", password)
                            putString("auth_token", authToken)
                            apply()
                        }
                        Log.d("TOKEN", "onCreate TOKEN: $authToken")
                        //производим оплату
                        val responsePayment = api.payment(
                            PaymentRequest(
                                UUID.fromString(sharedPref.getString("login", null)),
                                randomBigDecimal()
                            )
                        ).execute()
                        if (responsePayment.isSuccessful) {
                            // Переход к основной активности
                            withContext(Dispatchers.Main) {
                                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                finish()
                            }
                        }
                    } else {
                        showError("Failed to get auth token.")
                    }
                } else {
                    showError("Registration failed: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            }
        }
    }

    private fun randomBigDecimal(): BigDecimal {
        val randomValue = Random.nextLong(1, 1_000) // Рандомное число от 1 до 1 000
        val randomDecimalPart = Random.nextInt(0, 100) // Дробная часть
        return BigDecimal("$randomValue.$randomDecimalPart").setScale(2, RoundingMode.HALF_UP)
    }


    private suspend fun showError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@SplashActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}