package com.hazel.instadownloader.features.download.adapter

import com.hazel.instadownloader.core.database.DownloadedItem

interface DeleteFileCallback {
    fun onShowingMenu(mergeItem: DownloadedItem, position: Int)

    fun onDeleteFileSelected(isFileSelected: Boolean)
}