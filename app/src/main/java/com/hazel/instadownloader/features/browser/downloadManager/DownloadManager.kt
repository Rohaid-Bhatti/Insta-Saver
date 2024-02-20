package com.hazel.instadownloader.features.browser.downloadManager

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import java.io.File

class DownloadManager(
    var context: Context
) : Downloader {

    private val downloadManager =
        context.getSystemService(DownloadManager::class.java)

    override fun downloadFile(url: String, filename: String): Long {
        val downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/InstaDownloader/"
        val file = File(downloadDirectory)
        if (!file.exists()) {
            file.mkdirs()
        }

        val request = DownloadManager.Request(url.toUri())
//            .setMimeType("image/jpeg")
//            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(filename)
//            .addRequestHeader("Authorization", "Bearer <token>")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "InstaDownloader/$filename")
        return downloadManager.enqueue(request)
    }
}