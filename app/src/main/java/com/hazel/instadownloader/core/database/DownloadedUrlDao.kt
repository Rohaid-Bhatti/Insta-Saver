package com.hazel.instadownloader.core.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedUrlDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedItem(item: DownloadedItem)

    @Query("SELECT * FROM downloaded_urls")
    fun getAllDownloadedItems(): Flow<List<DownloadedItem>>
}