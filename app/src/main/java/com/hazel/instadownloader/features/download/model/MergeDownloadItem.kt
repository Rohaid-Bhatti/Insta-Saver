package com.hazel.instadownloader.features.download.model

import java.io.File

data class MergeDownloadItem(
    var file: File,
    val username: String,
    val caption: String,
    val url: String,
    val profile: String
)
