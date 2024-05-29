package com.example.project_news_app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project_news_app.R

@Composable
fun ReadLaterScreen(navController: NavController) {
    val readLaterItems = remember { mutableStateListOf<NewsData>() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // Load the read later items here. For now, we use static data for demonstration.
        readLaterItems.addAll(
            listOf(
                NewsData("ชื่อข่าว A", "admin", "วันที่ วง/ดด/ปปป", 4567, 4.55),
                NewsData("ชื่อข่าว B", "admin", "วันที่ วง/ดด/ปปป", 1234, 4.25),
                NewsData("ชื่อข่าว C", "admin", "วันที่ วง/ดด/ปปป", 7890, 4.75),
                NewsData("ชื่อข่าว D", "admin", "วันที่ วง/ดด/ปปป", 5678, 4.45)
            )
        )
    }

    Scaffold(
        topBar = { TopBarWithTitle(title = "อ่านภายหลัง") },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFCCFFFF))
        ) {
            val scrollState = rememberScrollState()
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                readLaterItems.forEach { news ->
                    NewsItem(
                        newsTitle = news.title,
                        admin = news.admin,
                        date = news.date,
                        views = news.views,
                        rating = news.rating,
                        navController = navController
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithTitle(title: String) {
    TopAppBar(
        title = { Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFCCFFFF)) // Set background color to blue
    )
}
