//package com.example.project_news_app.ui
//
//import android.app.Activity
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalView
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.core.view.WindowCompat
//import androidx.core.view.WindowInsetsControllerCompat
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.rememberNavController
//import com.example.project_news_app.R
//
//@Composable
//fun ChangeStatusBarColor(color: Color) {
//    val context = LocalContext.current
//    val window = (context as Activity).window
//    val view = LocalView.current
//
//    SideEffect {
//        window.statusBarColor = color.toArgb()
//        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
//    }
//}
//
//@Composable
//fun NewsFeedScreen() {
//    val navController = rememberNavController()
//    var searchQuery by remember { mutableStateOf("") }
//    val newsItems = listOf(
//        "ข่าว 1",
//        "ข่าว 2",
//        "ข่าว 3",
//        "ข่าว 4",
//        "ข่าว 5"
//    )
//
//    ChangeStatusBarColor(color = Color(0xFFCCFFFF)) // เรียกใช้ฟังก์ชันเพื่อเปลี่ยนสีสถานะบาร์
//
//    Scaffold(
//        topBar = { TopBar(searchQuery, onSearchQueryChange = { searchQuery = it }) },
//        bottomBar = { BottomNavigationBar(navController) }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .fillMaxSize()
//                .background(Color(0xFFCCFFFF))
//        ) {
//            Spacer(modifier = Modifier.height(16.dp)) // เพิ่มระยะห่างระหว่าง TextField และ TabRow
//            TabRowExample()
//            NewsList(newsItems, searchQuery)
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TopBar(searchQuery: String, onSearchQueryChange: (String) -> Unit) {
//    TopAppBar(
//        title = {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.Start // จัดเรียงให้อยู่ทางซ้าย
//            ) {
//                TextField(
//                    value = searchQuery,
//                    onValueChange = onSearchQueryChange,
//                    placeholder = { Text("ค้นหา...") },
//                    modifier = Modifier
//                        .width(200.dp)  // ปรับขนาดความกว้างของ TextField
//                        .padding(horizontal = 8.dp, vertical = 4.dp),  // ปรับ padding รอบ ๆ TextField
//                    colors = TextFieldDefaults.textFieldColors(
//                        containerColor = Color.White,
//                        focusedIndicatorColor = Color.Transparent,
//                        unfocusedIndicatorColor = Color.Transparent
//                    )
//                )
//            }
//        },
//        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFCCFFFF)) // ตั้งค่าสีพื้นหลังเป็นสีฟ้า
//    )
//}
//
//@Composable
//fun TabRowExample() {
//    val tabTitles = listOf("แนะนำ", "เทคโนโลยี", "เกม", "สุขภาพ", "ท่องเที่ยว")
//    var selectedTabIndex by remember { mutableStateOf(0) }
//
//    TabRow(selectedTabIndex = selectedTabIndex) {
//        tabTitles.forEachIndexed { index, title ->
//            Tab(
//                selected = selectedTabIndex == index,
//                onClick = { selectedTabIndex = index },
//                text = { Text(text = title) }
//            )
//        }
//    }
//}
//
//@Composable
//fun NewsList(newsItems: List<String>, searchQuery: String) {
//    val filteredNewsItems = newsItems.filter { it.contains(searchQuery, ignoreCase = true) }
//
//    LazyColumn {
//        items(filteredNewsItems) { news ->
//            NewsItem(news = news)
//        }
//    }
//}
//
//@Composable
//fun NewsItem(news: String) {
//    Card(
//        shape = RoundedCornerShape(8.dp), // กำหนดมุมโค้งมน
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp) // เพิ่ม padding รอบ ๆ รายการข่าว
//            .background(Color.White)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(70.dp)
//                        .background(Color.Gray, shape = RoundedCornerShape(8.dp)) // รูปภาพมุมโค้งมน
//                )
//                Spacer(modifier = Modifier.width(16.dp))
//                Text(text = news, fontSize = 20.sp, fontWeight = FontWeight.Bold)
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(text = "by Admin | 6 ชั่วโมงที่แล้ว", fontSize = 14.sp)
//        }
//    }
//}
//
//@Composable
//fun BottomNavigationBar(navController: NavHostController) {
//    NavigationBar(
//        containerColor = Color(0xFFCCFFFF)
//    ) {
//        NavigationBarItem(
//            icon = { Icon(painter = painterResource(id = R.drawable.ic_star), contentDescription = "Favorites") },
//            label = { Text("Favorites") },
//            selected = false,
//            onClick = { /* TODO */ }
//        )
//        NavigationBarItem(
//            icon = { Icon(painter = painterResource(id = R.drawable.ic_home), contentDescription = "Home") },
//            label = { Text("Home") },
//            selected = true,
//            onClick = { /* TODO */ }
//        )
//        NavigationBarItem(
//            icon = { Icon(painter = painterResource(id = R.drawable.ic_account), contentDescription = "Profile") },
//            label = { Text("Profile") },
//            selected = false,
//            onClick = { /* TODO */ }
//        )
//    }
//}

package com.example.project_news_app.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.project_news_app.R

@Composable
fun ChangeStatusBarColor(color: Color) {
    val context = LocalContext.current
    val window = (context as Activity).window
    val view = LocalView.current

    SideEffect {
        window.statusBarColor = color.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }
}

@Composable
fun NewsFeedScreen() {
    val navController = rememberNavController()
    var searchQuery by remember { mutableStateOf("") }
    val newsItems = listOf(
        NewsData("ชื่อข่าว 1", "admin", "วันที่ วง/ดด/ปปป", 4567, 4.55),
        NewsData("ชื่อข่าว 2", "admin", "วันที่ วง/ดด/ปปป", 1234, 4.25),
        NewsData("ชื่อข่าว 3", "admin", "วันที่ วง/ดด/ปปป", 7890, 4.75),
        NewsData("ชื่อข่าว 4", "admin", "วันที่ วง/ดด/ปปป", 5678, 4.45)
    )

    ChangeStatusBarColor(color = Color(0xFFCCFFFF)) // เรียกใช้ฟังก์ชันเพื่อเปลี่ยนสีสถานะบาร์

    Scaffold(
        topBar = { TopBar(searchQuery, onSearchQueryChange = { searchQuery = it }) },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFCCFFFF))
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // เพิ่มระยะห่างระหว่าง TextField และ TabRow
            TabRowExample()
            NewsList(newsItems, searchQuery)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(searchQuery: String, onSearchQueryChange: (String) -> Unit) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start // จัดเรียงให้อยู่ทางซ้าย
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("ค้นหา...") },
                    modifier = Modifier
                        .width(450.dp)  // ปรับขนาดความกว้างของ TextField
                        .padding(horizontal = 8.dp, vertical = 4.dp),  // ปรับ padding รอบ ๆ TextField
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFCCFFFF)) // ตั้งค่าสีพื้นหลังเป็นสีฟ้า
    )
}

data class NewsData(
    val title: String,
    val admin: String,
    val date: String,
    val views: Int,
    val rating: Double
)

@Composable
fun TabRowExample() {
    val tabTitles = listOf("แนะนำ", "เทคโนโลยี", "เกม", "สุขภาพ", "ท่องเที่ยว")
    var selectedTabIndex by remember { mutableStateOf(0) }

    TabRow(selectedTabIndex = selectedTabIndex) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = { Text(text = title) }
            )
        }
    }
}

@Composable
fun NewsList(newsItems: List<NewsData>, searchQuery: String) {
    val filteredNewsItems = newsItems.filter { it.title.contains(searchQuery, ignoreCase = true) }

    LazyColumn {
        items(filteredNewsItems) { news ->
            NewsItem(newsTitle = news.title, admin = news.admin, date = news.date, views = news.views, rating = news.rating)
        }
    }
}

@Composable
fun NewsItem(newsTitle: String, admin: String, date: String, views: Int, rating: Double) {
    Card(
        shape = RoundedCornerShape(8.dp), // กำหนดมุมโค้งมน
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp) // เพิ่ม padding รอบ ๆ รายการข่าว
            .background(Color(0xFFCCFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(Color.Gray, shape = RoundedCornerShape(8.dp)) // รูปภาพมุมโค้งมน
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = newsTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                horizontalAlignment = Alignment.Start // จัดชิดซ้าย
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "$admin | $date", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "อ่าน $views ครั้ง", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(painter = painterResource(id = R.drawable.ic_star), contentDescription = "Rating", modifier = Modifier.size(12.dp))
                    Text(text = "$rating", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar(
        containerColor = Color(0xFFCCFFFF)
    ) {
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.ic_star), contentDescription = "Favorites") },
            label = { Text("Favorites") },
            selected = false,
            onClick = { /* TODO */ }
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.ic_home), contentDescription = "Home") },
            label = { Text("Home") },
            selected = true,
            onClick = { /* TODO */ }
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(id = R.drawable.ic_account), contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = false,
            onClick = { /* TODO */ }
        )
    }
}
