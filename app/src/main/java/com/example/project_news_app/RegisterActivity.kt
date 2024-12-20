package com.example.project_news_app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firstNameEditText = findViewById(R.id.first_name)
        lastNameEditText = findViewById(R.id.last_name)
        phoneNumberEditText = findViewById(R.id.phone_number)
        emailEditText = findViewById(R.id.email)
        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        confirmPasswordEditText = findViewById(R.id.confirm_password)
        registerButton = findViewById(R.id.register_button)

        registerButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val phoneNumber = phoneNumberEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            //เงื่อนไข
            if (firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบทุกช่อง", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "รหัสผ่านไม่ตรงกัน", Toast.LENGTH_SHORT).show()
            } else {
                registerMember(firstName, lastName, phoneNumber, email, username, password)
            }
        }
    }

    private fun registerMember(firstName: String, lastName: String, phoneNumber: String, email: String, username: String, password: String) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        //object เก็บข้อมูล
        val memberData = MemberData(
            memId = 0,
            memFname = firstName,
            memLname = lastName,
            memUsername = username,
            memPassword = password,
            memEmail = email,
            memPhone = phoneNumber,
        )

        //post ข้อมูล
        apiService.postMember(memberData).enqueue(object : Callback<MemberData> {
            override fun onResponse(call: Call<MemberData>, response: Response<MemberData>) {
                if (response.isSuccessful) {
                    showSuccessDialog()
                } else {
                    handleRegistrationError(response)
                }
            }

            override fun onFailure(call: Call<MemberData>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "มีข้อผิดพลาดเกิดขึ้น: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showSuccessDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("สมัครสมาชิกสำเร็จ")
            .setPositiveButton("OK") { dialog, id ->
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        builder.create().show()
    }
    private fun handleRegistrationError(response: Response<MemberData>) {
        val errorMessage = when (response.code()) {
            409 -> "ชื่อผู้ใช้งาน, อีเมล์, หรือหมายเลขโทรศัพท์นี้ มีอยู่ในระบบแล้ว"
            else -> "สมัครสมาชิกไม่สำเร็จ: ${response.message()}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
