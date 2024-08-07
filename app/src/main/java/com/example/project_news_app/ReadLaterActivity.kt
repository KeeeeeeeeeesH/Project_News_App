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

        readLaterRecyclerView = findViewById(R.id.read_later_recycler_view)
        readLaterRecyclerView.layoutManager = LinearLayoutManager(this)

        newsAdapter = NewsAdapter(listOf(), isReadLater = true)
        readLaterRecyclerView.adapter = newsAdapter

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        memId = sharedPreferences.getInt("memId", -1)

        if (memId != -1) {
            fetchReadLaterNews(memId)
        } else {
            Toast.makeText(this, "ไม่พบ Member ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchReadLaterNews(memId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getReadLaterByMemId(memId).enqueue(object : Callback<List<Read_LaterData>> {
            override fun onResponse(call: Call<List<Read_LaterData>>, response: Response<List<Read_LaterData>>) {
                if (response.isSuccessful) {
                    val readLaterList = response.body() ?: listOf()
                    if (readLaterList.isNotEmpty()) {
                        fetchNewsData(readLaterList.map { it.newsId })
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

    private fun fetchNewsData(newsIds: List<Int>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getNewsByIds(newsIds).enqueue(object : Callback<List<NewsData>> {
            override fun onResponse(call: Call<List<NewsData>>, response: Response<List<NewsData>>) {
                if (response.isSuccessful) {
                    val newsList = response.body() ?: listOf()
                    newsAdapter.setNews(newsList)
                } else {
                    Toast.makeText(this@ReadLaterActivity, "ไม่สามารถดึงข้อมูลข่าวได้", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<NewsData>>, t: Throwable) {
                Toast.makeText(this@ReadLaterActivity, "เกิดข้อผิดพลาดในการดึงข้อมูลข่าว: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun deleteReadLater(memId: Int, newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val readLaterData = Read_LaterData(memId, newsId)

        apiService.deleteReadLater(readLaterData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    fetchReadLaterNews(memId)
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
