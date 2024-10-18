package com.example.project_news_app

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    //LoginRequest
    @POST("api/loginMember/login")
    fun loginMember(@Body request: LoginRequest): Call<LoginResponse>


    //OTP Request/Verify + ResetPassword
    @POST("api/recovery_member/request-otp")
    fun requestOtp(@Body phoneNumber: PhoneNumberRequest): Call<OtpResponse>
    @POST("api/recovery_member/verify-otp")
    fun verifyOtp(@Body otpRequest: OtpRequest): Call<OtpResponse>
    @POST("api/reset_password_member/reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<ResetPasswordResponse>


    //Member
    @POST("api/member")
    fun postMember(@Body member: MemberData): Call<MemberData>
    @PUT("api/member/{id}")
    fun updateMember(@Path("id") memId: Int, @Body memberData: MemberData): Call<MemberData>


    //Category
    @GET("api/category")
    fun getCategory(): Call<List<CategoryData>>
    @GET("api/category/{id}")
    fun getCategoryById(@Path("id") catId: Int): Call<CategoryData>


    //News
    @GET("api/news")
    fun getAllNews(): Call<List<NewsData>>
    @GET("api/news/category/{id}")
    fun getNewsByCategoryPaged(
        @Path("id") catId: Int,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Call<List<NewsData>>
    @GET("api/news/{id}")
    fun getNewsById(@Path("id") newsId: Int): Call<NewsData>


    //Picture
    @GET("api/picture/news/{newsId}")
    fun getCoverImage(@Path("newsId") newsId: Int): Call<List<PictureData>>


    //News_Rating
    @GET("api/news_rating")
    fun getNewsRating(): Call<List<News_RatingData>>
    @PUT("api/news_rating/{memId}/{newsId}")
    fun putNewsRatingByMemId(
        @Path("memId") memId: Int,
        @Path("newsId") newsId: Int,
        @Body newsRating: News_RatingData
    ): Call<ResponseBody>


    //Total_Read
    @GET("api/total_read")
    fun getTotalRead(): Call<List<Total_ReadData>>
    @POST("api/total_read")
    fun postTotalRead(@Body totalRead: Total_ReadData): Call<Total_ReadData>


    //Sub_Category
    @GET("api/sub_category/tag/ids")
    fun getSubcategoriesByIds(@Query("ids") ids: List<Int>): Call<List<Sub_CategoryData>>


    //News_Sub_Cate
    @GET("api/news_sub_cate/tag/{newsId}")
    fun getNewsSubCateByNewsId(@Path("newsId") newsId: Int): Call<List<News_Sub_CateData>>


    //Major
    @GET("api/major/{id}")
    fun getMajorById(@Path("id") majorId: Int): Call<MajorData>


    //Favorite_Category
    @GET("api/favorite_category/{id}")
    fun getFavoriteCategoryByMemId(@Path("id") memId: Int): Call<List<Favorite_CategoryData>>
    @POST("api/favorite_category/update")
    fun updateFavoriteCategories(@Body updateRequest: UpdateFavoriteCategoriesRequest): Call<Void>
    @GET("api/favorite_category/news")
    fun getNewsByFavoriteCategory(@Query("memId") memId: Int): Call<List<NewsData>>


    //Read_Later
    @GET("api/read_later/{memId}")
    fun getReadLaterByMemId(@Path("memId") memId: Int): Call<List<Read_LaterData>>
    @POST("api/read_later")
    fun postReadLater(@Body readLater: Read_LaterData): Call<Void>


    //Read_History
    @GET("api/read_history/{memId}")
    fun getReadHistoryByMemId(@Path("memId") memId: Int): Call<List<Read_HistoryData>>
    @POST("api/read_history")
    fun addReadHistory(@Body readHistory: Read_HistoryData): Call<Void>
    @DELETE("api/read_history/{memId}/{newsId}")
    fun deleteReadHistory(@Path("memId") memId: Int, @Path("newsId") newsId: Int): Call<Void>

    // endpoint สำหรับดึงจำนวนการอ่านเฉพาะของสมาชิกคนเดียว
    @GET("api/total_read/member/{memId}")
    fun getMemberTotalReadById(
        @Path("memId") memId: Int
    ): Call<List<Total_ReadData>>

    // endpoint สำหรับดึงคะแนนของข่าวเฉพาะสมาชิกคนเดียว
    @GET("api/news_rating/member/{memId}")
    fun getMemberRatingByMemId(
        @Path("memId") memId: Int
    ): Call<List<News_RatingData>>
}

