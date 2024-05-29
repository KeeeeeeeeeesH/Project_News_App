package com.example.project_news_app.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.project_news_app.LoginActivity
import com.example.project_news_app.R


@Composable
fun UserProfileScreen(navController: NavController) {
    val context = LocalContext.current
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
                .background(Color(0xFFCCFFFF)) // กำหนดพื้นหลังของ Column เป็นสีฟ้า
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "มานะ ขยันดี", // ชื่อผู้ใช้
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            ProfileOption(
                label = "แก้ไขข้อมูลส่วนตัว",
                icon = R.drawable.ic_edit,
                onClick = { navController.navigate("edit_user") }
            )
            ProfileOption(
                label = "อ่านภายหลัง",
                icon = R.drawable.ic_clock,
                onClick = { navController.navigate("read_later") }
            )
            ProfileOption(
                label = "ประวัติการอ่าน",
                icon = R.drawable.ic_clock,
                onClick = { navController.navigate("read_history") }
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)},
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .background(Color(0xFFCCFFFF)) // พื้นหลังสีฟ้า
            ) {
                Text(text = "ออกจากระบบ")
            }
        }
    }
}

@Composable
fun ProfileOption(label: String, icon: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontSize = 20.sp)
    }
}