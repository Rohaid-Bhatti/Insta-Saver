package com.hazel.instadownloader.core.database

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class DownloadedUrlRepository(
    private val downloadedUrlDao: DownloadedUrlDao,

) {
    val allDownloadedItems: Flow<List<DownloadedItem>> = downloadedUrlDao.getAllDownloadedItems()


    suspend fun insertDownloadedItem(item: DownloadedItem) {
        downloadedUrlDao.insertDownloadedItem(item)
    }
}