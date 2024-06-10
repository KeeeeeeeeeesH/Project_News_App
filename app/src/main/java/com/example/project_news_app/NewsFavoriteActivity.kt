package com.example.project_news_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class NewsFavoriteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_favorite)

        val selectedCategories = intent.getStringArrayListExtra("selectedCategories")
        val textView: TextView = findViewById(R.id.textView)
        textView.text = "Selected Categories: ${selectedCategories?.joinToString(", ")}"
    }
}
