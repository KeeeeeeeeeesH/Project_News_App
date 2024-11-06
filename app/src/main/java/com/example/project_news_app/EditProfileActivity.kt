package com.example.project_news_app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var usernameEditText: EditText
    private lateinit var fnameEditText: EditText
    private lateinit var lnameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        usernameEditText = findViewById(R.id.usernameEditText)
        fnameEditText = findViewById(R.id.fnameEditText)
        lnameEditText = findViewById(R.id.lnameEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        emailEditText = findViewById(R.id.emailEditText)
        saveButton = findViewById(R.id.saveButton)

        // Load existing user data
        usernameEditText.setText(sharedPreferences.getString("username", ""))
        fnameEditText.setText(sharedPreferences.getString("fname", ""))
        lnameEditText.setText(sharedPreferences.getString("lname", ""))
        phoneEditText.setText(sharedPreferences.getString("phone", ""))
        emailEditText.setText(sharedPreferences.getString("email", ""))

        saveButton.setOnClickListener {
            val newUsername = usernameEditText.text.toString().trim()
            val newFname = fnameEditText.text.toString().trim()
            val newLname = lnameEditText.text.toString().trim()
            val newPhone = phoneEditText.text.toString().trim()
            val newEmail = emailEditText.text.toString().trim()

            if (newUsername.isEmpty() || newFname.isEmpty() || newLname.isEmpty() || newPhone.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบทุกช่อง", Toast.LENGTH_SHORT).show()
            } else {
                // Update user data
                val memId = sharedPreferences.getInt("memId", 0)
                val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
                val memberData = MemberData(
                    memId = memId,
                    memFname = newFname,
                    memLname = newLname,
                    memUsername = newUsername,
                    memPassword = sharedPreferences.getString("password", "") ?: "",
                    memEmail = newEmail,
                    memPhone = newPhone
                )

                apiService.updateMember(memId, memberData).enqueue(object : Callback<MemberData> {
                    override fun onResponse(call: Call<MemberData>, response: Response<MemberData>) {
                        if (response.isSuccessful) {
                            editor.putString("username", newUsername)
                            editor.putString("fname", newFname)
                            editor.putString("lname", newLname)
                            editor.putString("phone", newPhone)
                            editor.putString("email", newEmail)
                            editor.apply()
                            Toast.makeText(this@EditProfileActivity, "อัพเดทข้อมูลส่วนตัวสำเร็จ", Toast.LENGTH_SHORT).show()

                            // Set result and finish
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            Toast.makeText(this@EditProfileActivity, "อัพเดทข้อมูลส่วนตัวล้มเหลว", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<MemberData>, t: Throwable) {
                        Toast.makeText(this@EditProfileActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}