package com.example.project_news_app

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class NewsDetailsActivity : AppCompatActivity() {

    private lateinit var newsTitle: TextView
    private lateinit var newsCategory: TextView
    private lateinit var newsMajorLevel: TextView
    private lateinit var newsDate: TextView
    private lateinit var newsReadCount: TextView
    private lateinit var newsRating: TextView
    private lateinit var newsDetails: TextView
    private lateinit var newsSubCategory: TextView
    private lateinit var newsImagesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        // Set up toolbar with back button
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, android.R.color.black))
        toolbar.setNavigationOnClickListener { onBackPressed() }

        newsTitle = findViewById(R.id.news_name)
        newsCategory = findViewById(R.id.cat_name)
        newsMajorLevel = findViewById(R.id.major_level)
        newsDate = findViewById(R.id.date_added)
        newsReadCount = findViewById(R.id.news_read_count)
        newsRating = findViewById(R.id.rating_score)
        newsDetails = findViewById(R.id.news_details)
        newsSubCategory = findViewById(R.id.news_sub_category)
        newsImagesContainer = findViewById(R.id.news_images_container)

        val newsId = intent.getIntExtra("news_id", -1)
        if (newsId != -1) {
            fetchNewsDetails(newsId)
        }
    }

    // Fetch news details from API
    private fun fetchNewsDetails(newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getNewsById(newsId).enqueue(object : Callback<NewsData> {
            override fun onResponse(call: Call<NewsData>, response: Response<NewsData>) {
                if (response.isSuccessful) {
                    response.body()?.let { news ->
                        displayNewsDetails(news)
                    }
                }
            }

            override fun onFailure(call: Call<NewsData>, t: Throwable) {
                // Handle failure
            }
        })
    }

    // Fetch read count from API
    private fun fetchReadCount(news: NewsData) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)

        apiService.getTotalRead().enqueue(object : Callback<List<Total_ReadData>> {
            override fun onResponse(
                call: Call<List<Total_ReadData>>,
                response: Response<List<Total_ReadData>>
            ) {
                if (response.isSuccessful) {
                    val readCounts = response.body() ?: listOf()
                    news.readCount = readCounts.count { it.newsId == news.newsId }
                    newsReadCount.text = "อ่าน ${news.readCount} ครั้ง"
                }
            }

            override fun onFailure(call: Call<List<Total_ReadData>>, t: Throwable) {
                // Handle failure
            }
        })
    }

    // Fetch subcategories from API
    private fun fetchSubCategoryNames(subCatIds: List<Int>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val subCategoryNames = mutableListOf<String>()

        apiService.getSubcategoriesByIds(subCatIds)
            .enqueue(object : Callback<List<Sub_CategoryData>> {
                override fun onResponse(
                    call: Call<List<Sub_CategoryData>>,
                    response: Response<List<Sub_CategoryData>>
                ) {
                    if (response.isSuccessful) {
                        val subCategories = response.body() ?: listOf()
                        subCategoryNames.addAll(subCategories.map { it.subCatName })
                        newsSubCategory.text = "แท็กข่าว: ${subCategoryNames.joinToString(", ")}"
                    } else {
                        Log.e(
                            "NewsDetailsActivity",
                            "Failed to fetch subcategories: ${response.errorBody()?.string()}"
                        )
                    }
                }

                override fun onFailure(call: Call<List<Sub_CategoryData>>, t: Throwable) {
                    Log.e("NewsDetailsActivity", "Error fetching subcategories: ${t.message}")
                }
            })
    }

    // Fetch news rating from API
    private fun fetchRating(news: NewsData) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)

        apiService.getNewsRating().enqueue(object : Callback<List<News_RatingData>> {
            override fun onResponse(
                call: Call<List<News_RatingData>>,
                response: Response<List<News_RatingData>>
            ) {
                if (response.isSuccessful) {
                    val ratings = response.body() ?: listOf()
                    val newsRatings = ratings.filter { it.newsId == news.newsId }
                    news.ratingScore = if (newsRatings.isNotEmpty()) {
                        newsRatings.sumByDouble { it.ratingScore.toDouble() }
                            .toFloat() / newsRatings.size
                    } else {
                        0f
                    }
                    newsRating.text = "★ %.2f".format(news.ratingScore)
                }
            }

            override fun onFailure(call: Call<List<News_RatingData>>, t: Throwable) {
                // Handle failure
            }
        })
    }

    // Fetch category name from API
    private fun fetchCategoryName(catId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getCategoryById(catId).enqueue(object : Callback<CategoryData> {
            override fun onResponse(call: Call<CategoryData>, response: Response<CategoryData>) {
                if (response.isSuccessful) {
                    newsCategory.text = "หมวดหมู่: ${response.body()?.catName}"
                }
            }

            override fun onFailure(call: Call<CategoryData>, t: Throwable) {
                // Handle failure
            }
        })
    }

    // Fetch major level from API
    private fun fetchMajorLevel(majorId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getMajorById(majorId).enqueue(object : Callback<MajorData> {
            override fun onResponse(call: Call<MajorData>, response: Response<MajorData>) {
                if (response.isSuccessful) {
                    newsMajorLevel.text = "ระดับความสำคัญ: ${response.body()?.majorLevel}"
                }
            }

            override fun onFailure(call: Call<MajorData>, t: Throwable) {
                // Handle failure
            }
        })
    }

    // Fetch and format date
    private fun fetchFormattedDate(dateString: String) {
        val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val targetFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = originalFormat.parse(dateString)
        val formattedDate = targetFormat.format(date)
        newsDate.text = formattedDate
    }

    // Fetch and display images excluding cover image
    private fun fetchImages(newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getCoverImage(newsId).enqueue(object : Callback<List<PictureData>> {
            override fun onResponse(
                call: Call<List<PictureData>>,
                response: Response<List<PictureData>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.filter { !it.pictureName.startsWith("cover_") }
                        ?.forEach { picture ->
                            val imageView = ImageView(this@NewsDetailsActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    800
                                ).apply {
                                    setMargins(0, 8, 0, 8)
                                }
                            }
                            val baseUrl =
                                RetrofitClient.getClient(this@NewsDetailsActivity).baseUrl()
                                    .toString()
                            Glide.with(this@NewsDetailsActivity)
                                .load("${baseUrl}uploads/${picture.pictureName}")
                                .into(imageView)
                            newsImagesContainer.addView(imageView)
                        }
                }
            }

            override fun onFailure(call: Call<List<PictureData>>, t: Throwable) {
                // Handle failure
            }
        })
    }

    // Display news details
    private fun displayNewsDetails(news: NewsData) {
        fetchCategoryName(news.catId)
        fetchMajorLevel(news.majorId)
        fetchReadCount(news)
        fetchRating(news)
        fetchFormattedDate(news.dateAdded.toString())
        newsTitle.text = news.newsName
        newsDetails.text = news.newsDetails

        // Fetch and display news subcategories
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getNewsSubCateByNewsId(news.newsId)
            .enqueue(object : Callback<List<News_Sub_CateData>> {
                override fun onResponse(
                    call: Call<List<News_Sub_CateData>>,
                    response: Response<List<News_Sub_CateData>>
                ) {
                    if (response.isSuccessful) {
                        val subCategoryIds = response.body()?.map { it.subCatId } ?: listOf()
                        Log.d("NewsDetailsActivity", "Subcategory IDs: $subCategoryIds")
                        fetchSubCategoryNames(subCategoryIds)
                    } else {
                        Log.e(
                            "NewsDetailsActivity",
                            "Failed to fetch news subcategories: ${response.errorBody()?.string()}"
                        )
                    }
                }

                override fun onFailure(call: Call<List<News_Sub_CateData>>, t: Throwable) {
                    Log.e("NewsDetailsActivity", "Error fetching news subcategories: ${t.message}")
                }
            })

        // Fetch and display images excluding cover image
        fetchImages(news.newsId)
    }
}

