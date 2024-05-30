package com.example.project_news_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NewsAdapter(private val newsList: List<News>) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = newsList[position]
        holder.newsTitle.text = currentItem.title
        holder.newsAdmin.text = currentItem.admin
        holder.newsDate.text = currentItem.date
        holder.newsReadCount.text = currentItem.readCount
        holder.newsRating.text = currentItem.rating
    }

    override fun getItemCount() = newsList.size

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsImage: ImageView = itemView.findViewById(R.id.news_image)
        val newsTitle: TextView = itemView.findViewById(R.id.news_title)
        val newsAdmin: TextView = itemView.findViewById(R.id.news_admin)
        val newsDate: TextView = itemView.findViewById(R.id.news_date)
        val newsReadCount: TextView = itemView.findViewById(R.id.news_read_count)
        val newsRating: TextView = itemView.findViewById(R.id.news_rating)
    }
}

data class News(
    val title: String,
    val admin: String,
    val date: String,
    val readCount: String,
    val rating: String
)
