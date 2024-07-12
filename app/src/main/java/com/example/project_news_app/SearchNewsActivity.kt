package com.example.project_news_app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class SearchNewsActivity : AppCompatActivity() {

    private lateinit var searchByNameEditText: EditText
    private lateinit var searchByDateEditText: EditText
    private lateinit var searchByNameButton: Button
    private lateinit var searchByDateButton: Button
    private lateinit var timePeriodSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_news)

        searchByNameEditText = findViewById(R.id.search_by_name_edit_text)
        searchByDateEditText = findViewById(R.id.search_by_date_edit_text)
        searchByNameButton = findViewById(R.id.search_by_name_button)
        searchByDateButton = findViewById(R.id.search_by_date_button)
        timePeriodSpinner = findViewById(R.id.time_period_spinner)

        // Set up the spinner with options
        val timePeriods = arrayOf("เลือกช่วงเวลา", "1 สัปดาห์ที่แล้ว", "1 เดือนที่แล้ว", "1 ปีที่แล้ว")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timePeriods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timePeriodSpinner.adapter = adapter

        searchByDateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        searchByNameButton.setOnClickListener {
            val query = searchByNameEditText.text.toString()
            if (query.isNotBlank()) {
                searchNewsByName(query)
            } else {
                Toast.makeText(this, "กรุณากรอกชื่อหรือหัวข้อข่าว", Toast.LENGTH_SHORT).show()
            }
        }

        searchByDateButton.setOnClickListener {
            val dateString = searchByDateEditText.text.toString()
            if (dateString.isNotBlank()) {
                try {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = dateFormat.parse(dateString)
                    searchNewsByDate(date)
                } catch (e: Exception) {
                    Toast.makeText(this, "กรุณาเลือกวันที่ที่ถูกต้อง", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "กรุณาเลือกวันที่", Toast.LENGTH_SHORT).show()
            }
        }

        timePeriodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (position) {
                    1 -> searchNewsByTimePeriod("WEEK")
                    2 -> searchNewsByTimePeriod("MONTH")
                    3 -> searchNewsByTimePeriod("YEAR")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    // กลับไปที่หน้า Home
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_favorite -> {
                    // อยู่ที่หน้า Favorite
                    true
                }
                R.id.navigation_profile -> {
                    // ไปที่หน้า Profile
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                searchByDateEditText.setText(dateFormat.format(selectedDate.time))
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun searchNewsByName(query: String) {
        val intent = Intent().apply {
            putExtra("SEARCH_QUERY", query)
            putExtra("SEARCH_TYPE", "NAME")
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun searchNewsByDate(date: Date) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateString = dateFormat.format(date)
        val intent = Intent().apply {
            putExtra("SEARCH_QUERY", dateString)
            putExtra("SEARCH_TYPE", "DATE")
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun searchNewsByTimePeriod(period: String) {
        val intent = Intent().apply {
            putExtra("SEARCH_QUERY", period)
            putExtra("SEARCH_TYPE", "PERIOD")
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}
