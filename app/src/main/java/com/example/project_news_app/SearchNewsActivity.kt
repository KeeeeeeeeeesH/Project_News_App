package com.example.project_news_app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

        searchByNameEditText = findViewById(R.id.search_by_name_edit_text)
        searchByDateEditText = findViewById(R.id.search_by_date_edit_text)
        startDateEditText = findViewById(R.id.start_date_edit_text)
        endDateEditText = findViewById(R.id.end_date_edit_text)
        searchByNameButton = findViewById(R.id.search_by_name_button)
        searchByDateButton = findViewById(R.id.search_by_date_button)
        searchByDateRangeButton = findViewById(R.id.search_by_date_range_button)

        //ตั้งค่า toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        //datepicker
        searchByDateEditText.setOnClickListener {
            showDatePickerDialog(false, true) // สำหรับเลือกวันที่อย่างเดียวเดียว
        }

        startDateEditText.setOnClickListener {
            showDatePickerDialog(true, false) // สำหรับเลือกวันที่เริ่มต้น
        }

        endDateEditText.setOnClickListener {
            showDatePickerDialog(false, false) // สำหรับเลือกวันที่สิ้นสุด
        }

        //ปุ่มค้นหาจากชื่อ
        searchByNameButton.setOnClickListener {
            val query = searchByNameEditText.text.toString()
            if (query.isNotBlank()) {
                searchNewsByName(query)
            } else {
                Toast.makeText(this, "กรุณากรอกชื่อหรือหัวข้อข่าว", Toast.LENGTH_SHORT).show()
            }
        }

        //ปุ่มค้นหาจากวันที่
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

        //ปุ่มค้นหาจากช่วงวันที่
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

    //แสดง datepicker
    private fun showDatePickerDialog(isStartDate: Boolean, isSingleDate: Boolean) {
        //สร้าง instance และเก็บวันเดือนปี
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        //สร้างตัวแปรมาเก็บวันที่เลือก
        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            //สร้าง instance อีกครั้งเพื่อเก็บวันที่เลือก
            val selectedDate = Calendar.getInstance()
            selectedDate.set(selectedYear, selectedMonth, selectedDay)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            //เก็บวันที่เลือกพร้อม format
            val dateStr = dateFormat.format(selectedDate.time)

                when {
                    //ถ้า isStart เป็น true คือวันที่เริ่มต้น
                    isStartDate -> {
                        startDateEditText.setText(dateStr)
                        startDate = selectedDate.time
                    }
                    //ถ้า isSingle เป็น false คือวันที่สิ้นสุด
                    !isSingleDate -> {
                        endDateEditText.setText(dateStr)
                        endDate = selectedDate.time
                    }
                    //ถ้า false ทั้งคู่คือเลือกวันที่เฉยๆ
                    else -> {
                        searchByDateEditText.setText(dateStr)
                    }
                }
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    //ค้นหาจากชื่อ
    private fun searchNewsByName(query: String) {
        //ส่งข้อมูลไปหน้าแสดงผล
        val intent = Intent(this, SearchResultActivity::class.java).apply {
            putExtra("SEARCH_QUERY", query)
            putExtra("SEARCH_TYPE", "NAME")
        }
        startActivity(intent)
    }

    //ค้นหาจากวันที่
    private fun searchNewsByDate(date: Date) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateString = dateFormat.format(date)
        //ส่งข้อมูลไปหน้าแสดงผล
        val intent = Intent(this, SearchResultActivity::class.java).apply {
            putExtra("SEARCH_QUERY", dateString)
            putExtra("SEARCH_TYPE", "DATE")
        }
        startActivity(intent)
    }

    //ค้นหาจากช่วงวันที่
    private fun searchNewsByDateRange(startDate: Date, endDate: Date) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val startDateString = dateFormat.format(startDate)
        val endDateString = dateFormat.format(endDate)
        //ส่งข้อมูลไปหน้าแสดงผล
        val intent = Intent(this, SearchResultActivity::class.java).apply {
            putExtra("START_DATE", startDateString)
            putExtra("END_DATE", endDateString)
            putExtra("SEARCH_TYPE", "DATE_RANGE")
        }
        startActivity(intent)
    }
}
