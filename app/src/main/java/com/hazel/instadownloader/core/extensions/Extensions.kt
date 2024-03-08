package com.hazel.instadownloader.core.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.hazel.instadownloader.R
import com.hazel.instadownloader.app.activities.ImageActivity
import com.hazel.instadownloader.app.activities.PlayerActivity
import com.hazel.instadownloader.app.utils.SHARE_APP_INTENT_TAG
import com.hazel.instadownloader.features.dialogBox.DeleteConfirmationDialogFragment
import java.io.File

fun Context.shareApp() {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
    shareIntent.putExtra(
        Intent.EXTRA_TEXT, getString(R.string.share_app_message) + packageName
    )
    startActivity(Intent.createChooser(shareIntent, SHARE_APP_INTENT_TAG))
}

object LastClickTimeSingleton {
    var lastClickTime: Long = 0
}

fun debounce( view: View,action: (view: View) -> Unit) {
    if (SystemClock.elapsedRealtime() - LastClickTimeSingleton.lastClickTime < 500L) return
    else action(view)
    LastClickTimeSingleton.lastClickTime = SystemClock.elapsedRealtime()
}

fun View.setClickWithDebounce(action: (view: View) -> Unit) {
    setOnClickListener {
        debounce(it, action)
    }
}

fun isVideoFile(file: File): Boolean {
    return file.extension in arrayOf(
        "mp4",
        "3gp",
        "mkv"
    )
}

fun isImageFile(file: File): Boolean {
    return file.extension in arrayOf(
        "jpg",
        "png",
        "jpeg"
    )
}

fun getVideoDuration(file: File): Long {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(file.path)
    val durationString =
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    return durationString?.toLong() ?: 0L
}

fun formatVideoDuration(duration: Long): String {
    val seconds = duration / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

fun shareFile(context: Context, file: File) {
    val fileUri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = context.contentResolver.getType(fileUri)
        putExtra(Intent.EXTRA_STREAM, fileUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share File"))
}

fun shareOnWhatsApp(context: Context, file: File) {
    val fileUri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = context.contentResolver.getType(fileUri)
        putExtra(Intent.EXTRA_STREAM, fileUri)
        putExtra(Intent.EXTRA_TEXT, "Shared via your app")
        setPackage("com.whatsapp")
    }
    context.startActivity(shareIntent)
}

fun shareFileToInstagram(context: Context, file: File, isVideo: Boolean) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    val feedIntent = Intent(Intent.ACTION_SEND).apply {
        type = if (isVideo) "video/*" else "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        setPackage("com.instagram.android")
    }

    val storiesIntent = Intent("com.instagram.share.ADD_TO_STORY").apply {
        setDataAndType(uri, if (isVideo) "mp4" else "jpg")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        setPackage("com.instagram.android")
    }

    context.grantUriPermission(
        "com.instagram.android", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
    )

    val chooserIntent = Intent.createChooser(feedIntent, "Posting").apply {
        putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(storiesIntent))
    }

    context.startActivity(chooserIntent)
}

fun playVideo(context: Context, position: Int , files: List<File>) {
    val filteredVideos = files.filter { isVideoFile(it) }
    val videoUris = filteredVideos.map { it.toUri().toString() }
    val clickedFile = files[position]
    val clickedVideoIndex = filteredVideos.indexOf(clickedFile)
    val intent = Intent(context, PlayerActivity::class.java)
    intent.putStringArrayListExtra("videoUri", ArrayList(videoUris))
    intent.putExtra("positionVideo", clickedVideoIndex)
    context.startActivity(intent)
}

fun showImage(context: Context, position: Int , files: List<File>) {
    val filteredImages = files.filter { !isVideoFile(it) }
    val imageUris = filteredImages.map { it.toUri().toString() }
    val clickedFile = files[position]
    val clickedImageIndex = filteredImages.indexOf(clickedFile)
    val intent = Intent(context, ImageActivity::class.java)
    intent.putStringArrayListExtra("imageUris", ArrayList(imageUris))
    intent.putExtra("position", clickedImageIndex)
    context.startActivity(intent)
}

fun String.openInstagramPostInApp(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(this)).apply {
        setPackage("com.instagram.android")
    }

    if (isIntentAvailable(context, intent)) {
        startActivity(context, intent, null)
    } else {
        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(this))
        startActivity(context, fallbackIntent, null)
    }
}

@SuppressLint("QueryPermissionsNeeded")
private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
    val packageManager = context.packageManager
    val activities = packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return activities?.isNotEmpty() ?: false
}