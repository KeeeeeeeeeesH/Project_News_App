package com.example.project_news_app.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_news_app.CategoryData
import com.example.project_news_app.R

class CategoryAdapter(
    private val onCategoryClick: (CategoryData) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var categories = listOf<CategoryData>()
    private var selectedCategoryId: Int = 0

    //รับ data list ที่จะอัพเดทและแสดง
    fun setCategories(newCategories: List<CategoryData>) {
        categories = newCategories
        notifyDataSetChanged() //refresh รายการ
    }

    //set ให้เป็น id ของหมวดหมู่ที่เลือก
    fun setSelectedCategory(catId: Int) {
        selectedCategoryId = catId
        notifyDataSetChanged()
    }

    //สร้าง ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(itemView)
    }

    //ตั้งค่าข้อมูลและการแสดงผล
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryName.text = category.catName
        holder.categoryName.setTextColor(
            if (category.catId == selectedCategoryId) Color.RED else Color.BLACK
        )
        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }
    }

    override fun getItemCount() = categories.size //คืนค่าจำนวนหมวดหมู่ให้ Recycler

    //เก็บ textview แสดงชื่อ
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.category_name)
    }
}
