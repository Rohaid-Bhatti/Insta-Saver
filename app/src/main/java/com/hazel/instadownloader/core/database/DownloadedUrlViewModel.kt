package com.hazel.instadownloader.core.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DownloadedUrlViewModel(private val repository: DownloadedUrlRepository) : ViewModel() {
    val allDownloadedItems: LiveData<List<DownloadedItem>> = repository.allDownloadedItems.asLiveData()

    fun insertDownloadedItem(item: DownloadedItem) {
        viewModelScope.launch {
            repository.insertDownloadedItem(item)
        }
    }
}