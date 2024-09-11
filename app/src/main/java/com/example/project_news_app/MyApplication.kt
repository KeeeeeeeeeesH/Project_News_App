package com.example.project_news_app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // ตรวจสอบว่าเวอร์ชัน Android เป็น Oreo หรือสูงกว่า
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "important_news_channel"
            val channelName = "Important News"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for important news notifications"
            }

            // ลงทะเบียน Notification Channel กับระบบ
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
