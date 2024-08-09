package com.example.project_news_app

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_news_app.adapters.NewsAdapter
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Timestamp

class VisitHistoryActivity : AppCompatActivity() {

    private lateinit var visitHistoryRecyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private var memId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_history)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        visitHistoryRecyclerView = findViewById(R.id.visit_history_recycler_view)
        visitHistoryRecyclerView.layoutManager = LinearLayoutManager(this)

        newsAdapter = NewsAdapter(listOf(), isVisitHistory = true)
        visitHistoryRecyclerView.adapter = newsAdapter

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        memId = sharedPreferences.getInt("memId", -1)

        if (memId != -1) {
            fetchReadHistory()
        } else {
            Toast.makeText(this, "ไม่พบข้อมูลสมาชิก", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchReadHistory() {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getReadHistoryByMemId(memId).enqueue(object : Callback<List<Read_HistoryData>> {
            override fun onResponse(call: Call<List<Read_HistoryData>>, response: Response<List<Read_HistoryData>>) {
                if (response.isSuccessful) {
                    val readHistoryList = response.body() ?: emptyList()
                    fetchNewsData(readHistoryList)
                } else {
                    Toast.makeText(this@VisitHistoryActivity, "ไม่สามารถดึงประวัติการอ่านได้", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Read_HistoryData>>, t: Throwable) {
                Toast.makeText(this@VisitHistoryActivity, "เกิดข้อผิดพลาดในการดึงประวัติการอ่าน: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchNewsData(readHistoryList: List<Read_HistoryData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getAllNews().enqueue(object : Callback<List<NewsData>> {
            override fun onResponse(call: Call<List<NewsData>>, response: Response<List<NewsData>>) {
                if (response.isSuccessful) {
                    val newsList = response.body() ?: emptyList()
                    val readHistoryWithNewsList = readHistoryList.mapNotNull { readHistory ->
                        val news = newsList.find { it.newsId == readHistory.newsId }
                        news?.let {
                            ReadHistoryWithNewsData(
                                newsId = it.newsId,
                                readDate = readHistory.readDate,
                                newsName = it.newsName,
                                ratingScore = it.ratingScore,
                                coverImage = it.coverImageUrl ?: "",
                                readCount = it.readCount
                            )
                        }
                    }
                    fetchReadCounts(readHistoryWithNewsList) // เรียก fetchReadCounts ต่อ
                } else {
                    Toast.makeText(this@VisitHistoryActivity, "ไม่สามารถดึงข้อมูลข่าวได้", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<NewsData>>, t: Throwable) {
                Toast.makeText(this@VisitHistoryActivity, "เกิดข้อผิดพลาดในการดึงข้อมูลข่าว: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchReadCounts(readHistoryWithNewsList: List<ReadHistoryWithNewsData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getTotalRead().enqueue(object : Callback<List<Total_ReadData>> {
            override fun onResponse(call: Call<List<Total_ReadData>>, response: Response<List<Total_ReadData>>) {
                if (response.isSuccessful) {
                    val readCounts = response.body() ?: listOf()
                    readHistoryWithNewsList.forEach { news ->
                        news.readCount = readCounts.count { it.newsId == news.newsId }
                    }
                    fetchRatings(readHistoryWithNewsList)
                } else {
                    Toast.makeText(this@VisitHistoryActivity, "Failed to load read counts", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Total_ReadData>>, t: Throwable) {
                Toast.makeText(this@VisitHistoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchRatings(readHistoryWithNewsList: List<ReadHistoryWithNewsData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getNewsRating().enqueue(object : Callback<List<News_RatingData>> {
            override fun onResponse(call: Call<List<News_RatingData>>, response: Response<List<News_RatingData>>) {
                if (response.isSuccessful) {
                    val ratings = response.body() ?: listOf()
                    readHistoryWithNewsList.forEach { news ->
                        val newsRatings = ratings.filter { it.newsId == news.newsId }
                        news.ratingScore = if (newsRatings.isNotEmpty()) {
                            newsRatings.sumByDouble { it.ratingScore.toDouble() }.toFloat() / newsRatings.size
                        } else {
                            0f
                        }
                    }
                    fetchCoverImages(readHistoryWithNewsList)
                } else {
                    Toast.makeText(this@VisitHistoryActivity, "Failed to load ratings", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<News_RatingData>>, t: Throwable) {
                Toast.makeText(this@VisitHistoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCoverImages(readHistoryWithNewsList: List<ReadHistoryWithNewsData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        readHistoryWithNewsList.forEach { news ->
            apiService.getCoverImage(news.newsId).enqueue(object : Callback<List<PictureData>> {
                override fun onResponse(call: Call<List<PictureData>>, response: Response<List<PictureData>>) {
                    if (response.isSuccessful) {
                        val pictures = response.body() ?: listOf()
                        val coverImage = pictures.find { it.pictureName.startsWith("cover_") }
                        news.coverImage = coverImage?.let { "${RetrofitClient.getClient(this@VisitHistoryActivity).baseUrl()}uploads/${it.pictureName}" } ?: ""
                        newsAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@VisitHistoryActivity, "Failed to load cover images", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<PictureData>>, t: Throwable) {
                    Toast.makeText(this@VisitHistoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
        newsAdapter.setNews(readHistoryWithNewsList) // Add this line
    }

    // Function to delete read history
    fun deleteReadHistory(memId: Int, newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val call = apiService.deleteReadHistory(memId, newsId)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@VisitHistoryActivity, "ลบประวัติการอ่านเรียบร้อย", Toast.LENGTH_SHORT).show()
                    fetchReadHistory()
                } else {
                    Toast.makeText(this@VisitHistoryActivity, "ไม่สามารถลบประวัติการอ่านได้", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@VisitHistoryActivity, "เกิดข้อผิดพลาดในการลบประวัติการอ่าน: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}


