package com.example.project_news_app.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
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
fun NewsFeedScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var isSubcategoryVisible by remember { mutableStateOf(false) } // Default to false to hide subcategories initially
    val newsItems = listOf(
        NewsData("ชื่อข่าว 1", "admin", "วันที่ วง/ดด/ปปป", 4567, 4.55),
        NewsData("ชื่อข่าว 2", "admin", "วันที่ วง/ดด/ปปป", 1234, 4.25),
        NewsData("ชื่อข่าว 3", "admin", "วันที่ วง/ดด/ปปป", 7890, 4.75),
        NewsData("ชื่อข่าว 4", "admin", "วันที่ วง/ดด/ปปป", 5678, 4.45)
    )

    ChangeStatusBarColor(color = Color(0xFFCCFFFF))

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ScrollableTabRow(
                    selectedTabIndex = 0, // Add your selectedTabIndex logic here
                    modifier = Modifier.weight(1f)
                ) {
                    val tabTitles = listOf("แนะนำ", "เทคโนโลยี", "เกม", "สุขภาพ", "ท่องเที่ยว", "test")
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = false, // Replace with your selected state logic
                            onClick = { /* Handle tab click */ },
                            text = { Text(text = title) }
                        )
                    }
                }
                IconButton(
                    onClick = { isSubcategoryVisible = !isSubcategoryVisible },
                    modifier = Modifier.padding(end = 8.dp) // Adjust padding as needed
                ) {
                    Icon(
                        imageVector = if (isSubcategoryVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isSubcategoryVisible) "Collapse" else "Expand"
                    )
                }
            }
            if (isSubcategoryVisible) {
                SubTabRowExample()
            }
            NewsList(newsItems, searchQuery, navController)
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
                        .fillMaxWidth()  // ปรับขนาดความกว้างของ TextField
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
fun SubTabRowExample() {
    val subTabTitles = listOf("หมวดย่อย 1", "หมวดย่อย 2", "หมวดย่อย 3", "หมวดย่อย 4")
    var selectedSubTabIndex by remember { mutableStateOf(0) }

    ScrollableTabRow(
        selectedTabIndex = selectedSubTabIndex,
        containerColor = Color.LightGray
    ) {
        subTabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedSubTabIndex == index,
                onClick = { selectedSubTabIndex = index },
                text = { Text(text = title) }
            )
        }
    }
}

@Composable
fun NewsList(newsItems: List<NewsData>, searchQuery: String, navController: NavController) {
    val filteredNewsItems = newsItems.filter { it.title.contains(searchQuery, ignoreCase = true) }

    val scrollState = rememberScrollState()

    Column(modifier = Modifier.verticalScroll(scrollState)) {
        filteredNewsItems.forEach { news ->
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

@Composable
fun NewsItem(newsTitle: String, admin: String, date: String, views: Int, rating: Double, navController: NavController) {
    Card(
        shape = RoundedCornerShape(8.dp), // กำหนดมุมโค้งมน
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp) // เพิ่ม padding รอบ ๆ รายการข่าว
            .background(Color(0xFFCCFFFF))
            .clickable { navController.navigate("news_detail") } // เพิ่มการคลิกเพื่อนำทางไปยังหน้ารายละเอียดข่าว
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
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .border(1.dp, Color.Black) // เพิ่มขอบดำรอบๆ BottomNavigationBar
            .fillMaxWidth()
    ){
        NavigationBar(
            containerColor = Color(0xFFCCFFFF)
        ) {
            NavigationBarItem(
                icon = { Icon(painterResource(id = R.drawable.ic_star), contentDescription = "Favorites") },
                label = { Text("Favorites") },
                selected = currentRoute == "favorite_category",
                onClick = { navController.navigate("favorite_category") }
            )
            NavigationBarItem(
                icon = { Icon(painterResource(id = R.drawable.ic_home), contentDescription = "Home") },
                label = { Text("Home") },
                selected = currentRoute == "news_feed",
                onClick = { navController.navigate("news_feed") }
            )
            NavigationBarItem(
                icon = { Icon(painterResource(id = R.drawable.ic_account), contentDescription = "Profile") },
                label = { Text("Profile") },
                selected = currentRoute == "user_profile",
                onClick = { navController.navigate("user_profile") }
            )
        }
    }
}
