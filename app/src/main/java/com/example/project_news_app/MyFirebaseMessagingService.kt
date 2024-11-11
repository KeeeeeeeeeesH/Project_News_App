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
    //หลังจากได้รับข้อความจาก FCM
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Message Received from: ${remoteMessage.from}")

        // ตรวจสอบ Data Payload ถูกส่งมา
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "ข่าวสำคัญ"
            val message = remoteMessage.data["body"] ?: "เนื้อหาใหม่"
            val newsId = remoteMessage.data["news_id"] ?: "-1"  // รับ news_id จาก Data Payload

            Log.d("FCM", "Data Payload Received: Title: $title, Body: $message, News ID: $newsId")

            if (newsId != "-1") {  // ตรวจสอบว่ามีข่าวหรือไม่
                showNotification(title, message, newsId)  // แสดงแจ้งเตือนถ้ามี newsId จริง
            } else {
                Log.e("FCM", "Invalid news ID received")
            }
        }
    }

    //สร้าง channel และแสดงแจ้งเตือน
    private fun showNotification(title: String, message: String, newsId: String) {
        val channelId = "important_news_channel"
        val channelName = "Important News"

        //ถ้าเป็น android 8 ขึ้นไป ให้สร้าง Channel ก่อน
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            //เก็บข้อมูล notification channel พร้อมกำหนดคำอธิบาย
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for important news notifications"
            }
            //ลงทะเบียน เพื่อจัดการแจ้งเตือน และสร้าง notification channel
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        //เปิดหน้ารายละเอียดข่าวของแจ้งเตือน
        val intent = Intent(this, NewsDetailsActivity::class.java).apply {
            putExtra("news_id", newsId.toInt())  // ส่ง news_id ไปที่ หน้าเนื้อหาข่าว
            putExtra("news_title", title)
            putExtra("news_content", message)
        }

        //สร้าง paddingIntent ให้เปิด Activity เมื่อกดแจ้งเตือน พร้อมส่งข้อมูล
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //ปรับแต่ง builder ของ notification
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.blue))
            .setColorized(true)

        try {
            with(NotificationManagerCompat.from(this)) {
                notify((System.currentTimeMillis() % 10000).toInt(), builder.build())  // เปลี่ยน ID ของ Notification ให้ไม่ซ้ำกัน
            }
        } catch (e: SecurityException) {
            e.printStackTrace()  // จัดการ error ที่อาจเกิดขึ้น
        }
    }

}
