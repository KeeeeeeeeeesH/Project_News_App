package com.example.project_news_app

import NetworkUtil
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.project_news_app.adapters.CategoryAdapter
import com.example.project_news_app.adapters.NewsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var searchButton: ImageButton
    private lateinit var toggleCategories: ImageButton
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var categoryAdapter: CategoryAdapter


    private var currentCategoryId: Int = 0 // Default ไปที่ "แนะนำ"
    private var currentPage: Int = 0 // index ของหน้าข่าวสำหรับการโหลดข่าวเพิ่ม
    private var allNewsList: List<NewsData> = listOf() // List ของ News จาก NewsData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        searchButton = findViewById(R.id.search_button)
        toggleCategories = findViewById(R.id.toggle_categories)
        categoriesRecyclerView = findViewById(R.id.categories_recycler_view)
        newsRecyclerView = findViewById(R.id.news_recycler_view)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        //กำหนดค่า RecyclerView กับ Adapter
        categoriesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        newsRecyclerView.layoutManager = LinearLayoutManager(this)

        //แสดงข่าวตามหมวดหมู่ที่เลือก
        categoryAdapter = CategoryAdapter { category ->
            currentCategoryId = category.catId
            categoryAdapter.setSelectedCategory(category.catId)
            loadNewsByCategory(category.catId)
        }
        newsAdapter = NewsAdapter(listOf(), NewsAdapter.NewsType.GENERAL) //ใช้ Type General

        categoriesRecyclerView.adapter = categoryAdapter
        newsRecyclerView.adapter = newsAdapter

        loadCategories()
        loadNewsByCategory(currentCategoryId)

        //ตั้งค่า bottom nav
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    currentCategoryId = 0
                    loadNewsByCategory(currentCategoryId)
                    true
                }
                R.id.navigation_favorite -> {
                    startActivity(Intent(this, SelectFavoriteActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
        bottomNavigation.selectedItemId = R.id.navigation_home

//        toggleCategories.setOnClickListener {
//            onToggleCategoriesClick()
//        }

        //โหลดข่าวเพิ่ม เมื่อ Scroll ลง
        newsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    loadMoreNews()
                }
            }
        })

        // ตั้งค่า swipe refresh
        swipeRefreshLayout.setOnRefreshListener {
            loadNewsByCategory(currentCategoryId)
        }

        //เข้าหน้า search
        searchButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SearchNewsActivity::class.java)
            startActivity(intent)
        }

        //ตรวจสอบเวอร์ชัน SDK ในการขอ permission ส่งแจ้งเตือน
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        //ให้ user subscribe topic สำหรับรับแจ้งเตือนจาก firebase
        FirebaseMessaging.getInstance().subscribeToTopic("news_topic")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "สมัครรับข้อมูลจาก news_topic สำเร็จ")
                } else {
                    Log.d("FCM", "สมัครรับข้อมูลจาก news_topic ล้มเหลว")
                }
            }

        //sharedPref
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val memId = sharedPreferences.getInt("memId", -1)

       //ถ้าเจอ memId ให้ subscribe topic
        if (memId != -1) {
            FirebaseMessaging.getInstance().subscribeToTopic("user_$memId")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FCM", "การสมัครรับข้อมูลโดย user_$memId สำเร็จ")
                    } else {
                        Log.d("FCM", "การสมัครรับข้อมูลโดย user_$memId ล้มเหลว")
                    }
                }
        } else {
            Log.e("FCM", "ล้มเหลวในการรับข้อมูลผู้ใช้ไปยังการสมัครรับข้อมูล")
        }

    }

//    private fun onToggleCategoriesClick() {
//    }

    //โหลดหมวดหมู่ข่าว
    private fun loadCategories() {
        //ตรวจสอบการเชื่อมต่อ internet
        if (!NetworkUtil.isInternetAvailable(this)) {
            showNoInternetError()
            swipeRefreshLayout.isRefreshing = false
            return
        }

        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        apiService.getCategory().enqueue(object : Callback<List<CategoryData>> {
            override fun onResponse(call: Call<List<CategoryData>>, response: Response<List<CategoryData>>) {
                if (response.isSuccessful) {
                    //แสดงรายการหมวดหมู่พร้อมหมวดหมู่แนะนำ
                    val categories = response.body() ?: listOf()
                    val allCategories = listOf(CategoryData(0, "แนะนำ")) + categories //list ของ category และ แนะนำ
                    categoryAdapter.setCategories(allCategories)
                } else {
                    Toast.makeText(this@MainActivity, "โหลดข้อมูลหมวดหมู่ไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<CategoryData>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //โหลดข่าวตามหมวดหมู่
    private fun loadNewsByCategory(catId: Int) {
        currentPage = 0 //set index = 0 เพราะจะดึงทีละ 5 ข่าว
        allNewsList = listOf()
        loadMoreNews()
        swipeRefreshLayout.isEnabled = true
    }

    //โหลดข่าวตามหมวดหมู่เพิ่มเรื่อยๆ
    private fun loadMoreNews() {
        if (!NetworkUtil.isInternetAvailable(this)) {
            showNoInternetError()
            swipeRefreshLayout.isRefreshing = false
            return
        }
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)
        //limit 5 ข่าว / ครั้ง
        apiService.getNewsByCategoryPaged(currentCategoryId, currentPage, 5).enqueue(object : Callback<List<NewsData>> {
            override fun onResponse(call: Call<List<NewsData>>, response: Response<List<NewsData>>) {
                if (response.isSuccessful) {
                    val newsList = response.body() ?: listOf()
                    val newNewsList = newsList.distinctBy { it.newsId } //ลบถ้าเจอข่าวซ้ำ
                    //ถ้าโหลดครั้งแรก
                    if (currentPage == 0) {
                        allNewsList = newNewsList
                        newsAdapter.setNews(newNewsList) //ดึง list ใน adapter มา
                    } else {
                        //ข่าวเดิม+ข่าวใหม่-ลบข่าวซ้ำ
                        allNewsList = (allNewsList + newNewsList).distinctBy { it.newsId }
                        newsAdapter.addNews(newNewsList) //เพิ่มข่าวใหม่ลง adapter
                    }
                    fetchReadCounts(newsList) //ดึงจำนวนการอ่าน
                    currentPage++
                    swipeRefreshLayout.isRefreshing = false
                } else {
                    Toast.makeText(this@MainActivity, "โหลดข้อมูลข่าวไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }

            override fun onFailure(call: Call<List<NewsData>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    //ดึงจำนวนการอ่าน
    private fun fetchReadCounts(newsList: List<NewsData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)

        apiService.getTotalRead().enqueue(object : Callback<List<Total_ReadData>> {
            override fun onResponse(call: Call<List<Total_ReadData>>, response: Response<List<Total_ReadData>>) {
                if (response.isSuccessful) {
                    val readCounts = response.body() ?: listOf()
                    //วนลูปหาจำนวนของแต่ละข่าว
                    newsList.forEach { news ->
                        news.readCount = readCounts.count { it.newsId == news.newsId }
                    }
                    fetchRatings(newsList) //ไปดึงคะแนนต่อ
                } else {
                    Toast.makeText(this@MainActivity, "โหลดยอดการอ่านไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Total_ReadData>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //ดึงคะแนนข่าว
    private fun fetchRatings(newsList: List<NewsData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)

        apiService.getNewsRating().enqueue(object : Callback<List<News_RatingData>> {
            override fun onResponse(call: Call<List<News_RatingData>>, response: Response<List<News_RatingData>>) {
                if (response.isSuccessful) {
                    val ratings = response.body() ?: listOf()
                    //loop เหมือนตัวข้างบน
                    newsList.forEach { news ->
                        val newsRatings = ratings.filter { it.newsId == news.newsId }
                        //หาค่าเฉลี่ยของข่าวที่ได้
                        news.ratingScore = if (newsRatings.isNotEmpty()) {
                            newsRatings.sumByDouble { it.ratingScore.toDouble() }
                                .toFloat() / newsRatings.size
                        } else {
                            0f
                        }
                    }
                    fetchCoverImages(newsList) //ไปดึงรูปปกต่อ
                } else {
                    Toast.makeText(this@MainActivity, "โหลดคะแนนข่าวไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<News_RatingData>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //ดึงรูปหน้าปก
    private fun fetchCoverImages(newsList: List<NewsData>) {
        val apiService = RetrofitClient.getClient(this).create(ApiService::class.java)

        newsList.forEach { news ->
            apiService.getCoverImage(news.newsId).enqueue(object : Callback<List<PictureData>> {
                override fun onResponse(call: Call<List<PictureData>>, response: Response<List<PictureData>>) {
                    if (response.isSuccessful) {
                        val pictures = response.body() ?: listOf()
                        val coverImage = pictures.find { it.pictureName.startsWith("cover_") } //หาภาพที่ชื่อรูปเป็น cover_
                        //ตั้งค่าให้ url ของรูป
                        news.coverImage = coverImage?.let { "${RetrofitClient.getClient(this@MainActivity).baseUrl()}uploads/${it.pictureName}" }
                        //จบทุกอย่างแล้วอัปเดท RecyclerView
                        newsAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@MainActivity, "โหลดรูปภาพหน้าปกข่าวไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<PictureData>>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "เกิดข้อผิดพลาด: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    //ตรวจสอบกาสรเชื่อมต่อ
    private fun showNoInternetError() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("ไม่มีการเชื่อมต่ออินเทอร์เน็ต")
            .setMessage("ไม่สามารถโหลดรายการข่าวได้ โปรดตรวจสอบการเชื่อมต่ออินเทอร์เน็ตของคุณ")
            .setPositiveButton("ตกลง", null)
            .create()
        dialog.show()
    }

//    companion object {
//        private const val SEARCH_REQUEST_CODE = 1
//    }
}

