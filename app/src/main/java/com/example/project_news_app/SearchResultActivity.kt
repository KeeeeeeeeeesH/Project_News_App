package com.example.project_news_app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_news_app.adapters.NewsAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SearchResultActivity : AppCompatActivity() {

    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private var allNewsList: List<NewsData> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)

        newsRecyclerView = findViewById(R.id.news_recycler_view)
        newsRecyclerView.layoutManager = LinearLayoutManager(this)
        newsAdapter = NewsAdapter(listOf(), NewsAdapter.NewsType.GENERAL)
        newsRecyclerView.adapter = newsAdapter

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // ดึงข้อมูลข่าวจาก API ก่อนทำการค้นหา
        loadAllNews {
            // หลังจากดึงข้อมูลเสร็จแล้ว ทำการค้นหาต่อ
            val searchType = intent.getStringExtra("SEARCH_TYPE")
            when (searchType) {
                "NAME" -> {
                    val query = intent.getStringExtra("SEARCH_QUERY") ?: return@loadAllNews
                    searchNews(query)
                }

                "DATE" -> {
                    val query = intent.getStringExtra("SEARCH_QUERY") ?: return@loadAllNews
                    searchNewsByDate(query)
                }

                "DATE_RANGE" -> {
                    val startDateStr = intent.getStringExtra("START_DATE")
                    val endDateStr = intent.getStringExtra("END_DATE")
                    if (startDateStr != null && endDateStr != null) {
                        searchNewsByDateRange(startDateStr, endDateStr)
                    }
                }
            }
        }
    }

    private fun loadAllNews(onLoaded: () -> Unit) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getAllNews().enqueue(object : Callback<List<NewsData>> {
            override fun onResponse(call: Call<List<NewsData>>, response: Response<List<NewsData>>) {
                if (response.isSuccessful) {
                    allNewsList = response.body() ?: listOf()
                    onLoaded()  // เรียกเมื่อโหลดข่าวเสร็จสิ้น
                } else {
                    Toast.makeText(this@SearchResultActivity, "Failed to load news", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<NewsData>>, t: Throwable) {
                Toast.makeText(this@SearchResultActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchNews(query: String) {
        val filteredNewsList = allNewsList.filter {
            it.newsName.contains(query, ignoreCase = true)
        }.distinctBy { it.newsId }

        fetchReadCounts(filteredNewsList) // Fetch read counts for filtered news
    }

    private fun searchNewsByDate(query: String) {
        val filteredNewsList = allNewsList.filter {
            it.dateAdded != null && isDateMatch(it.dateAdded, query)
        }.distinctBy { it.newsId }

        fetchReadCounts(filteredNewsList) // Fetch read counts for filtered news
    }

    private fun searchNewsByDateRange(startDateStr: String, endDateStr: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val startDate = dateFormat.parse(startDateStr)
        val endDate = dateFormat.parse(endDateStr)

        if (startDate == endDate) {
            Toast.makeText(this, "กรุณาเลือกวันที่แตกต่างกัน", Toast.LENGTH_SHORT).show()
            return
        }

        // ปรับ endDate ให้เป็น 23.59.59 แก้ปัญหาเรื่อง timezone
        val calendar = Calendar.getInstance()
        calendar.time = endDate
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val adjustedEndDate = calendar.time

        val filteredNewsList = allNewsList.filter {
            it.dateAdded != null && it.dateAdded.after(startDate) && it.dateAdded.before(adjustedEndDate)
        }

        newsAdapter.setNews(filteredNewsList)
    }


    private fun isDateMatch(date: Date, query: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateString = dateFormat.format(date)
        return dateString == query
    }

    private fun fetchReadCounts(newsList: List<NewsData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getTotalRead().enqueue(object : Callback<List<Total_ReadData>> {
            override fun onResponse(call: Call<List<Total_ReadData>>, response: Response<List<Total_ReadData>>) {
                if (response.isSuccessful) {
                    val readCounts = response.body() ?: listOf()
                    newsList.forEach { news ->
                        news.readCount = readCounts.count { it.newsId == news.newsId }
                    }
                    fetchRatings(newsList) // Fetch ratings after fetching read counts
                } else {
                    Toast.makeText(this@SearchResultActivity, "Failed to load read counts", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Total_ReadData>>, t: Throwable) {
                Toast.makeText(this@SearchResultActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchRatings(newsList: List<NewsData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getNewsRating().enqueue(object : Callback<List<News_RatingData>> {
            override fun onResponse(call: Call<List<News_RatingData>>, response: Response<List<News_RatingData>>) {
                if (response.isSuccessful) {
                    val ratings = response.body() ?: listOf()
                    newsList.forEach { news ->
                        val newsRatings = ratings.filter { it.newsId == news.newsId }
                        news.ratingScore = if (newsRatings.isNotEmpty()) {
                            newsRatings.sumByDouble { it.ratingScore.toDouble() }.toFloat() / newsRatings.size
                        } else {
                            0f
                        }
                    }
                    fetchCoverImages(newsList)
                } else {
                    Toast.makeText(this@SearchResultActivity, "Failed to load ratings", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<News_RatingData>>, t: Throwable) {
                Toast.makeText(this@SearchResultActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCoverImages(newsList: List<NewsData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        newsList.forEach { news ->
            apiService.getCoverImage(news.newsId).enqueue(object : Callback<List<PictureData>> {
                override fun onResponse(call: Call<List<PictureData>>, response: Response<List<PictureData>>) {
                    if (response.isSuccessful) {
                        val pictures = response.body() ?: listOf()
                        val coverImage = pictures.find { it.pictureName.startsWith("cover_") }
                        news.coverImageUrl = coverImage?.let { "${RetrofitClient.getClient(this@SearchResultActivity).baseUrl()}uploads/${it.pictureName}" } ?: ""
                        newsAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@SearchResultActivity, "Failed to load cover images", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<PictureData>>, t: Throwable) {
                    Toast.makeText(this@SearchResultActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
        newsAdapter.setNews(newsList) // Refresh list after fetching all data
    }
}
