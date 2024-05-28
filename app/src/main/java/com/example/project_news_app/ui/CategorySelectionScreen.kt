package com.example.project_news_app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project_news_app.R

@Composable
fun CategorySelectionScreen(navController: NavController) {
    val categories = listOf(
        "เกมส์", "ไลฟ์สไตล์", "บันเทิง", "ท่องเที่ยว", "เศรษฐกิจ", "เทคโนโลยี",
        "กีฬา", "สุขภาพ", "สังคม", "ดิจิทัล", "ฟุตบอล", "อาหาร"
    )
    val selectedCategories = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        modifier = Modifier.background(Color(0xFFCCFFFF)), // กำหนดพื้นหลังของ Scaffold เป็นสีฟ้า
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFCCFFFF)), // กำหนดพื้นหลังของ Column เป็นสีฟ้า
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "หมวดหมู่",
                fontSize = 24.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // ใช้ weight เพื่อให้ LazyColumn เต็มพื้นที่ที่เหลือ
                    .background(Color(0xFFCCFFFF)), // กำหนดพื้นหลังของ LazyColumn เป็นสีฟ้า
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(categories.chunked(2)) { rowCategories ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp), // เพิ่ม padding เพื่อให้มีระยะห่างระหว่าง row
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowCategories.forEach { category ->
                            CategoryItem(
                                category = category,
                                isSelected = selectedCategories[category] ?: false,
                                onSelectionChanged = { selected ->
                                    selectedCategories[category] = selected
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* TODO: Handle OK click */ },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(text = "OK")
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: String,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(8.dp)
            .width(150.dp) // กำหนดความกว้างคงที่
            .height(60.dp) // กำหนดความสูงคงที่
            .clickable { onSelectionChanged(!isSelected) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center, // จัดให้อยู่ตรงกลางแนวนอน
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectionChanged(!isSelected) },
                colors = CheckboxDefaults.colors(checkmarkColor = Color.Black)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category,
                fontSize = 16.sp,
                color = Color.Black // กำหนดสีของข้อความให้มองเห็นได้ชัดเจน
            )
        }
    }
}
