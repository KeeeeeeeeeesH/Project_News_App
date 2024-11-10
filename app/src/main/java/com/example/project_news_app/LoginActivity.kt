package com.example.project_news_app

import NetworkUtil
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //setting sharedPref
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        //check login status
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.login_button)

        val registerTextView = findViewById<TextView>(R.id.register)
        val recoveryTextView = findViewById<TextView>(R.id.recovery)

        //เข้าหน้า register
        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        //เข้าหน้า recovery
        recoveryTextView.setOnClickListener {
            val intent = Intent(this, RecoveryActivity::class.java)
            startActivity(intent)
        }
        supportActionBar?.hide()

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกชื่อผู้ใช้หรืออีเมล์ และรหัสผ่าน", Toast.LENGTH_SHORT).show()
            } else {
                loginMember(username, password)
            }
        }

        // ตรวจสอบการเชื่อมต่อ internet
        if (!NetworkUtil.isInternetAvailable(this)) {
            showInternetErrorDialog()
            return
        }
    }

    private fun showInternetErrorDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("ไม่มีการเชื่อมต่ออินเทอร์เน็ต")
            .setMessage("ไม่สามารถเข้าสู่แอปพลิเคชันได้ โปรดตรวจสอบการเชื่อมต่ออินเทอร์เน็ตของคุณ")
            .setPositiveButton("ตกลง") { _, _ -> finish() }
            .create()
        dialog.show()
    }

    private fun loginMember(username: String, password: String) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val loginRequest = LoginRequest(login = username, password = password)

        apiService.loginMember(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    Log.d(
                        "loginMember",
                        "Response: $loginResponse"
                    )
                    if (loginResponse?.success == true) {
                        // บันทึกข้อมูลการล็อคอินเข้า SharedPreference
                        editor.putBoolean("isLoggedIn", true)
                        editor.putInt("memId", loginResponse.user?.memId ?: 0)
                        editor.putString("username", loginResponse.user?.memUsername)
                        editor.putString("fname", loginResponse.user?.memFname)
                        editor.putString("lname", loginResponse.user?.memLname)
                        editor.putString("phone", loginResponse.user?.memPhone)
                        editor.putString("email", loginResponse.user?.memEmail)
                        editor.apply()
                        //เข้าหน้า Main
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            loginResponse?.message ?: "ล็อคอินไม่สำเร็จ เกิดข้อผิดพลาด",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "ชื่อผู้ใช้อีเมล์ หรือรหัสผ่านไม่ถูกต้อง",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(
                    this@LoginActivity,
                    "เกิดข้อผิดพลาด: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}

