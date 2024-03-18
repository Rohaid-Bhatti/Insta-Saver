package com.hazel.instadownloader.core.database

import android.content.Context
import android.os.Environment
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.hazel.instadownloader.app.utils.DataStores
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class DownloadedUrlViewModel : ViewModel() {
    private var repository: DownloadedUrlRepository? = null
    var allDownloadedItems: LiveData<List<DownloadedItem>>? = null
    var allSearchedItems: LiveData<List<RecentSearchItem>>? = null
//    val filesLiveData: MutableLiveData<List<File>> = MutableLiveData()
    val _autoDownloadingMutableLiveData = MutableLiveData(false)
    val autoDownloadingLiveData: LiveData<Boolean> = _autoDownloadingMutableLiveData

    fun setAutoDownloadBoolean(isTurnOn: Boolean) {
        _autoDownloadingMutableLiveData.postValue(isTurnOn)
    }

    fun init(context: Context) {
        val dao = AppDatabase.getDatabase(context).downloadedUrlDao()
        repository = DownloadedUrlRepository(dao)
        allDownloadedItems = repository?.allDownloadedItems!!.asLiveData()
        allSearchedItems = repository?.allSearchItems!!.asLiveData()
    }

    fun insertDownloadedItem(item: DownloadedItem) {
        viewModelScope.launch {
            repository?.insertDownloadedItem(item)
        }
    }

    fun updateFileName(oldName: String, newName: String, newPath: String) {
        viewModelScope.launch {
            repository?.updateFileName(oldName, newName, newPath)
        }
    }

    fun deleteDownloadedItem(fileName: String) {
        viewModelScope.launch {
            repository?.deleteDownloadedItem(fileName)
        }
    }

    fun checkIfUrlExists(url: String, callback: (DownloadedItem?) -> Unit) {
        viewModelScope.launch {
            val exists = repository?.checkIfUrlExists(url)
            callback(exists)
        }
    }

    fun getCountOfSearchItems(): Int {
        return repository?.getCountOfSearchItems()!!
    }

    fun deleteOldestSearchItems(countToDelete: Int) {
        viewModelScope.launch {
            repository?.deleteOldestSearchItems(countToDelete)
        }
    }

    fun insertOrUpdateRecentItem(recentItem: RecentSearchItem) {
        viewModelScope.launch {
            repository?.insertOrUpdateRecentItem(recentItem)
        }
    }

    fun clearRecentSearchHistory() {
        viewModelScope.launch {
            repository?.clearRecentSearchHistory()
            allSearchedItems = repository?.allSearchItems!!.asLiveData()
        }
    }
}