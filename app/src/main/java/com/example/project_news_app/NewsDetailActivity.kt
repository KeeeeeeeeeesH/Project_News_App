package com.example.project_news_app

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

class NewsDetailActivity : AppCompatActivity() {

    private lateinit var starImages: List<ImageView>
    private var selectedRating = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val newsTitle = intent.getStringExtra("news_title")
        val newsAdmin = intent.getStringExtra("news_admin")
        val newsDate = intent.getStringExtra("news_date")
        val newsReadCount = intent.getStringExtra("news_read_count")
        val newsRating = intent.getStringExtra("news_rating")

        val newsTitleTextView: TextView = findViewById(R.id.news_title)
        val newsAdminTextView: TextView = findViewById(R.id.news_admin)
        val newsDateTextView: TextView = findViewById(R.id.news_date)
        val newsReadCountTextView: TextView = findViewById(R.id.news_read_count)
        val newsRatingTextView: TextView = findViewById(R.id.news_rating)

        newsTitleTextView.text = newsTitle
        newsAdminTextView.text = newsAdmin
        newsDateTextView.text = newsDate
        newsReadCountTextView.text = newsReadCount
        newsRatingTextView.text = newsRating

        // Initialize star images
        starImages = listOf(
            findViewById(R.id.star1),
            findViewById(R.id.star2),
            findViewById(R.id.star3),
            findViewById(R.id.star4),
            findViewById(R.id.star5)
        )

        // Set click listeners for star images
        for ((index, star) in starImages.withIndex()) {
            star.setOnClickListener {
                setRating(index + 1)
            }
        }

        // Bottom Navigation Bar
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_favorites -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_home -> {
                    // Handle navigation to Favorites
                    true
                }
                R.id.navigation_favorites -> {
                    // Handle navigation to Profile
                    true
                }
                else -> false
            }
        }
    }

    private fun setRating(rating: Int) {
        selectedRating = rating
        for (i in starImages.indices) {
            if (i < rating) {
                starImages[i].setImageResource(R.drawable.ic_star_outline)
            } else {
                starImages[i].setImageResource(R.drawable.ic_star_filled)
            }
        }
    }
}
