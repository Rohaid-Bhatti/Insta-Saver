package com.hazel.instadownloader.features.download.adapter

import com.hazel.instadownloader.features.download.model.MergeDownloadItem

interface DeleteFileCallback {
    fun onShowingMenu(mergeItem: MergeDownloadItem, position: Int, selectedFiles: HashSet<MergeDownloadItem>)
    fun onDeleteFileSelected(isFileSelected: Boolean)
}