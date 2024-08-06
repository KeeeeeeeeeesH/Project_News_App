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

//        val memId = sharedPreferences.getInt("memId", 0)
//        val fname = sharedPreferences.getString("fname", "")
//        val lname = sharedPreferences.getString("lname", "")
//        val welcomeTextView = findViewById<TextView>(R.id.member_name)
//        welcomeTextView.text = "$fname $lname!"
        val memId = sharedPreferences.getInt("memId", 0)
        val fname = sharedPreferences.getString("fname", "")
        val lname = sharedPreferences.getString("lname", "")
        val welcomeTextView = findViewById<TextView>(R.id.member_name)
        welcomeTextView.text = "$fname $lname"

        val editProfileButton = findViewById<Button>(R.id.editProfileButton)
        val readNewsLaterButton = findViewById<Button>(R.id.readLaterButton)
        val VisitHistoryNewsButton = findViewById<Button>(R.id.NewHistoryButton)

        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivityForResult(intent, 1)
        }

        readNewsLaterButton.setOnClickListener {
            val intent = Intent(this, ReadLaterActivity::class.java)
            startActivity(intent)
        }

        VisitHistoryNewsButton.setOnClickListener {
            val intent = Intent(this, VisitHistoryActivity::class.java)
            startActivity(intent)
        }


        val logoutButton = findViewById<Button>(R.id.logout_button)
        logoutButton.setOnClickListener {
            // Clear login status
            editor.clear()
            editor.apply()

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
                    true
                }
                else -> false
            }
        }

        bottomNavigation.selectedItemId = R.id.navigation_profile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Refresh data
            val fname = sharedPreferences.getString("fname", "")
            val lname = sharedPreferences.getString("lname", "")
            val welcomeTextView = findViewById<TextView>(R.id.member_name)
            welcomeTextView.text = "$fname $lname!"
        }
    }
}
