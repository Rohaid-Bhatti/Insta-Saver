package com.hazel.instadownloader.core.database

import android.util.Log
import kotlinx.coroutines.flow.Flow

class DownloadedUrlRepository(
    private val downloadedUrlDao: DownloadedUrlDao,

    ) {
    val allDownloadedItems: Flow<List<DownloadedItem>> = downloadedUrlDao.getAllDownloadedItems()
    val allSearchItems: Flow<List<RecentSearchItem>> = downloadedUrlDao.getAllSearchItems()

    suspend fun insertDownloadedItem(item: DownloadedItem) {
        downloadedUrlDao.insertDownloadedItem(item)
    }

    suspend fun updateFileName(oldName: String, newName: String, newPath: String) {
        downloadedUrlDao.updateFileName(oldName, newName, newPath)
    }

    suspend fun deleteDownloadedItem(fieName: String) {
        downloadedUrlDao.deleteDownloadedItem(fieName)
    }

    suspend fun checkIfUrlExists(url: String): DownloadedItem? {
        return downloadedUrlDao.checkIfUrlExists(url)
    }

    fun getCountOfSearchItems(): Int {
        return downloadedUrlDao.getCountOfSearchItems()
    }

    suspend fun deleteOldestSearchItems(countToDelete: Int) {
        downloadedUrlDao.deleteOldestSearchItems(countToDelete)
    }

    suspend fun insertOrUpdateRecentItem(recentItem: RecentSearchItem) {
        val existingItem = downloadedUrlDao.findRecentByUsername(recentItem.username)
        if (existingItem != null) {
            downloadedUrlDao.deleteSearchItems(recentItem.username)
            downloadedUrlDao.insertSearchItem(recentItem)
        } else {
            downloadedUrlDao.insertSearchItem(recentItem)
        }
    }

    suspend fun clearRecentSearchHistory() {
        downloadedUrlDao.clearRecentSearchHistory()
    }
}