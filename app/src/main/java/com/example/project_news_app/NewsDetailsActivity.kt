package com.example.project_news_app

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
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
    private lateinit var ratingBar: RatingBar
    private lateinit var submitRatingButton: Button
    private lateinit var saveForLaterButton: ImageView
    private var isSavedForLater: Boolean = false
    private var memId: Int = -1
    private var newsId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        // ตั้งค่า toolbar และปุ่มย้อนกลับ
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, android.R.color.black))
        toolbar.setNavigationOnClickListener { onBackPressed() }

        //Initialize UI
        newsTitle = findViewById(R.id.news_name)
        newsCategory = findViewById(R.id.cat_name)
        newsMajorLevel = findViewById(R.id.major_level)
        newsDate = findViewById(R.id.date_added)
        newsReadCount = findViewById(R.id.news_read_count)
        newsRating = findViewById(R.id.rating_score)
        newsDetails = findViewById(R.id.news_details)
        newsSubCategory = findViewById(R.id.news_sub_category)
        newsImagesContainer = findViewById(R.id.news_images_container)
        ratingBar = findViewById(R.id.rating_bar)
        submitRatingButton = findViewById(R.id.submit_rating_button)
        saveForLaterButton = findViewById(R.id.save_for_later_image)
        newsId = intent.getIntExtra("news_id", -1)

        //SharedPref
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        memId = sharedPreferences.getInt("memId", -1)

        //ตรวจสอบว่าเจอข่าวในระบบ
        if (newsId != -1) {
            fetchNewsDetails(newsId) //ดึงข่าว
            increaseReadCount(newsId) // เพิ่มการอ่านเมื่อเปิดหน้า
            addReadHistory(newsId) // เพิ่มประวัติการอ่านเมื่อเปิดหน้า
            checkIfSavedForLater(memId, newsId) // ตรวจสอบสถานะการบันทึกข่าวอ่านภายหลัง
        }

        //ให้คะแนน
        submitRatingButton.setOnClickListener {
            if (newsId != -1) {
                submitRating(newsId)
            }
        }

        //บันทึกอ่านภายหลัง
        saveForLaterButton.setOnClickListener {
            saveOrRemoveNewsForLater(memId, newsId)
        }
    }

   // ข้อมูลต่างๆที่ดึงมาใช้
    private fun displayNewsDetails(news: NewsData) {
        fetchCategoryName(news.catId)
        fetchMajorLevel(news.majorId)
        fetchFormattedDate(news.dateAdded.toString())
        fetchReadCount(news.newsId)
        fetchRating(news)
        fetchImages(news.newsId)
        newsTitle.text = news.newsName
        newsDetails.text = news.newsDetails
        fetchNewsSubCategories(news.newsId)
        addReadHistory(news.newsId)
    }

    //ดึงรายละเอียดข่าวทั้งหมด
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
                Toast.makeText(this@NewsDetailsActivity, "เกิดข้อผิดพลาดในการโหลดรายละเอียดข่าว: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //ดึงจำนวนการอ่าน
    private fun fetchReadCount(newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)

        apiService.getTotalRead().enqueue(object : Callback<List<Total_ReadData>> {
            override fun onResponse(call: Call<List<Total_ReadData>>, response: Response<List<Total_ReadData>>) {
                if (response.isSuccessful) {
                    val readCounts = response.body() ?: listOf()
                    val readCount = readCounts.count { it.newsId == newsId }
                    newsReadCount.text = "อ่าน $readCount ครั้ง"
                }
            }

            override fun onFailure(call: Call<List<Total_ReadData>>, t: Throwable) {
                Toast.makeText(this@NewsDetailsActivity, "เกิดข้อผิดพลาดในการโหลดคะแนนข่าว: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //ดึงคะแนน
    private fun fetchRating(news: NewsData) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)

        apiService.getNewsRating().enqueue(object : Callback<List<News_RatingData>> {
            override fun onResponse(call: Call<List<News_RatingData>>, response: Response<List<News_RatingData>>) {
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
                Toast.makeText(this@NewsDetailsActivity, "เกิดข้อผิดพลาดในการโหลดคะแนนข่าว: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //ดึงชื่อหมวดหมู่
    private fun fetchCategoryName(catId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getCategoryById(catId).enqueue(object : Callback<CategoryData> {
            override fun onResponse(call: Call<CategoryData>, response: Response<CategoryData>) {
                if (response.isSuccessful) {
                    newsCategory.text = "หมวดหมู่: ${response.body()?.catName}"
                }
            }

            override fun onFailure(call: Call<CategoryData>, t: Throwable) {
                Toast.makeText(this@NewsDetailsActivity, "เกิดข้อผิดพลาดในการโหลดชื่อหมวดหมู่ข่าว: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //ดึงระดับความสำคัญ
    private fun fetchMajorLevel(majorId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getMajorById(majorId).enqueue(object : Callback<MajorData> {
            override fun onResponse(call: Call<MajorData>, response: Response<MajorData>) {
                if (response.isSuccessful) {
                    val majorLevel = response.body()?.majorLevel
                    //เปลี่ยนการแสดงผล
                    val majorLevelText = when (majorLevel) {
                        0 -> "ปกติ"
                        1 -> "สูง"
                        else -> "ไม่มี"
                    }
                    newsMajorLevel.text = "ระดับความสำคัญ: $majorLevelText"
                }
            }

            override fun onFailure(call: Call<MajorData>, t: Throwable) {
                Toast.makeText(this@NewsDetailsActivity, "เกิดข้อผิดพลาดในการโหลดระดับความสำคัญ: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // format และ ดึงวันที่ลงข่าว
    private fun fetchFormattedDate(dateString: String) {
        val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val targetFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = originalFormat.parse(dateString)
        val formattedDate = targetFormat.format(date)
        newsDate.text = formattedDate
    }

    // ดึงข้อมูลรูปภาพข่าวทั้งหมด ยกเว้นรูปภาพหน้าปก
    private fun fetchImages(newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        //ดึงชื่อรูปจากฐานข้อมูลมาก่อน
        apiService.getCoverImage(newsId).enqueue(object : Callback<List<PictureData>> {
            override fun onResponse(call: Call<List<PictureData>>, response: Response<List<PictureData>>) {
                if (response.isSuccessful) {
                    response.body()?.filter { !it.pictureName.startsWith("cover_") } //กรองและไม่เอา
                        ?.forEach { picture ->
                            val imageView = ImageView(this@NewsDetailsActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    800
                                ).apply {
                                    setMargins(0, 8, 0, 8)
                                }
                            }
                            //ดึงรูปที่อยู่ใน server ผ่าน path api
                            val baseUrl = RetrofitClient.getClient(this@NewsDetailsActivity).baseUrl().toString()
                            Glide.with(this@NewsDetailsActivity)
                                .load("${baseUrl}uploads/${picture.pictureName}")
                                .into(imageView)
                            newsImagesContainer.addView(imageView)
                        }
                }
            }

            override fun onFailure(call: Call<List<PictureData>>, t: Throwable) {
                Toast.makeText(this@NewsDetailsActivity, "เกิดข้อผิดพลาดในการโหลดรูปภาพข่าว: ${t.message}", Toast.LENGTH_SHORT).show()

            }
        })
    }

    //ตรวจสอบและดึงข้อมูลในตารางแท็กข่าว
    private fun fetchNewsSubCategories(newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        //ถ้าเจอ news_id ที่ตรงกันอยู่ในตารางนี้ ดึงข้อมูลตารางแท็กข่าว
        apiService.getNewsSubCateByNewsId(newsId).enqueue(object : Callback<List<News_Sub_CateData>> {
            override fun onResponse(call: Call<List<News_Sub_CateData>>, response: Response<List<News_Sub_CateData>>) {
                if (response.isSuccessful) {
                    val subCategoryIds = response.body()?.map { it.subCatId } ?: listOf() //เอาเฉพาะ subCatId
                    if (subCategoryIds.isEmpty()) {
                        newsSubCategory.text = "แท็กข่าว: ไม่มีแท็กข่าว"
                    } else {
                        fetchSubCategoryNames(subCategoryIds) //เสร็จแล้วเอา id ไปหาชื่อต่อ
                    }
                } else {
                    Log.e("NewsDetailsActivity", "ดึงข้อมูลแท็กข่าวไม่สำเร็จ: ${response.errorBody()?.string()}")
                    newsSubCategory.text = "แท็กข่าว: ไม่สามารถดึงแท็กข่าวได้"
                }
            }

            override fun onFailure(call: Call<List<News_Sub_CateData>>, t: Throwable) {
                newsSubCategory.text = "แท็กข่าว: เกิดข้อผิดพลาดในการดึงข้อมูลแท็กข่าว"
                Toast.makeText(this@NewsDetailsActivity, "เกิดข้อผิดพลาดในการโหลดแท็กข่าว: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //ดึงชื่อหมวดหมู่รอง ถ้ามีแท็กข่าว
    private fun fetchSubCategoryNames(subCatIds: List<Int>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        //ดึงข้อมูลหมวดหมู่รอง
        apiService.getSubcategoriesByIds(subCatIds).enqueue(object : Callback<List<Sub_CategoryData>> {
            override fun onResponse(call: Call<List<Sub_CategoryData>>, response: Response<List<Sub_CategoryData>>) {
                if (response.isSuccessful) {
                    val subCategories = response.body() ?: listOf()
                    if (subCategories.isEmpty()) {
                        newsSubCategory.text = "แท็กข่าว: ไม่มีแท็กข่าว"
                    } else {
                        val subCategoryNames = subCategories.map { it.subCatName } //เอาเฉพาะ Name
                        newsSubCategory.text = "แท็กข่าว: ${subCategoryNames.joinToString(", ")}"
                    }
                } else {
                    Log.e("NewsDetailsActivity", "ดึงชื่อหมวดหมู่รองไม่สำเร็จ: ${response.errorBody()?.string()}")
                    newsSubCategory.text = "แท็กข่าว: ไม่สามารถดึงชื่อหมวดหมู่รองได้"
                }
            }

            override fun onFailure(call: Call<List<Sub_CategoryData>>, t: Throwable) {
                newsSubCategory.text = "แท็กข่าว: เกิดข้อผิดพลาดในการดึงชื่อหมวดหมู่รอง"
                Toast.makeText(this@NewsDetailsActivity, "เกิดข้อผิดพลาดในการโหลดชื่อหมวดหมู่รอง: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //ฟังก์ชันให้คะแนน
    private fun submitRating(newsId: Int) {
        val selectedRating = ratingBar.rating // รับค่าจาก rating bar

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val memId = sharedPreferences.getInt("memId", -1)
        if (memId == -1) {
            return
        }
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val newsRating = News_RatingData(memId, newsId, selectedRating) //ข้อมูลที่จะเก็บลง
        apiService.putNewsRatingByMemId(memId, newsId, newsRating).enqueue(object : Callback<ResponseBody> {
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

    //เพิ่มจำนวนการอ่าน
    private fun increaseReadCount(newsId: Int) {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val memId = sharedPreferences.getInt("memId", -1)

        if (memId != -1) {
            val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
            val totalRead = Total_ReadData(0, newsId, memId) //ข้อมูลที่จะเก็บลง
            apiService.postTotalRead(totalRead).enqueue(object : Callback<Total_ReadData> {
                override fun onResponse(call: Call<Total_ReadData>, response: Response<Total_ReadData>) {
                    if (response.isSuccessful) {
                        fetchReadCount(newsId) // อัปเดตจำนวนการอ่านใน UI ทันที
                    } else {
                        Log.e("NewsDetailsActivity", "เพิ่มจำนวนการอ่านข่าวไม่สำเร็จ")
                    }
                }

                override fun onFailure(call: Call<Total_ReadData>, t: Throwable) {
                    Log.e("NewsDetailsActivity", "เกิดข้อผิดพลาด: ${t.message}")
                }
            })
        } else {
            Log.e("NewsDetailsActivity", "ล้มเหลวในการดึงข้อมูลสมาชิก")
        }
    }

    //เพิ่มประวัติการอ่าน
    private fun addReadHistory(newsId: Int) {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val memId = sharedPreferences.getInt("memId", -1)

        if (memId != -1) {
            val readDate = Timestamp(System.currentTimeMillis())
            val readHistory = Read_HistoryData(memId, newsId, readDate) //ข้อมูลที่จะเก็บลง
            val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
            apiService.addReadHistory(readHistory).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@NewsDetailsActivity, "ไม่สามารถเพิ่มประวัติการอ่านได้", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@NewsDetailsActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "ไม่พบสมาชิกในระบบ", Toast.LENGTH_SHORT).show()
        }
    }

    //ตรวจสอบการบันทึกอ่านภายหลัง
    private fun checkIfSavedForLater(memId: Int, newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getReadLaterByMemId(memId).enqueue(object : Callback<List<Read_LaterData>> {
            override fun onResponse(call: Call<List<Read_LaterData>>, response: Response<List<Read_LaterData>>) {
                if (response.isSuccessful) {
                    val readLaterList = response.body() ?: emptyList() //ถ้าไม่เจอข้อมูล ให้สร้างลิสต์ว่างๆรอ
                    isSavedForLater = readLaterList.any { it.newsId == newsId } //ลูปหาข่าวที่ไอดีตรงกับข่าวที่ดูอยู่ ถ้าเจอ = true
                    updateSaveForLaterButton() //เปลี่ยนไอคอน
                } else {
                    Toast.makeText(this@NewsDetailsActivity, "ไม่สามารถตรวจสอบสถานะข่าวอ่านภายหลังได้", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Read_LaterData>>, t: Throwable) {
                Toast.makeText(this@NewsDetailsActivity, "เกิดข้อผิดพลาดในการตรวจสอบสถานะข่าวอ่านภายหลัง: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //เพิ่ม-ลบอ่านภายหลัง ตามสถานะ boolean
    private fun saveOrRemoveNewsForLater(memId: Int, newsId: Int) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        val readLaterData = Read_LaterData(memId, newsId)

        apiService.postReadLater(readLaterData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful || response.code() == 201) {
                    isSavedForLater = !isSavedForLater //สลับค่า boolean
                    updateSaveForLaterButton() //เปลี่ยนไอคอนให้ตรง
                    Toast.makeText(this@NewsDetailsActivity, if (isSavedForLater) "เพิ่มข่าวอ่านภายหลังสำเร็จ" else "ลบข่าวอ่านภายหลังสำเร็จ", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMessage = response.errorBody()?.string()
                    Toast.makeText(this@NewsDetailsActivity, "ไม่สามารถปรับสถานะข่าวอ่านภายหลังได้: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@NewsDetailsActivity, "เกิดข้อผิดพลาดในการปรับสถานะข่าวอ่านภายหลัง: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //เปลี่ยนไอคอนอ่านภายหลัง
    private fun updateSaveForLaterButton() {
        if (isSavedForLater) {
            saveForLaterButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.bookmark_24px))
        } else {
            saveForLaterButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.bookmark_add_24px))
        }
    }
}
