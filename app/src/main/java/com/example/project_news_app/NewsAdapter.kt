package com.example.project_news_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_news_app.NewsData
import com.example.project_news_app.R

class NewsAdapter(private var newsList: List<NewsData>) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    fun setNews(newList: List<NewsData>) {
        newsList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = newsList[position]
        holder.newsName.text = news.newsName
        holder.newsDate.text = news.dateAdded.toString()
        holder.newsReadCount.text = "อ่าน ${news.readCount} ครั้ง"
        holder.newsRating.text = "★ ${news.ratingScore}"
    }

    override fun getItemCount() = newsList.size

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsPicture: ImageView = itemView.findViewById(R.id.news_picture)
        val newsName: TextView = itemView.findViewById(R.id.news_name)
        val newsDate: TextView = itemView.findViewById(R.id.news_date)
        val newsReadCount: TextView = itemView.findViewById(R.id.news_read_count)
        val newsRating: TextView = itemView.findViewById(R.id.news_rating)
    }
}
