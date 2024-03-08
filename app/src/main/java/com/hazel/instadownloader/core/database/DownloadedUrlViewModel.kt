package com.hazel.instadownloader.core.database

import android.content.Context
import android.os.Environment
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File

class DownloadedUrlViewModel : ViewModel() {
    private var repository: DownloadedUrlRepository?=null
    var allDownloadedItems: LiveData<List<DownloadedItem>>? = null
    val filesLiveData: MutableLiveData<List<File>> = MutableLiveData()

    fun init(context: Context){
        val dao = AppDatabase.getDatabase(context).downloadedUrlDao()
        repository = DownloadedUrlRepository(dao)
        allDownloadedItems = repository?.allDownloadedItems!!.asLiveData()
    }

    fun insertDownloadedItem(item: DownloadedItem) {
        viewModelScope.launch {
            repository?.insertDownloadedItem(item)
        }
    }

     fun getDownloadedItemByFileName(filename: String): LiveData<List<DownloadedItem>> {
           return repository!!.getDownloadedItemByFileName(filename)
     }

    fun loadFiles() {
        val files = loadFilesInBackground()
        filesLiveData.postValue(files)
    }

    private fun loadFilesInBackground(): List<File> {
        val downloadFolder =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "InstaDownloader")

        val files = downloadFolder.listFiles { file ->
            file.isFile &&
                    (file.extension.equals("jpg", ignoreCase = true) ||
                            file.extension.equals("png", ignoreCase = true) ||
                            file.extension.equals("jpeg", ignoreCase = true) ||
                            file.extension.equals("mp4", ignoreCase = true) ||
                            file.extension.equals("3gp", ignoreCase = true) ||
                            file.extension.equals("mkv", ignoreCase = true))
        }?.toList() ?: emptyList()

        return files.sortedByDescending { it.lastModified() }
    }
}