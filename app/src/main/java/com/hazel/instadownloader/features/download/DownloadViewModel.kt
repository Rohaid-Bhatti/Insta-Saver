package com.hazel.instadownloader.features.download

import android.os.Environment
import android.os.FileObserver
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DownloadViewModel : ViewModel() {

    val filesLiveData: MutableLiveData<List<File>> = MutableLiveData()

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