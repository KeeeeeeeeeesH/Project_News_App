//package com.example.project_news_app
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class LoginActivity : AppCompatActivity() {
//
//    private lateinit var usernameEditText: EditText
//    private lateinit var passwordEditText: EditText
//    private lateinit var loginButton: Button
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
//
//        usernameEditText = findViewById(R.id.username)
//        passwordEditText = findViewById(R.id.password)
//        loginButton = findViewById(R.id.login_button)
//
//        val registerTextView = findViewById<TextView>(R.id.register)
//        val recoveryTextView = findViewById<TextView>(R.id.recovery)
//
//        registerTextView.setOnClickListener {
//            val intent = Intent(this, RegisterActivity::class.java)
//            startActivity(intent)
//        }
//
//        recoveryTextView.setOnClickListener {
//            val intent = Intent(this, RecoveryActivity::class.java)
//            startActivity(intent)
//        }
//        supportActionBar?.hide()
//
//        loginButton.setOnClickListener {
//            val username = usernameEditText.text.toString().trim()
//            val password = passwordEditText.text.toString().trim()
//
//            if (username.isEmpty() || password.isEmpty()) {
//                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
//            } else {
//                loginMember(username, password)
//            }
//        }
//    }
//    private fun loginMember(username: String, password: String) {
//        val apiService = RetrofitClient.instance.create(ApiService::class.java)
//        val loginRequest = LoginRequest(login = username, password = password)
//
//        apiService.loginMember(loginRequest).enqueue(object : Callback<LoginResponse> {
//            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
//                if (response.isSuccessful) {
//                    val loginResponse = response.body()
//                    if (loginResponse?.success == true) {
//                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
//                        startActivity(intent)
//                        finish()
//                    } else {
//                        Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
//                }
//            }
//            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
//                Toast.makeText(this@LoginActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//}

package com.example.project_news_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.SharedPreferences

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            // User is already logged in, navigate to the main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.login_button)

        val registerTextView = findViewById<TextView>(R.id.register)
        val recoveryTextView = findViewById<TextView>(R.id.recovery)

        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        recoveryTextView.setOnClickListener {
            val intent = Intent(this, RecoveryActivity::class.java)
            startActivity(intent)
        }
        supportActionBar?.hide()

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            } else {
                loginMember(username, password)
            }
        }
    }

    private fun loginMember(username: String, password: String) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        val loginRequest = LoginRequest(login = username, password = password)

        apiService.loginMember(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse?.success == true) {
                        // Save login status and user details in SharedPreferences
                        editor.putBoolean("isLoggedIn", true)
                        editor.putInt("memId", loginResponse.user?.memId ?: 0)
                        editor.putString("fname", loginResponse.user?.memFname)
                        editor.putString("lname", loginResponse.user?.memLname)
                        editor.apply()

                        // Pass fname and lname to ProfileActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
