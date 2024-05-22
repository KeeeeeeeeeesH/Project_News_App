package com.example.project_news_app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        // Example GET request to fetch news data
        apiService.getNews().enqueue(object : Callback<List<News>> {
            override fun onResponse(call: Call<List<News>>, response: Response<List<News>>) {
                if (response.isSuccessful) {
                    val newsList = response.body()
                    // Handle the news list data
                    Log.d("MainActivity", "News List: $newsList")
                }
            }

            override fun onFailure(call: Call<List<News>>, t: Throwable) {
                Log.e("MainActivity", "Error: ${t.message}")
            }
        })
    }
}
