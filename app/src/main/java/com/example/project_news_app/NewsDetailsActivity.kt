package com.example.project_news_app

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.project_news_app.NewsData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsDetailsActivity : AppCompatActivity() {

    private lateinit var newsTitle: TextView
    private lateinit var newsCategory: TextView
    private lateinit var newsMajorLevel: TextView
    private lateinit var newsDate: TextView
    private lateinit var newsReadCount: TextView
    private lateinit var newsRating: TextView
    private lateinit var newsImage: ImageView
    private lateinit var newsDetails: TextView
    private lateinit var newsSubCategory: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        newsTitle = findViewById(R.id.news_name)
        newsCategory = findViewById(R.id.cat_name)
        newsMajorLevel = findViewById(R.id.major_level)
        newsDate = findViewById(R.id.date_added)
        newsReadCount = findViewById(R.id.news_read_count)
        newsRating = findViewById(R.id.rating_score)
        newsImage = findViewById(R.id.news_picture)
        newsDetails = findViewById(R.id.news_details)
        newsSubCategory = findViewById(R.id.news_sub_category)

        val newsId = intent.getIntExtra("news_id", -1)
        if (newsId != -1) {
            fetchNewsDetails(newsId)
        }
    }

    private fun fetchNewsDetails(newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getNewsById(newsId).enqueue(object : Callback<NewsData> {
            override fun onResponse(call: Call<NewsData>, response: Response<NewsData>) {
                if (response.isSuccessful) {
                    response.body()?.let { news ->
                        displayNewsDetails(news)
                    }
                }
            }

            override fun onFailure(call: Call<NewsData>, t: Throwable) {
                // Handle failure
            }
        })
    }

    private fun displayNewsDetails(news: NewsData) {
        newsTitle.text = news.newsName
        newsCategory.text = "หมวดหมู่: ${news.catId}"
        newsMajorLevel.text = "ระดับความสำคัญ: ${news.majorId}"
        newsDate.text = news.dateAdded.toString()
        newsReadCount.text = "อ่าน ${news.readCount} ครั้ง"
        newsRating.text = "★ ${news.ratingScore}"
        newsDetails.text = news.newsDetails
        newsSubCategory.text = "แท็กข่าว: ${news.catId}" // Update this to display actual subcategories if needed

        // Load image if available
    }
}
