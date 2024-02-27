package com.hazel.instadownloader.core.database

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ss_ass")
@Keep
data class DownloadedUrl(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val fileName: String
)
