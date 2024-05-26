package com.example.project_news_app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.project_news_app.ui.NewsDetailScreen
import com.example.project_news_app.ui.NewsFeedScreen
import com.example.project_news_app.ui.theme.Project_News_AppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContent {
            Project_News_AppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "news_feed") {
        composable("news_feed") { NewsFeedScreen(navController) }
        composable("news_detail") { NewsDetailScreen(navController) }
    }
}