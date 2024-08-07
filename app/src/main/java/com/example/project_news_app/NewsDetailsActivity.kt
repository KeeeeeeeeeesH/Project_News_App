package com.example.project_news_app

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Timestamp
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
    private lateinit var ratingSpinner: Spinner
    private lateinit var submitRatingButton: Button
    private lateinit var saveForLaterButton: ImageView
    private var isSavedForLater: Boolean = false
    private var memId: Int = -1
    private var newsId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        // Set up toolbar with back button
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, android.R.color.black))
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Initialize UI elements
        newsTitle = findViewById(R.id.news_name)
        newsCategory = findViewById(R.id.cat_name)
        newsMajorLevel = findViewById(R.id.major_level)
        newsDate = findViewById(R.id.date_added)
        newsReadCount = findViewById(R.id.news_read_count)
        newsRating = findViewById(R.id.rating_score)
        newsDetails = findViewById(R.id.news_details)
        newsSubCategory = findViewById(R.id.news_sub_category)
        newsImagesContainer = findViewById(R.id.news_images_container)
        ratingSpinner = findViewById(R.id.rating_spinner)
        submitRatingButton = findViewById(R.id.submit_rating_button)
        saveForLaterButton = findViewById(R.id.save_for_later_image)

        val ratingOptions = listOf(0f, 0.5f, 1f, 1.5f, 2f, 2.5f, 3f, 3.5f, 4f, 4.5f, 5f)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ratingOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ratingSpinner.adapter = adapter

        newsId = intent.getIntExtra("news_id", -1)
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        memId = sharedPreferences.getInt("memId", -1)

        if (newsId != -1) {
            fetchNewsDetails(newsId)
            increaseReadCount(newsId) // เพิ่มการอ่านเมื่อเปิดหน้า
            addReadHistory(newsId) // เพิ่มประวัติการอ่านเมื่อเปิดหน้า
            checkIfSavedForLater(memId, newsId) // ตรวจสอบสถานะการบันทึกข่าวอ่านภายหลัง
        }

        submitRatingButton.setOnClickListener {
            if (newsId != -1) {
                submitRating(newsId)
            }
        }

        saveForLaterButton.setOnClickListener {
            saveOrRemoveNewsForLater(memId, newsId)
        }
    }

    private fun submitRating(newsId: Int) {
        val selectedRating = ratingSpinner.selectedItem.toString().toFloat()

        // ดึง member ID จาก SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val memId = sharedPreferences.getInt("memId", -1)

        if (memId == -1) {
            Toast.makeText(this, "ไม่พบ Member ID", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val newsRating = News_RatingData(memId, newsId, selectedRating)
        apiService.putNewsRatingByMemId(memId, newsId, newsRating)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@NewsDetailsActivity, "ให้คะแนนสำเร็จ", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMessage = response.errorBody()?.string()
                        Toast.makeText(this@NewsDetailsActivity, "ไม่สามารถให้คะแนนได้: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@NewsDetailsActivity, "เกิดข้อผิดพลาดในการให้คะแนน: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
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

    private fun increaseReadCount(newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val totalRead = Total_ReadData(0, newsId) // Count_Id เป็น 0 เพราะจะให้ฐานข้อมูลเพิ่มค่าอัตโนมัติ

        apiService.postTotalRead(totalRead).enqueue(object : Callback<Total_ReadData> {
            override fun onResponse(call: Call<Total_ReadData>, response: Response<Total_ReadData>) {
                if (response.isSuccessful) {
                    Log.d("NewsDetailsActivity", "Read count increased successfully")
                    fetchReadCount(newsId) // อัปเดตจำนวนการอ่านใน UI
                } else {
                    Log.e("NewsDetailsActivity", "Failed to increase read count")
                }
            }

            override fun onFailure(call: Call<Total_ReadData>, t: Throwable) {
                Log.e("NewsDetailsActivity", "Error: ${t.message}")
            }
        })
    }

    // Fetch read count from API
    private fun fetchReadCount(newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)

        apiService.getTotalRead().enqueue(object : Callback<List<Total_ReadData>> {
            override fun onResponse(
                call: Call<List<Total_ReadData>>,
                response: Response<List<Total_ReadData>>
            ) {
                if (response.isSuccessful) {
                    val readCounts = response.body() ?: listOf()
                    val readCount = readCounts.count { it.newsId == newsId }
                    newsReadCount.text = "อ่าน $readCount ครั้ง"
                }
            }

            override fun onFailure(call: Call<List<Total_ReadData>>, t: Throwable) {
                // Handle failure
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
                    val majorLevel = response.body()?.majorLevel
                    val majorLevelText = when (majorLevel) {
                        0 -> "ปกติ"
                        1 -> "สูง"
                        else -> "ไม่มี"
                    }
                    newsMajorLevel.text = "ระดับความสำคัญ: $majorLevelText"
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

    private fun fetchNewsSubCategories(newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)

        // Fetch subcategory IDs from News_Sub_Cate table
        apiService.getNewsSubCateByNewsId(newsId).enqueue(object : Callback<List<News_Sub_CateData>> {
            override fun onResponse(call: Call<List<News_Sub_CateData>>, response: Response<List<News_Sub_CateData>>) {
                if (response.isSuccessful) {
                    val subCategoryIds = response.body()?.map { it.subCatId } ?: listOf()
                    if (subCategoryIds.isEmpty()) {
                        newsSubCategory.text = "แท็กข่าว: ไม่มีหมวดหมู่รอง"
                    } else {
                        // Fetch subcategory names from Sub_Category table using the fetched subcategory IDs
                        fetchSubCategoryNames(subCategoryIds)
                    }
                } else {
                    Log.e("NewsDetailsActivity", "Failed to fetch news subcategories: ${response.errorBody()?.string()}")
                    newsSubCategory.text = "แท็กข่าว: ไม่สามารถดึงหมวดหมู่รองได้"
                }
            }

            override fun onFailure(call: Call<List<News_Sub_CateData>>, t: Throwable) {
                Log.e("NewsDetailsActivity", "Error fetching news subcategories: ${t.message}")
                newsSubCategory.text = "แท็กข่าว: เกิดข้อผิดพลาดในการดึงหมวดหมู่รอง"
            }
        })
    }

    private fun fetchSubCategoryNames(subCatIds: List<Int>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)

        apiService.getSubcategoriesByIds(subCatIds).enqueue(object : Callback<List<Sub_CategoryData>> {
            override fun onResponse(call: Call<List<Sub_CategoryData>>, response: Response<List<Sub_CategoryData>>) {
                if (response.isSuccessful) {
                    val subCategories = response.body() ?: listOf()
                    if (subCategories.isEmpty()) {
                        newsSubCategory.text = "แท็กข่าว: ไม่มี"
                    } else {
                        val subCategoryNames = subCategories.map { it.subCatName }
                        newsSubCategory.text = "แท็กข่าว: ${subCategoryNames.joinToString(", ")}"
                    }
                } else {
                    Log.e("NewsDetailsActivity", "Failed to fetch subcategories: ${response.errorBody()?.string()}")
                    newsSubCategory.text = "แท็กข่าว: ไม่สามารถดึงแท็กข่าวได้"
                }
            }

            override fun onFailure(call: Call<List<Sub_CategoryData>>, t: Throwable) {
                Log.e("NewsDetailsActivity", "Error fetching subcategories: ${t.message}")
                newsSubCategory.text = "แท็กข่าว: เกิดข้อผิดพลาดในการดึงแท็กข่าว"
            }
        })
    }

    private fun displayNewsDetails(news: NewsData) {
        fetchCategoryName(news.catId)
        fetchMajorLevel(news.majorId)
        fetchReadCount(news.newsId)
        fetchRating(news)
        fetchFormattedDate(news.dateAdded.toString())
        newsTitle.text = news.newsName
        newsDetails.text = news.newsDetails

        // Fetch and display news subcategories
        fetchNewsSubCategories(news.newsId)

        // Fetch and display images excluding cover image
        fetchImages(news.newsId)

        // Save the news to visit history
        addReadHistory(news.newsId)
    }

    private fun addReadHistory(newsId: Int) {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val memId = sharedPreferences.getInt("memId", -1)

        if (memId != -1) {
            val readDate = Timestamp(System.currentTimeMillis())
            val readHistory = Read_HistoryData(memId, newsId, readDate)
            val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
            apiService.addReadHistory(readHistory).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@NewsDetailsActivity, "Failed to add read history", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@NewsDetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Member ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkIfSavedForLater(memId: Int, newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getReadLaterByMemId(memId).enqueue(object : Callback<List<Read_LaterData>> {
            override fun onResponse(call: Call<List<Read_LaterData>>, response: Response<List<Read_LaterData>>) {
                if (response.isSuccessful) {
                    val readLaterList = response.body() ?: emptyList()
                    isSavedForLater = readLaterList.any { it.newsId == newsId }
                    updateSaveForLaterButton()
                } else {
                    Toast.makeText(this@NewsDetailsActivity, "ไม่สามารถตรวจสอบสถานะข่าวอ่านภายหลังได้", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Read_LaterData>>, t: Throwable) {
                Toast.makeText(this@NewsDetailsActivity, "เกิดข้อผิดพลาดในการตรวจสอบสถานะข่าวอ่านภายหลัง: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveOrRemoveNewsForLater(memId: Int, newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val readLaterData = Read_LaterData(memId, newsId)

        apiService.postReadLater(readLaterData).enqueue(object : Callback<Read_LaterData> {
            override fun onResponse(call: Call<Read_LaterData>, response: Response<Read_LaterData>) {
                if (response.isSuccessful || response.code() == 201) {
                    isSavedForLater = !isSavedForLater
                    updateSaveForLaterButton()
                    Toast.makeText(this@NewsDetailsActivity, if (isSavedForLater) "เพิ่มข่าวอ่านภายหลังสำเร็จ" else "ลบข่าวอ่านภายหลังสำเร็จ", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMessage = response.errorBody()?.string()
                    Toast.makeText(this@NewsDetailsActivity, "ไม่สามารถปรับสถานะข่าวอ่านภายหลังได้: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Read_LaterData>, t: Throwable) {
                Toast.makeText(this@NewsDetailsActivity, "เกิดข้อผิดพลาดในการปรับสถานะข่าวอ่านภายหลัง: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateSaveForLaterButton() {
        if (isSavedForLater) {
            saveForLaterButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star))
        } else {
            saveForLaterButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_clock))
        }
    }
}
