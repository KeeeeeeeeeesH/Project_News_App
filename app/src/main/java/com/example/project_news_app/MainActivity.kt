package com.example.project_news_app

import NetworkUtil
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.project_news_app.adapters.CategoryAdapter
import com.example.project_news_app.adapters.NewsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var searchButton: ImageButton
    private lateinit var toggleCategories: ImageButton
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var categoryAdapter: CategoryAdapter


    private var currentCategoryId: Int = 0 // Default ไปที่ "แนะนำ"
    private var currentPage: Int = 0 // index ของหน้าข่าวสำหรับการโหลดข่าวเพิ่ม
    private var allNewsList: List<NewsData> = listOf() // All news list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        searchButton = findViewById(R.id.search_button)
        toggleCategories = findViewById(R.id.toggle_categories)
        categoriesRecyclerView = findViewById(R.id.categories_recycler_view)
        newsRecyclerView = findViewById(R.id.news_recycler_view)
        bottomNavigation = findViewById(R.id.bottom_navigation)


        categoriesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        newsRecyclerView.layoutManager = LinearLayoutManager(this)

        categoryAdapter = CategoryAdapter { category ->
            currentCategoryId = category.catId
            categoryAdapter.setSelectedCategory(category.catId)
            loadNewsByCategory(category.catId)
        }
        newsAdapter = NewsAdapter(listOf(), NewsAdapter.NewsType.GENERAL)

        categoriesRecyclerView.adapter = categoryAdapter
        newsRecyclerView.adapter = newsAdapter

        loadCategories()
        loadNewsByCategory(currentCategoryId)

        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    currentCategoryId = 0
                    loadNewsByCategory(currentCategoryId)
                    true
                }
                R.id.navigation_favorite -> {
                    startActivity(Intent(this, SelectFavoriteActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        bottomNavigation.selectedItemId = R.id.navigation_home

        toggleCategories.setOnClickListener {
            onToggleCategoriesClick()
        }

        newsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    loadMoreNews()
                }
            }
        })

        // ตั้งค่า swipe refresh
        swipeRefreshLayout.setOnRefreshListener {
            loadNewsByCategory(currentCategoryId)
        }

        searchButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SearchNewsActivity::class.java)
            startActivityForResult(intent, SEARCH_REQUEST_CODE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        FirebaseMessaging.getInstance().subscribeToTopic("news_topic")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Subscribed to news_topic successfully")
                } else {
                    Log.d("FCM", "Failed to subscribe to news_topic")
                }
            }

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val memId = sharedPreferences.getInt("memId", -1)
        if (memId != -1) {
            FirebaseMessaging.getInstance().subscribeToTopic("user_$memId")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FCM", "Subscribed to user_$memId successfully")
                    } else {
                        Log.d("FCM", "Failed to subscribe to user_$memId")
                    }
                }
        } else {
            Log.e("FCM", "Failed to retrieve memId for subscribing to user topic")
        }

    }

    private fun onToggleCategoriesClick() {
    }

    private fun loadCategories() {
        if (!NetworkUtil.isInternetAvailable(this)) {
            showNoInternetError()
            swipeRefreshLayout.isRefreshing = false
            return
        }

        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getCategory().enqueue(object : Callback<List<CategoryData>> {
            override fun onResponse(call: Call<List<CategoryData>>, response: Response<List<CategoryData>>) {
                if (response.isSuccessful) {
                    val categories = response.body() ?: listOf()
                    val allCategories = listOf(CategoryData(0, "แนะนำ")) + categories
                    categoryAdapter.setCategories(allCategories)
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<CategoryData>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadNewsByCategory(catId: Int) {
        currentPage = 0
        allNewsList = listOf()
        loadMoreNews()
        swipeRefreshLayout.isEnabled = true
    }

    private fun loadMoreNews() {
        if (!NetworkUtil.isInternetAvailable(this)) {
            showNoInternetError()
            swipeRefreshLayout.isRefreshing = false
            return
        }

        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getNewsByCategoryPaged(currentCategoryId, currentPage, 5).enqueue(object : Callback<List<NewsData>> {
            override fun onResponse(call: Call<List<NewsData>>, response: Response<List<NewsData>>) {
                if (response.isSuccessful) {
                    val newsList = response.body() ?: listOf()
                    val newNewsList = newsList.distinctBy { it.newsId }
                    if (currentPage == 0) {
                        allNewsList = newNewsList
                        newsAdapter.setNews(newNewsList)
                    } else {
                        allNewsList = (allNewsList + newNewsList).distinctBy { it.newsId }
                        newsAdapter.addNews(newNewsList)
                    }
                    fetchReadCounts(newsList)
                    currentPage++
                    swipeRefreshLayout.isRefreshing = false
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load news", Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }

            override fun onFailure(call: Call<List<NewsData>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false // Stop the refreshing indicator
            }
        })
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
                    Toast.makeText(this@MainActivity, "Failed to load read counts", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Total_ReadData>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                            newsRatings.sumByDouble { it.ratingScore.toDouble() }
                                .toFloat() / newsRatings.size
                        } else {
                            0f
                        }
                    }
                    fetchCoverImages(newsList)
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load ratings", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<News_RatingData>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                        news.coverImage = coverImage?.let { "${RetrofitClient.getClient(this@MainActivity).baseUrl()}uploads/${it.pictureName}" }
                        newsAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to load cover images", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<PictureData>>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    private fun showNoInternetError() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("ไม่มีการเชื่อมต่ออินเทอร์เน็ต")
            .setMessage("ไม่สามารถโหลดรายการข่าวได้ โปรดตรวจสอบการเชื่อมต่ออินเทอร์เน็ตของคุณ")
            .setPositiveButton("ตกลง", null)
            .create()
        dialog.show()
    }
    companion object {
        private const val SEARCH_REQUEST_CODE = 1
    }
}

