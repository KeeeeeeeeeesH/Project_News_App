//package com.example.project_news_app
//
//import android.os.Bundle
//import android.widget.Button
//import android.widget.CheckBox
//import android.widget.LinearLayout
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class CategorySelectionActivity : AppCompatActivity() {
//
//    private lateinit var categoriesLayoutLeft: LinearLayout
//    private lateinit var categoriesLayoutRight: LinearLayout
//    private lateinit var okButton: Button
//    private val selectedCategories = mutableListOf<String>()
//    private val checkBoxes = mutableListOf<CheckBox>()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_category_selection)
//
//        categoriesLayoutLeft = findViewById(R.id.categories_layout_left)
//        categoriesLayoutRight = findViewById(R.id.categories_layout_right)
//        okButton = findViewById(R.id.btn_ok)
//
//        loadCategories()
//
//        okButton.setOnClickListener {
//            selectedCategories.clear()
//            for (checkBox in checkBoxes) {
//                if (checkBox.isChecked) {
//                    selectedCategories.add(checkBox.text.toString())
//                }
//            }
//            Toast.makeText(this, "Selected Categories: ${selectedCategories.joinToString(", ")}", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    private fun loadCategories() {
//        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
//        apiService.getCategory().enqueue(object : Callback<List<CategoryData>> {
//            override fun onResponse(call: Call<List<CategoryData>>, response: Response<List<CategoryData>>) {
//                if (response.isSuccessful) {
//                    val categories = response.body() ?: listOf()
//                    updateCategoryUI(categories)
//                } else {
//                    Toast.makeText(this@CategorySelectionActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<List<CategoryData>>, t: Throwable) {
//                Toast.makeText(this@CategorySelectionActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun updateCategoryUI(categories: List<CategoryData>) {
//        checkBoxes.clear()
//        categoriesLayoutLeft.removeAllViews()
//        categoriesLayoutRight.removeAllViews()
//
//        val halfSize = (categories.size + 1) / 2
//        categories.forEachIndexed { index, category ->
//            val checkBox = CheckBox(this).apply {
//                text = category.catName
//                setPadding(8, 8, 8, 8)
//                setBackgroundColor(resources.getColor(R.color.light_gray, null))
//                setButtonTintList(resources.getColorStateList(R.color.black, null))
//                setTextColor(resources.getColor(R.color.black, null))
//            }
//            checkBoxes.add(checkBox)
//            if (index < halfSize) {
//                categoriesLayoutLeft.addView(checkBox)
//            } else {
//                categoriesLayoutRight.addView(checkBox)
//            }
//        }
//    }
//}
package com.example.project_news_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategorySelectionActivity : AppCompatActivity() {

    private lateinit var categoriesLayoutLeft: LinearLayout
    private lateinit var categoriesLayoutRight: LinearLayout
    private lateinit var okButton: Button
    private lateinit var bottomNavigationView: BottomNavigationView
    private val selectedCategories = mutableListOf<String>()
    private val checkBoxes = mutableListOf<CheckBox>()
    private val categoryMap = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_selection)

        categoriesLayoutLeft = findViewById(R.id.categories_layout_left)
        categoriesLayoutRight = findViewById(R.id.categories_layout_right)
        okButton = findViewById(R.id.btn_ok)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        loadCategories()

        okButton.setOnClickListener {
            selectedCategories.clear()
            for (checkBox in checkBoxes) {
                if (checkBox.isChecked) {
                    selectedCategories.add(checkBox.text.toString())
                }
            }

            // สร้าง Intent เพื่อไปยัง NewsFavoriteActivity
            val intent = Intent(this, NewsFavoriteActivity::class.java)
            intent.putStringArrayListExtra("selectedCategories", ArrayList(selectedCategories))
            startActivity(intent)
        }

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    // Implement navigation to home
                    true
                }
                R.id.navigation_favorite -> {
                    startActivity(Intent(this, FavoriteActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadCategories() {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getCategory().enqueue(object : Callback<List<CategoryData>> {
            override fun onResponse(call: Call<List<CategoryData>>, response: Response<List<CategoryData>>) {
                if (response.isSuccessful) {
                    val categories = response.body() ?: listOf()
                    updateCategoryUI(categories)
                    // แปลงชื่อหมวดหมู่เป็น ID และจัดเก็บใน Map
                    categories.forEach { category ->
                        categoryMap[category.catName] = category.catId
                    }
                } else {
                    Toast.makeText(this@CategorySelectionActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<CategoryData>>, t: Throwable) {
                Toast.makeText(this@CategorySelectionActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateCategoryUI(categories: List<CategoryData>) {
        checkBoxes.clear()
        categoriesLayoutLeft.removeAllViews()
        categoriesLayoutRight.removeAllViews()

        val halfSize = (categories.size + 1) / 2
        categories.forEachIndexed { index, category ->
            val checkBox = CheckBox(this).apply {
                text = category.catName
                textSize = 18f // เพิ่มขนาดตัวอักษร
                setPadding(16, 16, 16, 16) // เพิ่ม padding
                background = ContextCompat.getDrawable(this@CategorySelectionActivity, R.color.light_gray)
                buttonTintList = ContextCompat.getColorStateList(this@CategorySelectionActivity, android.R.color.black)
                setTextColor(ContextCompat.getColor(this@CategorySelectionActivity, android.R.color.black))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    300 // ตั้งค่าความสูงเป็น 100 พิกเซล หรือปรับตามที่ต้องการ
                ).apply {
                    setMargins(16, 16, 16, 16) // เพิ่ม margins
                }
            }
            checkBoxes.add(checkBox)
            if (index < halfSize) {
                categoriesLayoutLeft.addView(checkBox)
            } else {
                categoriesLayoutRight.addView(checkBox)
            }
        }
    }

//    private fun getCategoryID(categoryName: String): Int {
//        return categoryMap[categoryName] ?: 0
//    }
}
