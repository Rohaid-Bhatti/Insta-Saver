package com.hazel.instadownloader.core.database

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class DownloadedUrlRepository(
    private val downloadedUrlDao: DownloadedUrlDao,

    ) {
    val allDownloadedItems: Flow<List<DownloadedItem>> = downloadedUrlDao.getAllDownloadedItems()
    val allSearchItems: Flow<List<RecentSearchItem>> = downloadedUrlDao.getAllSearchItems()

    suspend fun insertDownloadedItem(item: DownloadedItem) {
        downloadedUrlDao.insertDownloadedItem(item)
    }

    suspend fun updateFileName(oldName: String, newName: String) {
        downloadedUrlDao.updateFileName(oldName, newName)
    }

    suspend fun deleteDownloadedItem(fieName: String) {
        downloadedUrlDao.deleteDownloadedItem(fieName)
    }

    suspend fun checkIfUrlExists(url: String): DownloadedItem? {
        return downloadedUrlDao.checkIfUrlExists(url)
    }

    suspend fun insertSearchItem(item: RecentSearchItem) {
        downloadedUrlDao.insertSearchItem(item)
    }

    fun getCountOfSearchItems(): Int {
        return downloadedUrlDao.getCountOfSearchItems()
    }

    suspend fun deleteOldestSearchItems(countToDelete: Int) {
        downloadedUrlDao.deleteOldestSearchItems(countToDelete)
    }
}