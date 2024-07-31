package com.example.project_news_app.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project_news_app.NewsData
import com.example.project_news_app.NewsDetailsActivity
import com.example.project_news_app.R
import java.text.SimpleDateFormat
import java.util.*

class NewsAdapter(private var newsList: List<NewsData>) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    fun setNews(news: List<NewsData>) {
        this.newsList = news
        Log.d("NewsAdapter", "Set news list size: ${news.size}")
        news.forEach { newsData ->
            Log.d("NewsAdapter", "Set News: ${newsData.newsName}")
        }
        notifyDataSetChanged()
    }

    fun addNews(news: List<NewsData>) {
        val currentSize = newsList.size
        newsList = newsList + news
        Log.d("NewsAdapter", "Added news. New list size: ${newsList.size}")
        news.forEach { newsData ->
            Log.d("NewsAdapter", "Added News: ${newsData.newsName}")
        }
        notifyItemRangeInserted(currentSize, news.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = newsList[position]
        holder.newsName.text = news.newsName

        // Format date
        val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val targetFormat = SimpleDateFormat("dd/M/yyyy HH:mm", Locale.getDefault())
        val date = originalFormat.parse(news.dateAdded.toString())
        val formattedDate = targetFormat.format(date)

        holder.newsDate.text = formattedDate
        holder.newsReadCount.text = "อ่าน ${news.readCount} ครั้ง"
        holder.newsRating.text = "★ %.2f".format(news.ratingScore)

        // Load cover image if available
        if (!news.coverImageUrl.isNullOrEmpty()) {
            Log.d("NewsAdapter", "Loading image from URL: ${news.coverImageUrl}") // Debug log
            Glide.with(holder.itemView.context)
                .load(news.coverImageUrl)
                .into(holder.newsPicture)
        } else {
            holder.newsPicture.setImageResource(com.google.android.material.R.drawable.navigation_empty_icon) // รูป placeholder_image ต้องอยู่ใน drawable
        }

        // Set click listener to open NewsDetailsActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, NewsDetailsActivity::class.java)
            intent.putExtra("news_id", news.newsId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = newsList.size

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsPicture: ImageView = itemView.findViewById(R.id.news_picture)
        val newsName: TextView = itemView.findViewById(R.id.news_name)
        val newsDate: TextView = itemView.findViewById(R.id.date_added)
        val newsReadCount: TextView = itemView.findViewById(R.id.news_read_count)
        val newsRating: TextView = itemView.findViewById(R.id.rating_score)
    }
}

