package com.example.project_news_app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import androidx.activity.compose.setContent
import com.example.project_news_app.ui.NewsFeedScreen
import com.example.project_news_app.ui.theme.Project_News_AppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContent {
            Project_News_AppTheme {
                NewsFeedScreen()
            }
        }
    }
}