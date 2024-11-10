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

        // disable ช่องกรอก OTP และปุ่ม OK
        otpEditText.isEnabled = false
        okButton.isEnabled = false

        // ขอOTP
        sendOtpButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString().trim()
            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกหมายเลขโทรศัพท์", Toast.LENGTH_SHORT).show()
            } else {
                requestOtp(phoneNumber)
            }
        }

        // ตรวจสอบ OTP
        okButton.setOnClickListener {
            val otp = otpEditText.text.toString().trim()
            if (otp.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกรหัส OTP", Toast.LENGTH_SHORT).show()
            } else {
                verifyOtp(otp)
            }
        }
    }

    // ฟังก์ชันสำหรับขอ OTP
    private fun requestOtp(phoneNumber: String) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val request = PhoneNumberRequest(phone = phoneNumber)

        //post request
        apiService.requestOtp(request).enqueue(object : Callback<OtpResponse> {
            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                val otpResponse = response.body()

                // ตรวจสอบว่า response ไม่เป็น null และมีข้อความ success
                if (otpResponse != null && otpResponse.message == "OTP ถูกส่งสำเร็จ") {
                    // ปลดล็อคการกรอก OTP และปุ่ม OK
                    otpEditText.isEnabled = true
                    okButton.isEnabled = true
                    // แสดง dialog ว่า OTP ถูกส่งแล้ว
                    showOtpSentDialog()
                } else {
                    // ถ้า response message ไม่ถูกต้อง
                    val errorMessage = otpResponse?.message ?: "ส่ง OTP ไม่สำเร็จ"
                    Toast.makeText(this@RecoveryActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                // เมื่อเกิดข้อผิดพลาดในการเชื่อมต่อ API
                Toast.makeText(this@RecoveryActivity, "มีข้อผิดพลาดเกิดขึ้น: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ฟังก์ชันตรวจสอบ OTP
    private fun verifyOtp(otp: String) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val request = OtpRequest(otp = otp)

        //post verify
        apiService.verifyOtp(request).enqueue(object : Callback<OtpResponse> {
            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                val otpResponse = response.body()

                // ตรวจสอบข้อความ message หรือ details จากการยืนยัน OTP
                if (otpResponse?.message == "OTP ยืนยันสำเร็จ" || otpResponse?.details?.msg == "Verify Success") {
                    // ถ้า OTP ตรงกับที่ส่งไป นำผู้ใช้ไปยังหน้า Reset Password
                    val intent = Intent(this@RecoveryActivity, ResetPasswordActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMessage = otpResponse?.message ?: "รหัส OTP ไม่ถูกต้อง"
                    Toast.makeText(this@RecoveryActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                Toast.makeText(this@RecoveryActivity, "มีข้อผิดพลาดเกิดขึ้น: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ฟังก์ชันแสดง dialog เมื่อ OTP ถูกส่งสำเร็จ
    private fun showOtpSentDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("ส่ง OTP สำเร็จ")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }
}
