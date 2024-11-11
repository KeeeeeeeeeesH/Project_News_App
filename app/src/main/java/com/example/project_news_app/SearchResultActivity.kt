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

        //set toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // ดึงข้อมูลข่าวจาก API ก่อนทำการค้นหา
        loadAllNews {
            // รับค่ารูปแบบการค้นหา
            val searchType = intent.getStringExtra("SEARCH_TYPE")
            // ค้นหาข้อมูลตามรูปแบบที่เลือก
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

    //ดึงข้อมูลข่าวทั้งหมด
    private fun loadAllNews(onLoaded: () -> Unit) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getAllNews().enqueue(object : Callback<List<NewsData>> {
            override fun onResponse(call: Call<List<NewsData>>, response: Response<List<NewsData>>) {
                if (response.isSuccessful) {
                    allNewsList = response.body() ?: listOf() //เก็บไว้ใน allNewsList
                    onLoaded()  // เรียกเมื่อโหลดข่าวเสร็จสิ้น
                } else {
                    Toast.makeText(this@SearchResultActivity, "โหลดข้อมูลข่าวไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<NewsData>>, t: Throwable) {
                Toast.makeText(this@SearchResultActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //กรองการค้นหาตามชื่อ
    private fun searchNews(query: String) {
        val filteredNewsList = allNewsList.filter {
            it.newsName.contains(query, ignoreCase = true) //ไม่สนใจตัวเล็ก-ใหญ่
        }.distinctBy { it.newsId } //ป้องกันข่าวซ้ำ

        fetchReadCounts(filteredNewsList)
    }

    //กรองการค้นหาตามวันที่
    private fun searchNewsByDate(query: String) {
        val filteredNewsList = allNewsList.filter {
            it.dateAdded != null && isDateMatch(it.dateAdded, query) //วันที่เพิ่มข่าวต้องตรงกับวันที่เลือก
        }.distinctBy { it.newsId }

        fetchReadCounts(filteredNewsList)
    }

    //กรองการค้นหาตามช่วงวันที่
    private fun searchNewsByDateRange(startDateStr: String, endDateStr: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val startDate = dateFormat.parse(startDateStr)
        val endDate = dateFormat.parse(endDateStr)

        if (startDate == endDate) {
            Toast.makeText(this, "กรุณาเลือกวันที่ที่แตกต่างกัน", Toast.LENGTH_SHORT).show()
            return
        }

        // ปรับวันที่สิ้นสุดให้เป็น 23.59.59 แก้ปัญหาเรื่อง timezone
        val calendar = Calendar.getInstance()
        calendar.time = endDate
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val adjustedEndDate = calendar.time //กลายเป็นวันที่สิ้นสุดตาม format

        //กรองเอาข่าวที่มีวันที่ข่าวที่อยู่ในช่วงวันที่
        val filteredNewsList = allNewsList.filter {
            it.dateAdded != null && it.dateAdded.after(startDate) && it.dateAdded.before(adjustedEndDate)
        }
        fetchReadCounts(filteredNewsList)
    }

    //ตรวจสอบวันที่ของข่าวกับที่ query
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
                    fetchRatings(newsList)
                } else {
                    Toast.makeText(this@SearchResultActivity, "โหลดข้อมูลยอดการอ่านข่าวไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Total_ReadData>>, t: Throwable) {
                Toast.makeText(this@SearchResultActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@SearchResultActivity, "โหลดข้อมูลคะแนนข่าวไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<News_RatingData>>, t: Throwable) {
                Toast.makeText(this@SearchResultActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
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
                        news.coverImage = coverImage?.let { "${RetrofitClient.getClient(this@SearchResultActivity).baseUrl()}uploads/${it.pictureName}" } ?: ""
                        newsAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@SearchResultActivity, "โหลดรูปภาพข่าวไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<PictureData>>, t: Throwable) {
                    Toast.makeText(this@SearchResultActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
        newsAdapter.setNews(newsList)
    }
}
