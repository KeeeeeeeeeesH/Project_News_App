package com.example.project_news_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RecoveryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery)

        val phoneNumberEditText = findViewById<EditText>(R.id.phone_number)
        val sendOtpTextView = findViewById<TextView>(R.id.send_otp)
        val otpEditText = findViewById<EditText>(R.id.otp)
        val okButton = findViewById<Button>(R.id.ok_button)

        okButton.isEnabled = false
        otpEditText.isEnabled = false

        supportActionBar?.hide()

        sendOtpTextView.setOnClickListener {
            val mem_Phone = phoneNumberEditText.text.toString()
            if (mem_Phone.isEmpty()) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "OTP sent to $mem_Phone", Toast.LENGTH_SHORT).show()
                okButton.isEnabled = true
                otpEditText.isEnabled = true
            }
        }

        okButton.setOnClickListener {
            val otp = otpEditText.text.toString()
            if (otp.isEmpty()) {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "OTP verified", Toast.LENGTH_SHORT).show()
                //setContentView(R.layout.activity_main)
            }
        }
    }
}