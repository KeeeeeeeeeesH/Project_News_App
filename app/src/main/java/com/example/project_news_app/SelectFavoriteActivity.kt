package com.example.project_news_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectFavoriteActivity : AppCompatActivity() {

    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var categoryScrollView: ScrollView
    private lateinit var selectCategoryButton: Button
    private lateinit var saveButton: Button
    private lateinit var categoriesContainer: GridLayout
    private val selectedCategories = mutableListOf<Int>()
    private var memId: Int = -1
    private val existingCategories = mutableListOf<Int>() //เก็บหมวดหมู่ที่มีอยู่
    private lateinit var textSelectFavorite: TextView

    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_favorite)

        emptyStateContainer = findViewById(R.id.empty_state_container)
        categoryScrollView = findViewById(R.id.category_scroll_view)
        selectCategoryButton = findViewById(R.id.select_category_button)
        saveButton = findViewById(R.id.save_button)
        categoriesContainer = findViewById(R.id.categories_container)
        textSelectFavorite = findViewById(R.id.text_select_favorite)

        //จัดการ bottom navbar
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_favorite -> true
                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        //SharedPref
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        memId = sharedPreferences.getInt("memId", -1)

        if (memId != -1) {
            isEditing = intent.getBooleanExtra("isEditing", false) // ดึงค่าจาก Intent
            fetchFavoriteCategories(memId)
        } else {
            Toast.makeText(this, "ไม่พบข้อมูลสมาชิก", Toast.LENGTH_SHORT).show()
            showEmptyState()
        }

        //ไปเลือกหมวดหมู่
        selectCategoryButton.setOnClickListener {
            showCategorySelection()
        }

        //ไปบันทึกหมวดหมู่
        saveButton.setOnClickListener {
            saveFavoriteCategories(memId)
        }
    }

    //ดึงข้อมูลหมวดหมู่โปรด
    private fun fetchFavoriteCategories(memId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getFavoriteCategoryByMemId(memId).enqueue(object : Callback<List<Favorite_CategoryData>> {
            override fun onResponse(call: Call<List<Favorite_CategoryData>>, response: Response<List<Favorite_CategoryData>>) {
                if (response.isSuccessful) {
                    val favorites = response.body()
                    //เช็คอีกรอบ ว่าควรเข้าหน้าเลือกหรือแสดงข้อมูล
                    if (favorites.isNullOrEmpty()) {
                        showEmptyState()
                    } else {
                        existingCategories.clear()
                        existingCategories.addAll(favorites.map { it.catId })
                        if (isEditing) {
                            // ถ้ากำลังแก้ไขหมวดหมู่ ให้แสดงหมวดหมู่เพื่อเลือก
                            showCategorySelection()
                        } else {
                            // ถ้าไม่ได้แก้ไข เข้าไปหน้าหมวดหมู่โปรด
                            navigateToMyFavoriteCategoryNewsActivity()
                        }
                    }
                } else {
                    //ถ้ายังไม่มีข้อมูล เข้าหน้าแจ้งเตือน
                    showEmptyState()
                }
            }

            override fun onFailure(call: Call<List<Favorite_CategoryData>>, t: Throwable) {
                Toast.makeText(this@SelectFavoriteActivity, "เกิดข้อผิดพลาดในการดึงข้อมูลหมวดหมู่โปรด", Toast.LENGTH_SHORT).show()
                showEmptyState()
            }
        })
    }

    //ไปหน้าแสดงข้อมูล
    private fun navigateToMyFavoriteCategoryNewsActivity() {
        val intent = Intent(this, MyFavoriteCategoryNewsActivity::class.java)
        startActivity(intent)
        finish()
    }

    //ไปหน้าแจ้งเตือนให้เลือก
    private fun showEmptyState() {
        emptyStateContainer.visibility = View.VISIBLE
        categoryScrollView.visibility = View.GONE
        saveButton.visibility = View.GONE
        textSelectFavorite.visibility = View.GONE
        existingCategories.clear()
        selectedCategories.clear()
    }

    //ไปหน้าเลือกหมวดหมู่
    private fun showCategorySelection() {
        emptyStateContainer.visibility = View.GONE
        categoryScrollView.visibility = View.VISIBLE
        saveButton.visibility = View.VISIBLE
        textSelectFavorite.visibility = View.VISIBLE
        loadAllCategories()
    }

    //โหลดหมวดหมู่ทั้งหมด
    private fun loadAllCategories() {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        categoriesContainer.removeAllViews()
        apiService.getCategory().enqueue(object : Callback<List<CategoryData>> {
            override fun onResponse(call: Call<List<CategoryData>>, response: Response<List<CategoryData>>) {
                if (response.isSuccessful) {
                    response.body()?.forEach { category ->
                        //สร้าง checkbox ให้แต่ละหมวดหมู่
                        val checkBox = CheckBox(this@SelectFavoriteActivity).apply {
                            text = category.catName
                            isChecked = existingCategories.contains(category.catId)
                            layoutParams = GridLayout.LayoutParams().apply {
                                width = 0
                                height = GridLayout.LayoutParams.WRAP_CONTENT
                                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                                setMargins(16, 16, 16, 16)
                            }
                            //ตรวจสอบว่าถูกเลือกอยู่แล้วหรือเปล่า
                            if (isChecked) {
                                selectedCategories.add(category.catId)
                            }
                            //เพิ่มหรือลบออกจาก list ตามการ check
                            setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked) {
                                    selectedCategories.add(category.catId)
                                } else {
                                    selectedCategories.remove(category.catId)
                                }
                            }
                        }
                        categoriesContainer.addView(checkBox)
                    }
                } else {
                    Toast.makeText(this@SelectFavoriteActivity, "ไม่สามารถดึงข้อมูลหมวดหมู่ได้", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<CategoryData>>, t: Throwable) {
                Toast.makeText(this@SelectFavoriteActivity, "เกิดข้อผิดพลาดในการโหลดหมวดหมู่", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //กดปุ่มบันทึก
    private fun saveFavoriteCategories(memId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val categoriesToUpdate = UpdateFavoriteCategoriesRequest(memId, selectedCategories)

        if (selectedCategories.isEmpty()) {
            // ถ้าไม่มีหมวดหมู่ที่ถูกเลือก ให้ลบหมวดหมู่ทั้งหมด
            apiService.updateFavoriteCategories(categoriesToUpdate).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SelectFavoriteActivity, "ลบหมวดหมู่โปรดเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                        showEmptyState() // ไปหน้าแจ้งเตือนให้เลือกหมวดหมู่
                    } else {
                        Toast.makeText(this@SelectFavoriteActivity, "การบันทึกหมวดหมู่ล้มเหลว", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@SelectFavoriteActivity, "การบันทึกหมวดหมู่ล้มเหลว: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // แก้ไข/บักทึกหมวดหมู่ที่ถูกเลือกตามปกติ
            apiService.updateFavoriteCategories(categoriesToUpdate).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SelectFavoriteActivity, "บันทึกหมวดหมู่เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                        navigateToMyFavoriteCategoryNewsActivity() //ไปหน้าแสดงข่าวหมวดหมู่โปรด
                    } else {
                        Toast.makeText(this@SelectFavoriteActivity, "การบันทึกหมวดหมู่ล้มเหลว", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@SelectFavoriteActivity, "การบันทึกหมวดหมู่ล้มเหลว: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

}




