package com.example.project_news_app

import java.util.Date

data class News(
    val NewsId: Int,
    val NewsName: String,
    val NewsDetails: String,
    val DateAdded: Date,
    val CatId: Int,
    val SubCatId: Int,
    val MajorId: Int
)
