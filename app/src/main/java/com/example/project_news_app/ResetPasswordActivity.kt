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

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var resetPasswordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resetpassword)

        newPasswordEditText = findViewById(R.id.etNewPassword)
        confirmPasswordEditText = findViewById(R.id.etConfirmPassword)
        resetPasswordButton = findViewById(R.id.btnResetPassword)

        resetPasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                resetPassword(newPassword, confirmPassword)
            }
        }
    }

    private fun resetPassword(newPassword: String, confirmPassword: String) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        val request = ResetPasswordRequest(newPassword, confirmPassword)

        apiService.resetPassword(request).enqueue(object : Callback<ResetPasswordResponse> {
            override fun onResponse(call: Call<ResetPasswordResponse>, response: Response<ResetPasswordResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    showResetSuccessDialog()
                } else {
                    val errorMessage = response.body()?.message ?: "Reset password failed"
                    Toast.makeText(this@ResetPasswordActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResetPasswordResponse>, t: Throwable) {
                Toast.makeText(this@ResetPasswordActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showResetSuccessDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Password reset successful")
            .setPositiveButton("OK") { dialog, id ->
                dialog.dismiss()
                val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        builder.create().show()
    }
}
