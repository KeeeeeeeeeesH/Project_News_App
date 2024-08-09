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

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        readLaterRecyclerView = findViewById(R.id.read_later_recycler_view)
        readLaterRecyclerView.layoutManager = LinearLayoutManager(this)

        newsAdapter = NewsAdapter(listOf(), isReadLater = true)
        readLaterRecyclerView.adapter = newsAdapter

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        memId = sharedPreferences.getInt("memId", -1)

        if (memId != -1) {
            fetchReadLaterNews()
        } else {
            Toast.makeText(this, "ไม่พบข้อมูลสมาชิก", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchReadLaterNews() {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getReadLaterByMemId(memId).enqueue(object : Callback<List<Read_LaterData>> {
            override fun onResponse(call: Call<List<Read_LaterData>>, response: Response<List<Read_LaterData>>) {
                if (response.isSuccessful) {
                    val readLaterList = response.body() ?: listOf()
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


    private fun fetchNewsData(readLaterList: List<Read_LaterData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getAllNews().enqueue(object : Callback<List<NewsData>> {
            override fun onResponse(
                call: Call<List<NewsData>>,
                response: Response<List<NewsData>>
            ) {
                if (response.isSuccessful) {
                    val newsList = response.body() ?: emptyList()
                    val readLaterWithNewsList = readLaterList.mapNotNull { readLater ->
                        val news = newsList.find { it.newsId == readLater.newsId }
                        news?.let {
                            ReadLaterWithNewsData(
                                newsId = it.newsId,
                                dateAdded = it.dateAdded,
                                newsName = it.newsName,
                                ratingScore = 0f, // Default value, will be updated later
                                coverImage = "",
                                readCount = 0 // Default value, will be updated later
                            )
                        }
                    }
                    // Call the next steps to update read count and ratings
                    fetchReadCounts(readLaterWithNewsList)
                } else {
                    Toast.makeText(
                        this@ReadLaterActivity,
                        "ไม่สามารถดึงข้อมูลข่าวได้",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<NewsData>>, t: Throwable) {
                Toast.makeText(this@ReadLaterActivity, "เกิดข้อผิดพลาดในการดึงข้อมูลข่าว: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


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
                    Toast.makeText(this@ReadLaterActivity, "Failed to load read counts", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Total_ReadData>>, t: Throwable) {
                Toast.makeText(this@ReadLaterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

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
                    Toast.makeText(this@ReadLaterActivity, "Failed to load ratings", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<News_RatingData>>, t: Throwable) {
                Toast.makeText(this@ReadLaterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


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
                        Toast.makeText(this@ReadLaterActivity, "Failed to load cover images", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<PictureData>>, t: Throwable) {
                    Toast.makeText(this@ReadLaterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
        newsAdapter.setNews(readLaterWithNewsList) // Add this line
    }


    fun deleteReadLater(memId: Int, newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val call = apiService.deleteReadLater(memId, newsId)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ReadLaterActivity, "ลบข่าวออกจากรายการอ่านภายหลังเรียบร้อย", Toast.LENGTH_SHORT).show()
                    fetchReadLaterNews()
                } else {
                    Toast.makeText(this@ReadLaterActivity, "ไม่สามารถลบข่าวออกจากรายการอ่านภายหลังได้", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ReadLaterActivity, "เกิดข้อผิดพลาดในการลบข่าวออกจากรายการอ่านภายหลัง: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}


