package com.example.project_news_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_news_app.adapters.CategoryAdapter
import com.example.project_news_app.adapters.NewsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var searchBar: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var toggleCategories: ImageButton
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private var currentCategoryId: Int = 0 // Default to "แนะนำ"
    private var currentPage: Int = 0 // Page index for loading more news

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchBar = findViewById(R.id.search_bar)
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
            // Handle search click
        }

        newsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    loadMoreNews()
                }
            }
        })
    }

    private fun onToggleCategoriesClick() {
        // Logic to handle the toggle categories click
    }

    private fun loadCategories() {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)
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
        loadMoreNews()
    }

    private fun loadMoreNews() {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        apiService.getNewsByCategoryPaged(currentCategoryId, currentPage, 5).enqueue(object : Callback<List<NewsData>> {
            override fun onResponse(call: Call<List<NewsData>>, response: Response<List<NewsData>>) {
                if (response.isSuccessful) {
                    val newsList = response.body() ?: listOf()
                    if (currentPage == 0) {
                        newsAdapter.setNews(newsList)
                    } else {
                        newsAdapter.addNews(newsList)
                    }
                    fetchReadCounts(newsList)
                    currentPage++
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load news", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<NewsData>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchReadCounts(newsList: List<NewsData>) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

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
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

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
                    Toast.makeText(this@MainActivity, "Failed to load ratings", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<News_RatingData>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCoverImages(newsList: List<NewsData>) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        newsList.forEach { news ->
            apiService.getCoverImage(news.newsId).enqueue(object : Callback<List<PictureData>> {
                override fun onResponse(call: Call<List<PictureData>>, response: Response<List<PictureData>>) {
                    if (response.isSuccessful) {
                        val pictures = response.body() ?: listOf()
                        val coverImage = pictures.find { it.pictureName.startsWith("cover_") }

                        news.coverImageUrl = coverImage?.let { "http://10.3.58.145:5000/uploads/${it.pictureName}" }

                        Log.d("MainActivity", "Cover Image URL: ${news.coverImageUrl}")
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

}

