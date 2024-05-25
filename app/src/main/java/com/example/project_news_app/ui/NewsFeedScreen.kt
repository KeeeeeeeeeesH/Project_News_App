//package com.example.project_news_app.ui
//
//import android.app.Activity
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
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
//    var isSearchVisible by remember { mutableStateOf(false) }
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
//        topBar = { TopBar(isSearchVisible, searchQuery, onSearchQueryChange = { searchQuery = it }, onSearchIconClick = { isSearchVisible = !isSearchVisible }) },
//        bottomBar = { BottomNavigationBar(navController) }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .fillMaxSize()
//                .background(Color(0xFFCCFFFF))
//        ) {
//            TabRowExample()
//            NewsList(newsItems, searchQuery)
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TopBar(isSearchVisible: Boolean, searchQuery: String, onSearchQueryChange: (String) -> Unit, onSearchIconClick: () -> Unit) {
//    TopAppBar(
//        title = {
//            AnimatedVisibility(
//                visible = isSearchVisible,
//                enter = fadeIn(),
//                exit = fadeOut()
//            ) {
//                TextField(
//                    value = searchQuery,
//                    onValueChange = onSearchQueryChange,
//                    placeholder = { Text("ค้นหา...") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp),
//                    colors = TextFieldDefaults.textFieldColors(
//                        containerColor = Color.White,
//                        focusedIndicatorColor = Color.Transparent,
//                        unfocusedIndicatorColor = Color.Transparent
//                    )
//                )
//            }
//        },
//        actions = {
//            IconButton(onClick = onSearchIconClick) {
//                Icon(painter = painterResource(id = R.drawable.ic_search), contentDescription = "Search")
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
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(70.dp)
//                    .background(Color.Gray, shape = RoundedCornerShape(8.dp)) // รูปภาพมุมโค้งมน
//            )
//            Spacer(modifier = Modifier.width(16.dp))
//            Column {
//                Text(text = news, fontSize = 20.sp, fontWeight = FontWeight.Bold)
//                Text(text = "by Admin | 6 ชั่วโมงที่แล้ว", fontSize = 14.sp)
//            }
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
        "ข่าว 1",
        "ข่าว 2",
        "ข่าว 3",
        "ข่าว 4",
        "ข่าว 5"
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
                    .fillMaxWidth()
                    .padding(horizontal =5.dp), // เพิ่ม padding แนวนอน
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start // จัดเรียงให้อยู่ทางซ้าย
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("ค้นหา...") },
                    modifier = Modifier
                        .fillMaxWidth(0.9f), // ใช้พื้นที่ 90% ของความกว้างทั้งหมด
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
fun NewsList(newsItems: List<String>, searchQuery: String) {
    val filteredNewsItems = newsItems.filter { it.contains(searchQuery, ignoreCase = true) }

    LazyColumn {
        items(filteredNewsItems) { news ->
            NewsItem(news = news)
        }
    }
}

@Composable
fun NewsItem(news: String) {
    Card(
        shape = RoundedCornerShape(8.dp), // กำหนดมุมโค้งมน
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp) // เพิ่ม padding รอบ ๆ รายการข่าว
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(8.dp)) // รูปภาพมุมโค้งมน
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = news, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "by Admin | 6 ชั่วโมงที่แล้ว", fontSize = 14.sp)
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
