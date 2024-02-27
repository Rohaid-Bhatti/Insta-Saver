package com.hazel.instadownloader.core.database

import kotlinx.coroutines.flow.Flow

class DownloadedUrlRepository(private val downloadedUrlDao: DownloadedUrlDao) {
    val allDownloadedUrls: Flow<List<DownloadedUrl>> = downloadedUrlDao.getAllDownloadedUrls()

    suspend fun insertDownloadedUrl(downloadedUrl: DownloadedUrl) {
        downloadedUrlDao.insertDownloadedUrl(downloadedUrl)
    }
}