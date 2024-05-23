package com.example.project_news_app

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    //LoginRequest
    @POST("api/loginMember/login")
    fun loginMember(@Body request: LoginRequest): Call<LoginResponse>

    //MemberData
    @GET("api/member")
    fun getMember(): Call<List<MemberData>>
    @GET("api/member/{id}")
    fun getMemberById(@Path("id") memId: Int): Call<MemberData>
    @POST("api/member")
    fun postMember(@Body member: MemberData): Call<MemberData>

    //CategoryData
    @GET("api/category")
    fun getCategory(): Call<List<CategoryData>>
    @GET("api/category/{id}")
    fun getCategoryById(@Path("id") catId: Int): Call<CategoryData>

    //Sub_CategoryData
    @GET("api/sub_category")
    fun getSubcategory(): Call<List<Sub_CategoryData>>
    @GET("api/sub_category/{id}")
    fun getSubCategoryById(@Path("id") subCatId: Int): Call<Sub_CategoryData>

    //MajorData
    @GET("api/major")
    fun getMajor(): Call<List<MajorData>>
    @GET("api/major/{id}")
    fun getMajorById(@Path("id") majorId: Int): Call<MajorData>

    //NewsData
    @GET("api/news")
    fun getNews(): Call<List<NewsData>>
    @GET("api/news/{id}")
    fun getNewsById(@Path("id") newsId: Int): Call<NewsData>

    //PictureData
    @GET("api/picture")
    fun getPicture(): Call<List<PictureData>>
    @GET("api/picture/{id}")
    fun getPictureByNewsId(@Path("id") newsId: Int): Call<PictureData>

    //Favorite_CategoryData
    @GET("api/favorite_category")
    fun getFavoriteCategory(): Call<List<Favorite_CategoryData>>
    @GET("api/favorite_category/{id}")
    fun getFavoriteCategoryByMemId(@Path("id") memId: Int): Call<Favorite_CategoryData>
    @POST("api/favorite_category")
    fun postFavoriteCategory(@Body favorite: Favorite_CategoryData): Call<Favorite_CategoryData>
    @DELETE("api/favorite_category/{id}")
    fun deleteFavoriteCategory(@Path("id") memId: Int): Call<Void>

    //Read_LaterData
    @GET("api/read_later")
    fun getReadLater(): Call<List<Read_LaterData>>
    @GET("api/read_later/{id}")
    fun getReadLaterByMemId(@Path("id") memId: Int): Call<Read_LaterData>
    @POST("api/read_later")
    fun postReadLater(@Body readLater: Read_LaterData): Call<Read_LaterData>
    @DELETE("api/read_later/{id}")
    fun deleteReadLater(@Path("id") memId: Int): Call<Void>

    //News_RatingData
    @GET("api/news_rating")
    fun getNewsRating(): Call<List<News_RatingData>>
    @GET("api/news_rating/{id}")
    fun getNewsRatingByNewsId(@Path("id") newsId: Int): Call<News_RatingData>
    @POST("api/news_rating")
    fun postNewsRating(@Body newsRating: News_RatingData): Call<News_RatingData>
    @PUT("api/news_rating/{id}")
    fun putNewsRatingByMemId(@Path("id") memId: Int, @Body newsRating: News_RatingData): Call<News_RatingData>

    //Read_HistoryData
    @GET("api/read_history")
    fun getReadHistory(): Call<List<Read_HistoryData>>
    @GET("api/read_history/{id}")
    fun getReadHistoryByMemId(@Path("id") memId: Int): Call<Read_HistoryData>
    @POST("api/read_history")
    fun postReadHistory(@Body readHistory: Read_HistoryData): Call<Read_HistoryData>
    @PUT("api/read_history")
    fun putReadHistoryByMemId(@Path("id") memId: Int, @Body readHistory: Read_HistoryData): Call<Read_HistoryData>
    @DELETE("api/read_history/{id}")
    fun deleteReadHistory(@Path("id") memId: Int): Call<Void>

    //Total_ReadData
    @GET("api/total_read")
    fun getTotalRead(): Call<List<Total_ReadData>>
    @GET("api/total_read/{id}")
    fun getTotalReadById(@Path("id") countId: Int): Call<Total_ReadData>
    @POST("api/total_read")
    fun postTotalRead(@Body totalRead: Total_ReadData): Call<Total_ReadData>

    //News_Sub_CateData
    @GET("api/news_sub_cate")
    fun getNewsSubCate(): Call<List<News_Sub_CateData>>
    @GET("api/news_sub_cate/{id}")
    fun getNewsSubCateByNewsId(@Path("id") newsId: Int): Call<News_Sub_CateData>
}
