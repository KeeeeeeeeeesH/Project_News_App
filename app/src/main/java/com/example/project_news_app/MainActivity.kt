package com.example.project_news_app

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.HorizontalScrollView
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private var isCategoriesExpanded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toggleButton: ImageButton = findViewById(R.id.toggle_categories)
        val subCategoriesScrollView: HorizontalScrollView = findViewById(R.id.subcategories_scroll_view)
        val subCategoriesRecyclerView: RecyclerView = findViewById(R.id.subcategories_recycler_view)

        toggleButton.setOnClickListener {
            isCategoriesExpanded = !isCategoriesExpanded
            updateToggleButton(subCategoriesScrollView)
        }

        // Categories RecyclerView
        val categoriesRecyclerView: RecyclerView = findViewById(R.id.categories_recycler_view)
        categoriesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoriesRecyclerView.adapter = CategoryAdapter(getCategoryList())

        // Subcategories RecyclerView
        subCategoriesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        subCategoriesRecyclerView.adapter = SubCategoryAdapter(getSubCategoryList())

        // News RecyclerView
        val newsRecyclerView: RecyclerView = findViewById(R.id.news_recycler_view)
        newsRecyclerView.layoutManager = LinearLayoutManager(this)
        newsRecyclerView.adapter = NewsAdapter(getNewsList())

        // Bottom Navigation
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_favorites -> {
                    // Handle Favorites action
                    true
                }
                R.id.navigation_home -> {
                    // Handle Home action
                    true
                }
                R.id.navigation_profile -> {
                    // Handle Profile action
                    true
                }
                else -> false
            }
        }
    }

    private fun updateToggleButton(subCategoriesScrollView: HorizontalScrollView) {
        val toggleButton: ImageButton = findViewById(R.id.toggle_categories)
        if (isCategoriesExpanded) {
            toggleButton.setImageResource(R.drawable.ic_arrow_up)
            subCategoriesScrollView.visibility = View.VISIBLE
        } else {
            toggleButton.setImageResource(R.drawable.ic_arrow_down)
            subCategoriesScrollView.visibility = View.GONE
        }
    }

    private fun getCategoryList(): List<String> {
        return listOf("แนะนำ", "เทคโนโลยี", "เกม", "สุขภาพ", "ท่องเที่ยว")
    }

    private fun getSubCategoryList(): List<String> {
        return listOf("Sub1", "Sub2", "Sub3", "Sub4", "Sub5")
    }

    private fun getNewsList(): List<News> {
        return listOf(
            News("ชื่อข่าว 1", "admin", "วันที่ วง/ดด/ปปปป", "อ่าน 4,567 ครั้ง", "★ 4.55"),
            News("ชื่อข่าว 2", "admin", "วันที่ วง/ดด/ปปปป", "อ่าน 3,456 ครั้ง", "★ 4.50"),
            News("ชื่อข่าว 3", "admin", "วันที่ วง/ดด/ปปปป", "อ่าน 2,345 ครั้ง", "★ 4.45"),
            News("ชื่อข่าว 4", "admin", "วันที่ วง/ดด/ปปปป", "อ่าน 1,234 ครั้ง", "★ 4.40"),
            News("ชื่อข่าว 5", "admin", "วันที่ วง/ดด/ปปปป", "อ่าน 567 ครั้ง", "★ 4.35")
        )
    }
}