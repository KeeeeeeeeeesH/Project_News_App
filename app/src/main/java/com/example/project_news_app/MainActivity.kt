package com.example.project_news_app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the TextView by its ID and set its text
        val greetingTextView: TextView = findViewById(R.id.greetingTextView)
        greetingTextView.text = "Hello Android!"
    }
}
