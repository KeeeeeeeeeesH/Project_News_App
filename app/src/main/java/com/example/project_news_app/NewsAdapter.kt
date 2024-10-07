package com.example.project_news_app.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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
import com.example.project_news_app.ReadHistoryWithNewsData
import com.example.project_news_app.VisitHistoryActivity
import java.text.SimpleDateFormat
import java.util.Locale

class NewsAdapter(
    private var newsList: List<Any>,
    private val newsType: NewsType
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    enum class NewsType {
        FAVORITE, READ_LATER, VISIT_HISTORY, GENERAL
    }

    fun setNews(news: List<Any>) {
        this.newsList = news
        notifyDataSetChanged()
    }

    fun addNews(news: List<Any>) {
        val currentSize = newsList.size
        newsList = newsList + news
        notifyItemRangeInserted(currentSize, news.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val item = newsList[position]

        when (newsType) {
            NewsType.VISIT_HISTORY -> {
                if (item is ReadHistoryWithNewsData) {
                    holder.newsName.text = item.newsName
                    holder.itemView.findViewById<TextView>(R.id.admin_label).visibility = View.GONE
                    holder.itemView.findViewById<TextView>(R.id.read_date_label).visibility = View.VISIBLE

                    // Format date
                    val targetFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val formattedDate = targetFormat.format(item.readDate)

                    holder.newsDate.text = formattedDate
                    holder.newsReadCount.text = "อ่าน ${item.readCount} ครั้ง"  // เปลี่ยนเป็นของสมาชิก
                    holder.newsRating.text = "คะแนนที่ให้ ★ %.2f".format(item.ratingScore)  // เปลี่ยนเป็นคะแนนของสมาชิก

                    // Load cover image if available
                    if (!item.coverImage.isNullOrEmpty()) {
                        Glide.with(holder.itemView.context)
                            .load(item.coverImage)
                            .into(holder.newsPicture)
                    } else {
                        holder.newsPicture.setImageResource(com.google.android.material.R.drawable.navigation_empty_icon)
                    }

                    // Set click listener to open NewsDetailsActivity
                    holder.itemView.setOnClickListener {
                        val intent = Intent(holder.itemView.context, NewsDetailsActivity::class.java)
                        intent.putExtra("news_id", item.newsId)
                        holder.itemView.context.startActivity(intent)
                    }

                    // Set long click listener to delete read history
                    holder.itemView.setOnLongClickListener {
                        AlertDialog.Builder(holder.itemView.context)
                            .setTitle("ลบประวัติการอ่าน")
                            .setMessage("คุณต้องการลบประวัติการอ่านข่าวนี้หรือไม่?")
                            .setPositiveButton("ใช่") { dialog, _ ->
                                val sharedPreferences = holder.itemView.context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                val memId = sharedPreferences.getInt("memId", -1)
                                if (memId != -1 && holder.itemView.context is VisitHistoryActivity) {
                                    (holder.itemView.context as VisitHistoryActivity).deleteReadHistory(memId, item.newsId)
                                }
                                dialog.dismiss()
                            }
                            .setNegativeButton("ไม่") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                        true
                    }
                }
            }
            NewsType.FAVORITE, NewsType.GENERAL, NewsType.READ_LATER -> {
                if (item is NewsData) {
                    // ระบบข่าวอื่นๆ
                    holder.newsName.text = item.newsName

                    // Format date
                    val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
                    val targetFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val date = originalFormat.parse(item.dateAdded.toString())
                    val formattedDate = targetFormat.format(date)

                    holder.newsDate.text = formattedDate
                    holder.newsReadCount.text = "อ่าน ${item.readCount} ครั้ง"
                    holder.newsRating.text = "คะแนน ★ %.2f".format(item.ratingScore)

                    // Load cover image if available
                    if (!item.coverImageUrl.isNullOrEmpty()) {
                        Glide.with(holder.itemView.context)
                            .load(item.coverImageUrl)
                            .into(holder.newsPicture)
                    } else {
                        holder.newsPicture.setImageResource(com.google.android.material.R.drawable.navigation_empty_icon)
                    }

                    // Set click listener to open NewsDetailsActivity
                    holder.itemView.setOnClickListener {
                        val intent = Intent(holder.itemView.context, NewsDetailsActivity::class.java)
                        intent.putExtra("news_id", item.newsId)
                        holder.itemView.context.startActivity(intent)
                    }
                }
            }
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

