package com.example.project_news_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubCategoryAdapter(private val subCategoryList: List<String>) : RecyclerView.Adapter<SubCategoryAdapter.SubCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return SubCategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        val currentItem = subCategoryList[position]
        holder.subCategoryName.text = currentItem
    }

    override fun getItemCount() = subCategoryList.size

    class SubCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subCategoryName: TextView = itemView.findViewById(R.id.category_name)
    }
}
