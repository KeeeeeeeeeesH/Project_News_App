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

        otpEditText.isEnabled = false
        okButton.isEnabled = false

        sendOtpButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString().trim()
            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
            } else {
                requestOtp(phoneNumber)
            }
        }

        okButton.setOnClickListener {
            val otp = otpEditText.text.toString().trim()
            if (otp.isEmpty()) {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            } else {
                verifyOtp(otp)
            }
        }
    }

    private fun requestOtp(phoneNumber: String) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        val request = PhoneNumberRequest(phone = phoneNumber)

        apiService.requestOtp(request).enqueue(object : Callback<OtpResponse> {
            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    showOtpSentDialog()
                    otpEditText.isEnabled = true
                    okButton.isEnabled = true
                } else {
                    Toast.makeText(this@RecoveryActivity, response.body()?.message ?: "Failed to send OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                Toast.makeText(this@RecoveryActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun verifyOtp(otp: String) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        val request = OtpRequest(otp = otp)

        apiService.verifyOtp(request).enqueue(object : Callback<OtpResponse> {
            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val intent = Intent(this@RecoveryActivity, ResetPasswordActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@RecoveryActivity, response.body()?.message ?: "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                Toast.makeText(this@RecoveryActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun showOtpSentDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("OTP Send Successful")
            .setPositiveButton("OK") { dialog, id ->
                dialog.dismiss()
            }
        builder.create().show()
    }
}
