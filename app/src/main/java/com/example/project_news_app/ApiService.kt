package com.example.project_news_app

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

interface ApiService {
    @GET("api/news")
    fun getNews(): Call<List<News>>

    @POST("api/news")
    fun postNews(@Body news: News): Call<News>
}
