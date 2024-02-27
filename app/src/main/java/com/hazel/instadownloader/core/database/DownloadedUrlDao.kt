package com.hazel.instadownloader.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface DownloadedUrlDao {
    @Query("SELECT * FROM ss_ass")
    fun getAllDownloadedUrls(): Flow<List<DownloadedUrl>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedUrl(downloadedUrl: DownloadedUrl)
}