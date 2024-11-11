package com.example.project_news_app

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_news_app.adapters.NewsAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReadLaterActivity : AppCompatActivity() {

    private lateinit var readLaterRecyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private var memId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_later)

        //toolbar ย้อนกลับ
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        readLaterRecyclerView = findViewById(R.id.read_later_recycler_view)
        readLaterRecyclerView.layoutManager = LinearLayoutManager(this)

        newsAdapter = NewsAdapter(listOf(), NewsAdapter.NewsType.READ_LATER) //ใช้ Read_Later Type
        readLaterRecyclerView.adapter = newsAdapter

       //SharedPref
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        memId = sharedPreferences.getInt("memId", -1)

        if (memId != -1) {
            fetchReadLaterNews()
        } else {
            Toast.makeText(this, "ไม่พบข้อมูลสมาชิก", Toast.LENGTH_SHORT).show()
        }
    }

    //ดึงข้อมูลข่าวอ่านภายหลัง
    private fun fetchReadLaterNews() {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getReadLaterByMemId(memId).enqueue(object : Callback<List<Read_LaterData>> {
            override fun onResponse(call: Call<List<Read_LaterData>>, response: Response<List<Read_LaterData>>) {
                if (response.isSuccessful) {
                    val readLaterList = response.body() ?: listOf()
                    //ถ้ามีข้อมูล ไปดึงข่าวมาแสดง
                    if (readLaterList.isNotEmpty()) {
                        fetchNewsData(readLaterList)
                    } else {
                        Toast.makeText(this@ReadLaterActivity, "ไม่มีข่าวในรายการอ่านภายหลัง", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ReadLaterActivity, "ไม่สามารถดึงข้อมูลข่าวอ่านภายหลังได้", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Read_LaterData>>, t: Throwable) {
                Toast.makeText(this@ReadLaterActivity, "เกิดข้อผิดพลาดในการดึงข้อมูลข่าวอ่านภายหลัง: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //ดึงข้อมูลข่าวที่จะแสดง
    private fun fetchNewsData(readLaterList: List<Read_LaterData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        //ดึงข่าวทั้งหมดมา
        apiService.getAllNews().enqueue(object : Callback<List<NewsData>> {
            override fun onResponse(call: Call<List<NewsData>>, response: Response<List<NewsData>>) {
                if (response.isSuccessful) {
                    val newsList = response.body() ?: emptyList()
                    //map หา newsId ที่ตรงกันทั้งคู่
                    val readLaterWithNewsList = readLaterList.mapNotNull { readLater ->
                        val news = newsList.find { it.newsId == readLater.newsId }
                        //ถ้าเจอจะสร้างชุดข้อมูลมาแสดงรายละเอียด
                        news?.let {
                            ReadLaterWithNewsData(
                                newsId = it.newsId,
                                dateAdded = it.dateAdded,
                                newsName = it.newsName,
                                ratingScore = 0f,
                                coverImage = "",
                                readCount = 0
                            )
                        }
                    }
                    // ดึงข้อมูลการอ่านเมื่อเสร็จ
                    fetchReadCounts(readLaterWithNewsList)
                } else {
                    Toast.makeText(this@ReadLaterActivity, "โหลดข้อมูลข่าวไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<NewsData>>, t: Throwable) {
                Toast.makeText(this@ReadLaterActivity, "เกิดข้อผิดพลาดในการดึงข้อมูลข่าว: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //ดึงจำนวนการอ่าน
    private fun fetchReadCounts(readLaterWithNewsList: List<ReadLaterWithNewsData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getTotalRead().enqueue(object : Callback<List<Total_ReadData>> {
            override fun onResponse(call: Call<List<Total_ReadData>>, response: Response<List<Total_ReadData>>) {
                if (response.isSuccessful) {
                    val readCounts = response.body() ?: listOf()
                    readLaterWithNewsList.forEach { news ->
                        news.readCount = readCounts.count { it.newsId == news.newsId }
                    }
                    fetchRatings(readLaterWithNewsList)
                } else {
                    Toast.makeText(this@ReadLaterActivity, "เกิดข้อผิดพลาดในการดึงข้อมูลการอ่าน", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Total_ReadData>>, t: Throwable) {
                Toast.makeText(this@ReadLaterActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //ดึงคะแนนข่าว
    private fun fetchRatings(readLaterWithNewsList: List<ReadLaterWithNewsData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getNewsRating().enqueue(object : Callback<List<News_RatingData>> {
            override fun onResponse(call: Call<List<News_RatingData>>, response: Response<List<News_RatingData>>) {
                if (response.isSuccessful) {
                    val ratings = response.body() ?: listOf()
                    readLaterWithNewsList.forEach { news ->
                        val newsRatings = ratings.filter { it.newsId == news.newsId }
                        news.ratingScore = if (newsRatings.isNotEmpty()) {
                            newsRatings.sumByDouble { it.ratingScore.toDouble() }.toFloat() / newsRatings.size
                        } else {
                            0f
                        }
                    }
                    fetchCoverImages(readLaterWithNewsList)
                } else {
                    Toast.makeText(this@ReadLaterActivity, "เกิดข้อผิดพลาดในการโหลดคะแนนข่าว", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<News_RatingData>>, t: Throwable) {
                Toast.makeText(this@ReadLaterActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //ดึงรูปปก
    private fun fetchCoverImages(readLaterWithNewsList: List<ReadLaterWithNewsData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        readLaterWithNewsList.forEach { news ->
            apiService.getCoverImage(news.newsId).enqueue(object : Callback<List<PictureData>> {
                override fun onResponse(call: Call<List<PictureData>>, response: Response<List<PictureData>>) {
                    if (response.isSuccessful) {
                        val pictures = response.body() ?: listOf()
                        val coverImage = pictures.find { it.pictureName.startsWith("cover_") }
                        news.coverImage = coverImage?.let { "${RetrofitClient.getClient(this@ReadLaterActivity).baseUrl()}uploads/${it.pictureName}" } ?: ""
                        newsAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@ReadLaterActivity, "เกิดข้อผิดพลาดในการโหลดรูปภาพหน้าปก", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<PictureData>>, t: Throwable) {
                    Toast.makeText(this@ReadLaterActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
        newsAdapter.setNews(readLaterWithNewsList)
    }
}



