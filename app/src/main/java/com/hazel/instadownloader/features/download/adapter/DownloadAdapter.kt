package com.hazel.instadownloader.features.download.adapter

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hazel.instadownloader.R
import com.hazel.instadownloader.app.activities.ImageActivity
import com.hazel.instadownloader.app.activities.PlayerActivity
import com.hazel.instadownloader.core.extensions.formatVideoDuration
import com.hazel.instadownloader.core.extensions.getVideoDuration
import com.hazel.instadownloader.core.extensions.isVideoFile
import com.hazel.instadownloader.core.extensions.shareFile
import com.hazel.instadownloader.core.extensions.shareFileToInstagram
import com.hazel.instadownloader.core.extensions.shareOnWhatsApp
import com.hazel.instadownloader.features.bottomSheets.DownloadMenu
import java.io.File

class DownloadAdapter(private val files: ArrayList<File>, private val delCallback: (isDel:Boolean) -> Unit) :
    RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_download, parent, false)
        return DownloadViewHolder(view, files)
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        holder.bind(files[position])

        Log.d("TESTING_ADAPTER", "onBindViewHolder: ${files.size}")
        holder.ivMenuIcon.setOnClickListener {
            showBottomSheet(holder.itemView.context, files[position], position)
        }
    }

    override fun getItemCount(): Int = files.size

    private fun showBottomSheet(context: Context, file: File, position: Int) {
        val bottomSheetFragment = DownloadMenu()
        bottomSheetFragment.setOnOptionClickListener(object : DownloadMenu.OnOptionClickListener {
            override fun onRepostInstagramClicked() {
                shareFileToInstagram(context, file, isVideoFile(file))
            }

            override fun onShareClicked() {
                shareFile(context, file)
            }

            override fun onShareWhatsAppClicked() {
                shareOnWhatsApp(context, file)
            }

            override fun onRenameClicked() {
                showRenameDialog(context, file, position)
            }

            override fun onDeleteClicked() {
                showDeleteConfirmationDialog(context, file)
            }
        })
        bottomSheetFragment.show(
            (context as AppCompatActivity).supportFragmentManager,
            bottomSheetFragment.tag
        )
    }

    private fun showDeleteConfirmationDialog(context: Context, file: File) {
        AlertDialog.Builder(context)
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete this file?")
            .setPositiveButton("Delete") { _, _ ->
                deleteFile(file)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteFile(file: File) {
        if (file.exists()) {
            file.delete()
            files.remove(file)
            delCallback(true)
            notifyDataSetChanged()
            Log.d("TESTING_ADAPTER", "deleteFile: ${files.size}")
        }
    }

    private fun showRenameDialog(context: Context, file: File, position: Int) {
        val editText = EditText(context)
        editText.setText(file.name)
        editText.setSelection(file.name.lastIndexOf('.'))

        AlertDialog.Builder(context)
            .setTitle("Rename File")
            .setView(editText)
            .setPositiveButton("Rename") { dialog, _ ->
                val newName = editText.text.toString()
                renameFile(context, file, newName, position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun renameFile(context: Context, file: File, newName: String, position: Int) {
        val newFile = File(file.parent, newName)
        if (file.renameTo(newFile)) {
            files[position] = newFile
            notifyItemChanged(position)
        } else {
            Toast.makeText(context, "Failed to rename file", Toast.LENGTH_SHORT).show()
        }
    }

    class DownloadViewHolder(itemView: View, var files: List<File>) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val playIcon: ImageView = itemView.findViewById(R.id.ivPlayIcon)
        private val textViewFileName: TextView = itemView.findViewById(R.id.textViewFileName)
        private val textViewDuration: TextView = itemView.findViewById(R.id.textViewDuration)
        private val playIconBg: LinearLayout = itemView.findViewById(R.id.playIconBg)
        val ivMenuIcon: ImageView = itemView.findViewById(R.id.ivMenuIcon)

        private lateinit var file: File

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(file: File) {
            this.file = file
            if (isVideoFile(file)) {
                playIcon.visibility = View.VISIBLE
                playIcon.contentDescription = "Play video"

                val duration = getVideoDuration(file)
                val formattedDuration = formatVideoDuration(duration)
                textViewDuration.text = formattedDuration
                textViewDuration.visibility = View.VISIBLE
                textViewFileName.text = file.name

                playIconBg.visibility = View.VISIBLE
            } else {
                playIcon.visibility = View.GONE
                textViewDuration.visibility = View.GONE
                textViewFileName.text = file.name

                playIconBg.visibility = View.GONE
            }

            Glide.with(itemView.context)
                .load(file)
                .into(imageView)
        }

        override fun onClick(v: View?) {
            if (isVideoFile(file)) {
                playVideo(itemView.context, adapterPosition)
            } else {
                showImage(itemView.context, adapterPosition)
            }
        }

        private fun playVideo(context: Context, position: Int) {
            val filteredVideos = files.filter { isVideoFile(it) }
            val videoUris = filteredVideos.map { it.toUri().toString() }
            val clickedFile = files[position]
            val clickedVideoIndex = filteredVideos.indexOf(clickedFile)
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putStringArrayListExtra("videoUri", ArrayList(videoUris))
            intent.putExtra("positionVideo", clickedVideoIndex)
            context.startActivity(intent)
        }

        private fun showImage(context: Context, position: Int) {
            val filteredImages = files.filter { !isVideoFile(it) }
            val imageUris = filteredImages.map { it.toUri().toString() }
            val clickedFile = files[position]
            val clickedImageIndex = filteredImages.indexOf(clickedFile)
            val intent = Intent(context, ImageActivity::class.java)
            intent.putStringArrayListExtra("imageUris", ArrayList(imageUris))
            intent.putExtra("position", clickedImageIndex)
            context.startActivity(intent)
        }
    }
}