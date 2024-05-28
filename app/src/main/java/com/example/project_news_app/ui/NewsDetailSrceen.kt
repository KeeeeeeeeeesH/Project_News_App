// NewsDetailScreen.kt
package com.example.project_news_app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project_news_app.R

@Composable
fun NewsDetailScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "รายละเอียดข่าว") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(painterResource(id = R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Handle notification click */ }) {
                        Icon(painterResource(id = R.drawable.ic_notifications), contentDescription = "Notifications")
                    }
                },
                backgroundColor = Color(0xFFCCFFFF)
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "หัวข้อข่าว",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "หมวดหมู่: กีฬา")
            Text(text = "ระดับความสำคัญ: ปกติ")
            Text(text = "15 ม.ค. 2567 13:25 น.")
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .background(Color.Gray)
            ) {
                // This Box is a placeholder for the image.
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "เนื้อหาข่าวสาร เนื้อหาข่าวสาร",
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Icon(painterResource(id = R.drawable.ic_star), contentDescription = "Favorite")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "แท็กข่าว: ฟุตบอล, นักฟุตบอล, กีฬา")
            }
        }
    }
}
