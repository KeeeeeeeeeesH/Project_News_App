package com.example.project_news_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Message Received from: ${remoteMessage.from}")

        // ตรวจสอบว่า Data Payload ถูกส่งมา
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "ข่าวสำคัญ"
            val message = remoteMessage.data["body"] ?: "เนื้อหาใหม่"
            val newsId = remoteMessage.data["news_id"] ?: "-1"  // รับ news_id จาก Data Payload

            Log.d("FCM", "Data Payload Received: Title: $title, Body: $message, News ID: $newsId")

            if (newsId != "-1") {  // ตรวจสอบว่ามี newsId หรือไม่
                showNotification(title, message, newsId)  // ส่ง newsId ไปในฟังก์ชัน showNotification
            } else {
                Log.e("FCM", "Invalid news ID received")
            }
        }
    }

    private fun showNotification(title: String, message: String, newsId: String) {
        val channelId = "important_news_channel"
        val channelName = "Important News"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for important news notifications"
            }
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, NewsDetailsActivity::class.java).apply {
            putExtra("news_id", newsId.toInt())  // ส่ง news_id ไปที่ NewsDetailsActivity
            putExtra("news_title", title)
            putExtra("news_content", message)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notifications)  // ไอคอนของคุณ
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.blue))  // กำหนดสีที่ต้องการ
            .setColorized(true)  // เปิดให้เปลี่ยนสีได้

        try {
            with(NotificationManagerCompat.from(this)) {
                notify((System.currentTimeMillis() % 10000).toInt(), builder.build())  // เปลี่ยน ID ของ Notification ให้ไม่ซ้ำกัน
            }
        } catch (e: SecurityException) {
            e.printStackTrace()  // จัดการ error ที่อาจเกิดขึ้น
        }
    }

}
