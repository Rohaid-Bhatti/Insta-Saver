package com.hazel.instadownloader.features.browser.downloadManager

interface Downloader {
    fun downloadFile(url: String, filename: String): Long
}