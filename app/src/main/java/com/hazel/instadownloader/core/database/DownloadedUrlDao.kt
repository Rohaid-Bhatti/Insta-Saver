package com.hazel.instadownloader.core.database

import androidx.room.Dao
import androidx.room.Delete
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

    @Query("UPDATE downloaded_urls SET fileName = :newName, filePath = :newPath WHERE fileName = :oldName")
    suspend fun updateFileName(oldName: String, newName: String, newPath: String)

    @Query("DELETE FROM downloaded_urls WHERE fileName = :fileName")
    suspend fun deleteDownloadedItem(fileName: String)

    @Query("SELECT * FROM downloaded_urls WHERE url = :url")
    suspend fun checkIfUrlExists(url: String): DownloadedItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchItem(item: RecentSearchItem)

    @Query("SELECT * FROM recent_table")
    fun getAllSearchItems(): Flow<List<RecentSearchItem>>

    @Query("SELECT COUNT(*) FROM recent_table")
    fun getCountOfSearchItems(): Int

    @Query("DELETE FROM recent_table WHERE id IN (SELECT id FROM recent_table ORDER BY id ASC LIMIT :countToDelete)")
    suspend fun deleteOldestSearchItems(countToDelete: Int)

    @Query("DELETE FROM recent_table WHERE username = :userName")
    suspend fun deleteSearchItems(userName: String)

    @Query("SELECT * FROM recent_table WHERE username = :cleanUsername")
    suspend fun findRecentByUsername(cleanUsername: String): RecentSearchItem?

    @Query("DELETE FROM recent_table")
    suspend fun clearRecentSearchHistory()
}