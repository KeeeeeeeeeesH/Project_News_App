package com.example.project_news_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_news_app.adapters.NewsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson

class ReadLaterActivity : AppCompatActivity() {

    private lateinit var readLaterRecyclerView: RecyclerView
    private lateinit var readLaterAdapter: NewsAdapter
    private lateinit var bottomNavigation: BottomNavigationView
    private var readLaterNewsList: List<NewsData> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_later)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        readLaterRecyclerView = findViewById(R.id.read_later_recycler_view)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        readLaterAdapter = NewsAdapter(readLaterNewsList)
        readLaterRecyclerView.layoutManager = LinearLayoutManager(this)
        readLaterRecyclerView.adapter = readLaterAdapter

        loadReadLaterNews()

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
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
    }

    private fun loadReadLaterNews() {
        val sharedPreferences = getSharedPreferences("read_later_prefs", MODE_PRIVATE)
        val newsJson = sharedPreferences.getString("read_later_news", "[]")
        val gson = Gson()
        readLaterNewsList = gson.fromJson(newsJson, Array<NewsData>::class.java).toList()
        readLaterAdapter.setNews(readLaterNewsList)
    }

    // Reload activity
    override fun onResume() {
        super.onResume()
        // Reload the data when coming back to this activity
        loadReadLaterNews()
    }
}
