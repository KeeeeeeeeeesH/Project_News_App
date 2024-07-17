package com.example.project_news_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.project_news_app.adapters.CategoryAdapter
import com.example.project_news_app.adapters.NewsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*
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
    private lateinit var foundNewsLabel: TextView

    private var currentCategoryId: Int = 0 // Default to "แนะนำ"
    private var currentPage: Int = 0 // Page index for loading more news
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
        foundNewsLabel = findViewById(R.id.found_news_label)

        categoriesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        newsRecyclerView.layoutManager = LinearLayoutManager(this)

        categoryAdapter = CategoryAdapter { category ->
            currentCategoryId = category.catId
            categoryAdapter.setSelectedCategory(category.catId)
            loadNewsByCategory(category.catId)
        }
        newsAdapter = NewsAdapter(listOf())

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
                    startActivity(Intent(this, FavoriteActivity::class.java))
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

        searchButton.setOnClickListener {
            startActivityForResult(Intent(this, SearchNewsActivity::class.java), SEARCH_REQUEST_CODE)
        }

        newsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    loadMoreNews()
                }
            }
        })

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            loadNewsByCategory(currentCategoryId)
        }
    }

    private fun onToggleCategoriesClick() {
        // Logic to handle the toggle categories click
    }

    private fun loadCategories() {
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
        allNewsList = listOf() // Reset all news list
        loadMoreNews()
    }

    private fun loadMoreNews() {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getNewsByCategoryPaged(currentCategoryId, currentPage, 5).enqueue(object : Callback<List<NewsData>> {
            override fun onResponse(call: Call<List<NewsData>>, response: Response<List<NewsData>>) {
                if (response.isSuccessful) {
                    val newsList = response.body() ?: listOf()
                    if (currentPage == 0) {
                        allNewsList = newsList
                        newsAdapter.setNews(newsList)
                    } else {
                        val newNewsList = newsList.distinctBy { it.newsId }
                        allNewsList = (allNewsList + newNewsList).distinctBy { it.newsId }
                        newsAdapter.addNews(newNewsList)
                    }
                    fetchReadCounts(newsList)
                    currentPage++
                    swipeRefreshLayout.isRefreshing = false // Stop the refreshing indicator
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load news", Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false // Stop the refreshing indicator
                }
            }

            override fun onFailure(call: Call<List<NewsData>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false // Stop the refreshing indicator
            }
        })
    }


    private fun searchNews(query: String) {
        val filteredNewsList = allNewsList.filter {
            it.newsName.contains(query, ignoreCase = true) || isDateMatch(it.dateAdded, query)
        }
        newsAdapter.setNews(filteredNewsList)
    }

    private fun isDateMatch(date: Date, query: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateString = dateFormat.format(date)
        return dateString.contains(query)
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
            override fun onResponse(
                call: Call<List<News_RatingData>>,
                response: Response<List<News_RatingData>>
            ) {
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
                    Toast.makeText(this@MainActivity, "Failed to load ratings", Toast.LENGTH_SHORT)
                        .show()
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
                        news.coverImageUrl = coverImage?.let { "${RetrofitClient.getClient(this@MainActivity).baseUrl()}uploads/${it.pictureName}" }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SEARCH_REQUEST_CODE && resultCode == RESULT_OK) {
            val query = data?.getStringExtra("SEARCH_QUERY") ?: return
            val searchType = data.getStringExtra("SEARCH_TYPE")

            when (searchType) {
                "NAME" -> searchNews(query)
                "DATE" -> searchNewsByDate(query)
                "PERIOD" -> searchNewsByPeriod(query)
            }

            // เปลี่ยนแถบหมวดหมู่เป็น "ข่าวที่พบ"
            findViewById<View>(R.id.categories_container).visibility = View.GONE
            foundNewsLabel.visibility = View.VISIBLE
            // เอาปุ่มค้นหาออก
            findViewById<View>(R.id.search_bar_container).visibility = View.GONE
        }
    }

    private fun searchNewsByDate(query: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = dateFormat.parse(query)
        val filteredNewsList = allNewsList.filter {
            isDateMatch(it.dateAdded, query)
        }
        newsAdapter.setNews(filteredNewsList)
    }

    private fun searchNewsByPeriod(period: String) {
        val calendar = Calendar.getInstance()
        val currentDate = Date()

        when (period) {
            "CURRENT_WEEK" -> {
                calendar.time = currentDate
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val targetDate = calendar.time
                val filteredNewsList = allNewsList.filter {
                    it.dateAdded.after(targetDate)
                }
                newsAdapter.setNews(filteredNewsList)
            }
            "LAST_WEEK" -> {
                calendar.time = currentDate
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val endDate = calendar.time
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val startDate = calendar.time
                val filteredNewsList = allNewsList.filter {
                    it.dateAdded.after(startDate) && it.dateAdded.before(endDate)
                }
                newsAdapter.setNews(filteredNewsList)
            }
            "LAST_MONTH" -> {
                calendar.time = currentDate
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val targetDate = calendar.time
                val filteredNewsList = allNewsList.filter {
                    it.dateAdded.after(targetDate) && it.dateAdded.before(currentDate)
                }
                newsAdapter.setNews(filteredNewsList)
            }
            "LAST_SIX_MONTHS" -> {
                calendar.time = currentDate
                calendar.add(Calendar.DAY_OF_YEAR, -180)
                val targetDate = calendar.time
                val filteredNewsList = allNewsList.filter {
                    it.dateAdded.after(targetDate) && it.dateAdded.before(currentDate)
                }
                newsAdapter.setNews(filteredNewsList)
            }
            "LAST_YEAR" -> {
                calendar.time = currentDate
                calendar.add(Calendar.DAY_OF_YEAR, -365)
                val targetDate = calendar.time
                val filteredNewsList = allNewsList.filter {
                    it.dateAdded.after(targetDate) && it.dateAdded.before(currentDate)
                }
                newsAdapter.setNews(filteredNewsList)
            }
        }
    }

    override fun onBackPressed() {
        if (foundNewsLabel.visibility == View.VISIBLE) {
            // ถ้าอยู่ในหน้าข่าวที่พบ ให้กลับไปที่หน้าค้นหา
            val intent = Intent(this, SearchNewsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val SEARCH_REQUEST_CODE = 1
    }
}

