package com.example.project_news_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_news_app.adapters.NewsAdapter
import com.google.gson.Gson
import com.google.android.material.bottomnavigation.BottomNavigationView

class VisitHistoryActivity : AppCompatActivity() {

    private lateinit var visitHistoryRecyclerView: RecyclerView
    private lateinit var visitHistoryAdapter: NewsAdapter
    private lateinit var bottomNavigation: BottomNavigationView
    private var visitHistoryNewsList: List<NewsData> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_history)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        visitHistoryRecyclerView = findViewById(R.id.visit_history_recycler_view)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        visitHistoryAdapter = NewsAdapter(visitHistoryNewsList)
        visitHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        visitHistoryRecyclerView.adapter = visitHistoryAdapter

        loadVisitHistoryNews()

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

    private fun loadVisitHistoryNews() {
        val sharedPreferences = getSharedPreferences("visit_history_prefs", MODE_PRIVATE)
        val newsJson = sharedPreferences.getString("visit_history_news", "[]")
        val gson = Gson()
        visitHistoryNewsList = gson.fromJson(newsJson, Array<NewsData>::class.java).toList()
        visitHistoryAdapter.setNews(visitHistoryNewsList)
    }
}
