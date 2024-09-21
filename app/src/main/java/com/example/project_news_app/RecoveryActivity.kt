package com.example.project_news_app

import android.app.AlertDialog
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

class RecoveryActivity : AppCompatActivity() {

    private lateinit var phoneNumberEditText: EditText
    private lateinit var otpEditText: EditText
    private lateinit var sendOtpButton: TextView
    private lateinit var okButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery)

        phoneNumberEditText = findViewById(R.id.phone_number)
        otpEditText = findViewById(R.id.otp)
        sendOtpButton = findViewById(R.id.send_otp)
        okButton = findViewById(R.id.ok_button)

        // เริ่มต้น disable ช่องกรอก OTP และปุ่ม OK
        otpEditText.isEnabled = false
        okButton.isEnabled = false

        // ฟังก์ชันสำหรับการขอ OTP
        sendOtpButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString().trim()
            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
            } else {
                // ยังคงทำการขอ OTP
                requestOtp(phoneNumber)
            }
        }

        // ฟังก์ชันสำหรับตรวจสอบ OTP
        okButton.setOnClickListener {
            val otp = otpEditText.text.toString().trim()
            if (otp.isEmpty()) {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            } else {
                verifyOtp(otp)
            }
        }
    }

    // ฟังก์ชันสำหรับขอ OTP
    private fun requestOtp(phoneNumber: String) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val request = PhoneNumberRequest(phone = phoneNumber)

        apiService.requestOtp(request).enqueue(object : Callback<OtpResponse> {
            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                val otpResponse = response.body()

                // ตรวจสอบว่า OTP ส่งสำเร็จจากข้อความ msg ใน response body
                if (otpResponse?.details?.msg == "[ACCEPTD] Message is in accepted state") {
                    // ปลดล็อคการกรอก OTP และปุ่ม OK เมื่อ OTP ส่งสำเร็จ
                    otpEditText.isEnabled = true
                    okButton.isEnabled = true

                    // แสดง dialog ว่า OTP ถูกส่งแล้ว
                    showOtpSentDialog()
                } else {
                    // ถ้า response body ไม่ถูกต้อง แสดงข้อความว่า "Failed to send OTP"
                    Toast.makeText(this@RecoveryActivity, otpResponse?.message ?: "Failed to send OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                // แสดงข้อความเมื่อเกิดข้อผิดพลาดในการเชื่อมต่อ API
                Toast.makeText(this@RecoveryActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ฟังก์ชันสำหรับตรวจสอบ OTP
    private fun verifyOtp(otp: String) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val request = OtpRequest(otp = otp)

        apiService.verifyOtp(request).enqueue(object : Callback<OtpResponse> {
            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                val otpResponse = response.body()

                if (otpResponse?.details?.msg == "[ACCEPTD] Message is in accepted state") {
                    // ถ้า OTP ตรงกับที่ส่งไป นำผู้ใช้ไปยังหน้า Reset Password
                    val intent = Intent(this@RecoveryActivity, ResetPasswordActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@RecoveryActivity, otpResponse?.message ?: "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                Toast.makeText(this@RecoveryActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ฟังก์ชันแสดง dialog เมื่อ OTP ถูกส่งสำเร็จ
    private fun showOtpSentDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("OTP Sent Successfully")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }
}
