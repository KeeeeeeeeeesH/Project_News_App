//package com.example.project_news_app
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.material.bottomnavigation.BottomNavigationView
//
//class ProfileActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_profile)
//
//        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
//        bottomNavigation.setOnNavigationItemSelectedListener {
//            when (it.itemId) {
//                R.id.navigation_home -> {
//                    startActivity(Intent(this, MainActivity::class.java))
//                    true
//                }
//                R.id.navigation_favorite -> {
//                    startActivity(Intent(this, FavoriteActivity::class.java))
//                    true
//                }
//                R.id.navigation_profile -> {
//                    // Already on Profile, do nothing
//                    true
//                }
//                else -> false
//            }
//        }
//
//        // Set the selected item as profile
//        bottomNavigation.selectedItemId = R.id.navigation_profile
//    }
//}

package com.example.project_news_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        val memId = sharedPreferences.getInt("memId", 0)
        val fname = sharedPreferences.getString("fname", "")
        val lname = sharedPreferences.getString("lname", "")
        val welcomeTextView = findViewById<TextView>(R.id.member_name)
        welcomeTextView.text = "$fname $lname!"

        val logoutButton = findViewById<Button>(R.id.logout_button)
        logoutButton.setOnClickListener {
            // Clear login status
            editor.clear()
            editor.apply()

            // Navigate back to login activity and clear the back stack
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.navigation_favorite -> {
                    startActivity(Intent(this, FavoriteActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    // Already on Profile, do nothing
                    true
                }
                else -> false
            }
        }

        // Set the selected item as profile
        bottomNavigation.selectedItemId = R.id.navigation_profile
    }
}
