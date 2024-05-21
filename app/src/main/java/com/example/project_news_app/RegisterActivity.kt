// RegisterActivity.kt
package com.example.project_news_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val firstNameEditText = findViewById<EditText>(R.id.first_name)
        val lastNameEditText = findViewById<EditText>(R.id.last_name)
        val phoneNumberEditText = findViewById<EditText>(R.id.phone_number)
        val emailEditText = findViewById<EditText>(R.id.email)
        val usernameEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirm_password)
        val registerButton = findViewById<Button>(R.id.register_button)
        val loginTextView = findViewById<TextView>(R.id.login)

        registerButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val phoneNumber = phoneNumberEditText.text.toString()
            val email = emailEditText.text.toString()
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                // Implement your registration logic here
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                // Navigate to login or main activity
            }
        }
        supportActionBar?.hide()

        loginTextView.setOnClickListener {
            // Navigate to login activity
            finish()
        }
    }
}
