package com.shiroma.smartgpt

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import android.widget.TextView
import android.widget.Toast
import com.shiroma.smartgpt.helpers.SslHelper
import com.shiroma.smartgpt.interfaces.HelloResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this).apply {
            textSize = 18f
            setPadding(50, 50, 50, 50)
        }
        setContentView(textView)

        val api = SslHelper(this).getRetrofit()
        // Запрос к серверу через Retrofit
        api.getHello().enqueue(object : Callback<HelloResponse> {
            override fun onResponse(call: Call<HelloResponse>, response: Response<HelloResponse>) {
                if (response.isSuccessful) {
                    val message = response.body()?.message
                    textView.text = message
                } else {
                    Log.d("ERROR", "CODE SERVER: ${response.code()}")
                    Toast.makeText(this@MainActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<HelloResponse>, t: Throwable) {
                Log.d("FAILED", "onFailure: ${t.message}")
                Toast.makeText(this@MainActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
