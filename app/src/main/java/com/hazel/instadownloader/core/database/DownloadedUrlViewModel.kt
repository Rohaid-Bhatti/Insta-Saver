package com.hazel.instadownloader.core.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DownloadedUrlViewModel(private val repository: DownloadedUrlRepository) : ViewModel() {
    val allDownloadedUrls: LiveData<List<DownloadedUrl>> = repository.allDownloadedUrls.asLiveData()

    fun insertDownloadedUrl(downloadedUrl: DownloadedUrl) {
        viewModelScope.launch {
            repository.insertDownloadedUrl(downloadedUrl)
        }
    }
}