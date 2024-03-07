package com.hazel.instadownloader.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_urls")
data class DownloadedItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val fileName: String,
    val postUrl: String,
    val caption: String,
    val username: String
)