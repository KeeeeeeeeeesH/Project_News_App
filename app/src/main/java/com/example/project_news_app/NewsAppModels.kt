package com.example.project_news_app

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp
import java.util.Date

data class LoginRequest(
    val login: String,
    val password: String,
)
data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val user: MemberData?,
    val userId: String
)

data class ResetPasswordRequest(
    val newPassword: String,
    val confirmPassword: String
)
data class ResetPasswordResponse(
    val success: Boolean,
    val message: String?
)
data class PhoneNumberRequest(val phone: String)
data class OtpRequest(val otp: String)
data class OtpResponse(
    val success: Boolean?,
    val message: String?,
    val details: OtpDetails?
)


data class OtpDetails(
    val code: String?,
    val status: String?,
    val msg: String?,
    val credit_balance: Int?,
    val result: OtpResult?
)

data class OtpResult(
    val requestNo: String?,
    val token: String?,
    val ref: String?
)


data class MemberData(
    @SerializedName("Mem_Id") val memId: Int,
    @SerializedName("Mem_Fname") val memFname: String,
    @SerializedName("Mem_Lname") val memLname: String,
    @SerializedName("Mem_Username") val memUsername: String,
    @SerializedName("Mem_Password") val memPassword: String,
    @SerializedName("Mem_Email") val memEmail: String,
    @SerializedName("Mem_Phone") val memPhone: String,
    @SerializedName("Mem_Status") val memStatus: Int
)

data class CategoryData(
    @SerializedName("Cat_Id") val catId: Int,
    @SerializedName("Cat_Name") val catName: String
)

data class Sub_CategoryData(
    @SerializedName("Sub_Cat_Id") val subCatId: Int,
    @SerializedName("Sub_Cat_Name") val subCatName: String,
    @SerializedName("Cat_Id") val catId: Int
)

data class News_Sub_CateData(
    @SerializedName("News_Id") val newsId: Int,
    @SerializedName("Sub_Cat_Id") val subCatId: Int
)

data class MajorData(
    @SerializedName("Major_Id") val majorId: Int,
    @SerializedName("Major_Level") val majorLevel: Int,
)

data class NewsData(
    @SerializedName("News_Id") val newsId: Int,
    @SerializedName("News_Name") val newsName: String,
    @SerializedName("News_Details") val newsDetails: String,
    @SerializedName("Date_Added") val dateAdded: Date,
    @SerializedName("Cat_Id") val catId: Int,
    @SerializedName("Major_Id") val majorId: Int,
    var readCount: Int = 0,
    var ratingScore: Float = 0f,
    var coverImageUrl: String? = null
)

data class PictureData(
    @SerializedName("News_Id") val newsId: Int,
    @SerializedName("News_Pic") val pictureName: String
)

data class News_RatingData(
    @SerializedName("Mem_Id") val memId: Int,
    @SerializedName("News_Id") val newsId: Int,
    @SerializedName("Rating_Score") val ratingScore: Float
)

data class Favorite_CategoryData(
    @SerializedName("Mem_Id") val memId: Int,
    @SerializedName("Cat_Id") val catId: Int
)

data class UpdateFavoriteCategoriesRequest(
    @SerializedName("Mem_Id") val memId: Int,
    @SerializedName("Cat_Ids") val catIds: List<Int>
)

data class Read_LaterData(
    @SerializedName("Mem_Id") val memId: Int,
    @SerializedName("News_Id") val newsId: Int
)

data class ReadLaterWithNewsData(
    val newsId: Int,
    val dateAdded: Date,
    var readCount: Int,
    val newsName: String,
    var ratingScore: Float,
    var coverImage: String
)

data class Read_HistoryData(
    @SerializedName("Mem_Id") val memId: Int,
    @SerializedName("News_Id") val newsId: Int,
    @SerializedName("Read_Date") val readDate: Timestamp,
)

data class ReadHistoryWithNewsData(
    val newsId: Int,
    val readDate: Timestamp,
    var readCount: Int,
    val newsName: String,
    var ratingScore: Float,
    var coverImage: String
)

data class Total_ReadData(
    @SerializedName("Count_Id") val countId: Int,
    @SerializedName("News_Id") val newsId: Int
)
