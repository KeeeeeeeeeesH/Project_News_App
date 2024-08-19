package com.example.project_news_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_news_app.adapters.NewsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.widget.Toolbar

class MyFavoriteCategoryNewsActivity : AppCompatActivity() {

    private lateinit var favoriteNewsRecyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var bottomNavigation: BottomNavigationView
    private var memId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_favorite_category_news)

        // ตั้งค่า Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // ซ่อน Title ที่มากับ Toolbar เพื่อใช้ TextView ตรงกลางแทน
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val editButton: FloatingActionButton = findViewById(R.id.edit_button)
        editButton.setOnClickListener {
            val intent = Intent(this, SelectFavoriteActivity::class.java)
            intent.putExtra("isEditing", true)
            startActivity(intent)
        }

        favoriteNewsRecyclerView = findViewById(R.id.recycler_view)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        favoriteNewsRecyclerView.layoutManager = LinearLayoutManager(this)

        newsAdapter = NewsAdapter(listOf(), NewsAdapter.NewsType.FAVORITE)
        favoriteNewsRecyclerView.adapter = newsAdapter

        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_favorite -> true
                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        memId = sharedPreferences.getInt("memId", -1)

        if (memId != -1) {
            fetchFavoriteNews()
        } else {
            Toast.makeText(this, "ไม่พบข้อมูลสมาชิก", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchFavoriteNews() {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getNewsByFavoriteCategory(memId).enqueue(object : Callback<List<NewsData>> {
            override fun onResponse(call: Call<List<NewsData>>, response: Response<List<NewsData>>) {
                if (response.isSuccessful) {
                    val newsList = response.body() ?: emptyList()
                    if (newsList.isEmpty()) {
                        Toast.makeText(
                            this@MyFavoriteCategoryNewsActivity,
                            "ไม่พบข่าวในหมวดหมู่โปรด",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        fetchAdditionalNewsData(newsList)
                    }
                } else {
                    Toast.makeText(
                        this@MyFavoriteCategoryNewsActivity,
                        "ไม่สามารถดึงข้อมูลข่าวในหมวดหมู่โปรดได้",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<NewsData>>, t: Throwable) {
                Toast.makeText(
                    this@MyFavoriteCategoryNewsActivity,
                    "เกิดข้อผิดพลาดในการดึงข้อมูลข่าวในหมวดหมู่โปรด: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun fetchAdditionalNewsData(newsList: List<NewsData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)

        // Fetch read counts
        apiService.getTotalRead().enqueue(object : Callback<List<Total_ReadData>> {
            override fun onResponse(call: Call<List<Total_ReadData>>, response: Response<List<Total_ReadData>>) {
                if (response.isSuccessful) {
                    val readCounts = response.body() ?: listOf()
                    newsList.forEach { news ->
                        news.readCount = readCounts.count { it.newsId == news.newsId }
                    }
                    fetchRatings(newsList)
                } else {
                    Toast.makeText(this@MyFavoriteCategoryNewsActivity, "Failed to load read counts", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Total_ReadData>>, t: Throwable) {
                Toast.makeText(this@MyFavoriteCategoryNewsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@MyFavoriteCategoryNewsActivity, "Failed to load ratings", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<News_RatingData>>, t: Throwable) {
                Toast.makeText(this@MyFavoriteCategoryNewsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                        news.coverImageUrl = coverImage?.let { "${RetrofitClient.getClient(this@MyFavoriteCategoryNewsActivity).baseUrl()}uploads/${it.pictureName}" } ?: ""
                        newsAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@MyFavoriteCategoryNewsActivity, "Failed to load cover images", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<PictureData>>, t: Throwable) {
                    Toast.makeText(this@MyFavoriteCategoryNewsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
        newsAdapter.setNews(newsList)
    }
}




