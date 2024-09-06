package com.example.project_news_app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.text.SimpleDateFormat
import java.util.*

class SearchNewsActivity : AppCompatActivity() {

    private lateinit var searchByNameEditText: EditText
    private lateinit var searchByDateEditText: EditText
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var searchByNameButton: Button
    private lateinit var searchByDateButton: Button
    private lateinit var searchByDateRangeButton: Button

    private var startDate: Date? = null
    private var endDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_news)

        // Bind the views with findViewById
        searchByNameEditText = findViewById(R.id.search_by_name_edit_text)
        searchByDateEditText = findViewById(R.id.search_by_date_edit_text)
        startDateEditText = findViewById(R.id.start_date_edit_text)
        endDateEditText = findViewById(R.id.end_date_edit_text)
        searchByNameButton = findViewById(R.id.search_by_name_button)
        searchByDateButton = findViewById(R.id.search_by_date_button)
        searchByDateRangeButton = findViewById(R.id.search_by_date_range_button)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        searchByDateEditText.setOnClickListener {
            showDatePickerDialog(false, true) // สำหรับเลือกวันที่เดียว
        }

        startDateEditText.setOnClickListener {
            showDatePickerDialog(true, false) // สำหรับเลือกวันที่เริ่มต้น
        }

        endDateEditText.setOnClickListener {
            showDatePickerDialog(false, false) // สำหรับเลือกวันที่สิ้นสุด
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

        searchByDateRangeButton.setOnClickListener {
            if (startDate != null && endDate != null) {
                if (startDate!!.before(endDate)) {
                    searchNewsByDateRange(startDate!!, endDate!!)
                } else {
                    Toast.makeText(this, "วันที่เริ่มต้นต้องมาก่อนวันที่สิ้นสุด", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "กรุณาเลือกวันที่เริ่มต้นและวันที่สิ้นสุด", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePickerDialog(isStartDate: Boolean, isSingleDate: Boolean) {
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
                val dateStr = dateFormat.format(selectedDate.time)

                when {
                    isStartDate -> {
                        startDateEditText.setText(dateStr)
                        startDate = selectedDate.time
                    }
                    !isSingleDate -> {
                        endDateEditText.setText(dateStr)
                        endDate = selectedDate.time
                    }
                    else -> {
                        searchByDateEditText.setText(dateStr)
                    }
                }
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

    private fun searchNewsByDateRange(startDate: Date, endDate: Date) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val startDateString = dateFormat.format(startDate)
        val endDateString = dateFormat.format(endDate)
        val intent = Intent().apply {
            putExtra("START_DATE", startDateString)
            putExtra("END_DATE", endDateString)
            putExtra("SEARCH_TYPE", "DATE_RANGE")
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
        super.onBackPressed()
    }
}
