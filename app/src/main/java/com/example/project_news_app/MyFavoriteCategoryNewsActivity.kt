package com.example.project_news_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_news_app.adapters.NewsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyFavoriteCategoryNewsActivity : AppCompatActivity() {

    private lateinit var favoriteNewsRecyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var selectedCategoriesTextView: TextView
    private lateinit var progressBar: ProgressBar
    private var memId: Int = -1
    private val categoryMap = mutableMapOf<Int, String>()  // เก็บ catId และ catName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_favorite_category_news)

        // ตั้งค่า Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        // ใช้ TextView ตรงกลางแทน
        supportActionBar?.setDisplayShowTitleEnabled(false)

        selectedCategoriesTextView = findViewById(R.id.selected_categories_text_view)
        favoriteNewsRecyclerView = findViewById(R.id.recycler_view)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        progressBar = findViewById(R.id.progress_bar)

        progressBar.visibility = View.VISIBLE

        favoriteNewsRecyclerView.layoutManager = LinearLayoutManager(this)

        newsAdapter = NewsAdapter(listOf(), NewsAdapter.NewsType.FAVORITE) //เรียกใช้ Favorite Type
        favoriteNewsRecyclerView.adapter = newsAdapter

        // ตั้งค่าปุ่มแก้ไข
        val editButton: FloatingActionButton = findViewById(R.id.edit_button)
        editButton.setOnClickListener {
            val intent = Intent(this, SelectFavoriteActivity::class.java)
            intent.putExtra("isEditing", true)
            startActivity(intent)
        }

        //ตั้งค่า bottomNav
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

        //SharedPref
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        memId = sharedPreferences.getInt("memId", -1)
        if (memId != -1) {
            fetchAllCategories { // เมื่อหมวดหมู่โหลดเสร็จแล้ว
                fetchFavoriteNews() // ดึงข่าวในหมวดหมู่โปรด
                fetchSelectedCategories(memId) // ดึงหมวดหมู่ที่เลือกแล้ว
                progressBar.visibility = View.GONE
            }
        } else {
            Toast.makeText(this, "ไม่พบข้อมูลสมาชิก", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }
    }

    //ดึงข้อมูลหมวดหมู่ทั้งหมดก่อน
    private fun fetchAllCategories(onComplete: () -> Unit) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getCategory().enqueue(object : Callback<List<CategoryData>> {
            override fun onResponse(call: Call<List<CategoryData>>, response: Response<List<CategoryData>>) {
                if (response.isSuccessful) {
                    val categories = response.body()
                    //แปลง id เป็น name
                    categories?.forEach { category ->
                        categoryMap[category.catId] = category.catName
                    }
                    onComplete() // ทำงานต่อได้
                } else {
                    Toast.makeText(this@MyFavoriteCategoryNewsActivity, "ไม่สามารถโหลดข้อมูลหมวดหมู่ได้", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<CategoryData>>, t: Throwable) {
                Toast.makeText(this@MyFavoriteCategoryNewsActivity, "เกิดข้อผิดพลาดในการโหลดข้อมูลหมวดหมู่", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //ดึงข่าวตามหมวดหมู่โปรด
    private fun fetchFavoriteNews() {
        progressBar.visibility = View.VISIBLE
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        //เตรียมข้อมูลหมวดหมู่ไว้ก่อน จะได้ไม่ต้องแปลงซ้ำอีกรอบ
        fetchAllCategories {
            //ดึงข่าวที่ catId ตรงกันทั้ง 2 ตาราง
            apiService.getNewsByFavoriteCategory(memId).enqueue(object : Callback<List<NewsData>> {
                override fun onResponse(call: Call<List<NewsData>>, response: Response<List<NewsData>>) {
                    if (response.isSuccessful) {
                        val newsList = response.body() ?: emptyList()
                        if (newsList.isEmpty()) {
                            Toast.makeText(this@MyFavoriteCategoryNewsActivity, "ไม่พบข่าวในหมวดหมู่โปรด", Toast.LENGTH_SHORT).show()
                        } else {
                            fetchReadCount(newsList) //ไปดึงจำนวนการอ่าน
                        }
                    } else {
                        Toast.makeText(this@MyFavoriteCategoryNewsActivity, "ไม่สามารถดึงข้อมูลข่าวในหมวดหมู่โปรดได้", Toast.LENGTH_SHORT).show()
                    }
                    progressBar.visibility = View.GONE
                }

                override fun onFailure(call: Call<List<NewsData>>, t: Throwable) {
                    Toast.makeText(this@MyFavoriteCategoryNewsActivity, "เกิดข้อผิดพลาดในการดึงข้อมูลข่าวในหมวดหมู่โปรด: ${t.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            })
        }
    }

    //ดึงชื่อหมวดหมู่ที่เลือกมาแสดง
    private fun fetchSelectedCategories(memId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getFavoriteCategoryByMemId(memId).enqueue(object : Callback<List<Favorite_CategoryData>> {
            override fun onResponse(call: Call<List<Favorite_CategoryData>>, response: Response<List<Favorite_CategoryData>>) {
                if (response.isSuccessful) {
                    val categories = response.body()
                    //ให้วนลูปฟังก์ชันเพื่อเข้าถึงข้อมูลที่ map แล้ว
                    val categoryNames = categories?.joinToString(", ") { getCategoryNameById(it.catId) }
                    selectedCategoriesTextView.text = "หมวดหมู่ที่เลือก: $categoryNames"
                } else {
                    selectedCategoriesTextView.text = "ไม่สามารถโหลดหมวดหมู่ได้"
                }
            }

            override fun onFailure(call: Call<List<Favorite_CategoryData>>, t: Throwable) {
                selectedCategoriesTextView.text = "เกิดข้อผิดพลาดในการโหลดหมวดหมู่"
            }
        })
    }

    //ดึงชื่อจาก map มาหาว่าชื่อไร
    private fun getCategoryNameById(catId: Int): String {
        return categoryMap[catId] ?: "Unknown Category"
    }

    private fun fetchReadCount(newsList: List<NewsData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getTotalRead().enqueue(object : Callback<List<Total_ReadData>> {
            override fun onResponse(call: Call<List<Total_ReadData>>, response: Response<List<Total_ReadData>>) {
                if (response.isSuccessful) {
                    val readCounts = response.body() ?: listOf()
                    newsList.forEach { news ->
                        news.readCount = readCounts.count { it.newsId == news.newsId }
                    }
                    fetchRatings(newsList) //ไปดึงคะแนนต่อ
                } else {
                    Toast.makeText(this@MyFavoriteCategoryNewsActivity, "ไม่สามารถโหลดยอดการอ่านข่าวได้", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Total_ReadData>>, t: Throwable) {
                Toast.makeText(this@MyFavoriteCategoryNewsActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
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
                    fetchCoverImages(newsList) //ไปดึงรูปปกต่อ
                } else {
                    Toast.makeText(this@MyFavoriteCategoryNewsActivity, "ไม่สามารถโหลดคะแนนข่าวได้", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<News_RatingData>>, t: Throwable) {
                Toast.makeText(this@MyFavoriteCategoryNewsActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
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
                        news.coverImage = coverImage?.let { "${RetrofitClient.getClient(this@MyFavoriteCategoryNewsActivity).baseUrl()}uploads/${it.pictureName}" } ?: ""
                        newsAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@MyFavoriteCategoryNewsActivity, "ไม่สามารถโหลดรูปภาพข่าวได้", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<PictureData>>, t: Throwable) {
                    Toast.makeText(this@MyFavoriteCategoryNewsActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
        newsAdapter.setNews(newsList) //ดึงครบแล้ว update ลง adapter
    }
}




