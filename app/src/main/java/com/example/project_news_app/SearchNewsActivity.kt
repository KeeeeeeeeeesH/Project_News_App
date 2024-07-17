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
import androidx.appcompat.widget.Toolbar
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

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Set up the spinner with options
        val timePeriods = arrayOf("เลือกช่วงเวลา", "ข่าวในสัปดาห์นี้", "1 สัปดาห์ที่แล้ว", "1 เดือนที่แล้ว", "6 เดือนที่แล้ว", "1 ปีที่แล้ว")
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
                    1 -> searchNewsByTimePeriod("CURRENT_WEEK")
                    2 -> searchNewsByTimePeriod("LAST_WEEK")
                    3 -> searchNewsByTimePeriod("LAST_MONTH")
                    4 -> searchNewsByTimePeriod("LAST_SIX_MONTHS")
                    5 -> searchNewsByTimePeriod("LAST_YEAR")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
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

//    override fun onBackPressed() {
//        if (foundNewsLabel.visibility == View.VISIBLE) {
//            // ถ้าอยู่ในหน้าข่าวที่พบ ให้กลับไปที่หน้าค้นหา
//            val intent = Intent(this, SearchNewsActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(intent)
//            finish()
//        } else {
//            super.onBackPressed()
//        }
//    }
}
