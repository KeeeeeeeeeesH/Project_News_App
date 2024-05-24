package com.example.project_news_app

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
//  private const val BASE_URL = "http://10.3.58.145:5000/"
    private const val BASE_URL = "http://192.168.0.177:5000/"
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val instance: Retrofit by lazy {
        retrofit
    }
}
