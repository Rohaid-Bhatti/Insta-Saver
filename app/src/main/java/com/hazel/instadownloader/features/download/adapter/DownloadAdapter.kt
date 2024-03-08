package com.hazel.instadownloader.features.download.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.hazel.instadownloader.R
import com.hazel.instadownloader.core.database.AppDatabase
import com.hazel.instadownloader.core.extensions.isVideoFile
import com.hazel.instadownloader.core.extensions.openInstagramPostInApp
import com.hazel.instadownloader.core.extensions.playVideo
import com.hazel.instadownloader.core.extensions.shareFile
import com.hazel.instadownloader.core.extensions.shareFileToInstagram
import com.hazel.instadownloader.core.extensions.shareOnWhatsApp
import com.hazel.instadownloader.core.extensions.showImage
import com.hazel.instadownloader.features.bottomSheets.DownloadMenu
import com.hazel.instadownloader.features.dialogBox.DeleteConfirmationDialogFragment
import com.hazel.instadownloader.features.dialogBox.RenameDialogFragment
import com.hazel.instadownloader.features.download.model.MergeDownloadItem
import java.io.File

class DownloadAdapter(
    private val mergeItems: List<MergeDownloadItem>,
    private val delCallback: (isDel: Boolean) -> Unit
) : RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder>() {

    val selectedFiles = HashSet<MergeDownloadItem>()
    var isSelectionModeEnabled = false
    private var selectionModeListener: SelectionModeListener? = null
    var isAllSelected = false

//    private var username: String = ""
//    private var caption: String = ""

    interface SelectionModeListener {
        fun onSelectionModeEnabled(enabled: Boolean)
    }

    fun setSelectionModeListener(listener: SelectionModeListener) {
        selectionModeListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_download, parent, false)
        return DownloadViewHolder(view)
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        val mergeItem = mergeItems[position]
        holder.bind(mergeItem)

        holder.ivMenuIcon.setOnClickListener {
            showBottomSheet(holder.itemView.context, mergeItem, position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun toggleSelection(item: MergeDownloadItem, onAllAdded: () -> Unit) {

        if (selectedFiles.contains(item)) {
            selectedFiles.remove(item)
            onAllAdded()
        } else {
            selectedFiles.add(item)
            onAllAdded()
        }

        selectionModeListener?.onSelectionModeEnabled(selectedFiles.isNotEmpty())
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAllItems(selectAll: Boolean) {
        if (selectAll) {
            selectedFiles.addAll(mergeItems)
        } else {
            selectedFiles.clear()
        }
        updateAllSelectedStatus()
        notifyDataSetChanged()
        selectionModeListener?.onSelectionModeEnabled(selectedFiles.isNotEmpty())
    }

    private fun updateAllSelectedStatus() {
        isAllSelected = selectedFiles.size == mergeItems.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteSelectedFiles() {
        selectedFiles.forEach { item ->
            item.file.delete()
        }
        selectedFiles.clear()
        isSelectionModeEnabled = false
        notifyDataSetChanged()
        delCallback(true)
    }

    override fun getItemCount(): Int = mergeItems.size

    private fun showBottomSheet(context: Context, item: MergeDownloadItem, position: Int) {
        val bottomSheetFragment = DownloadMenu()

        bottomSheetFragment.setOnOptionClickListener(object : DownloadMenu.OnOptionClickListener {
            override fun onRepostInstagramClicked() {
                shareFileToInstagram(context, item.file, isVideoFile(item.file))
            }

            override fun onShareClicked() {
                shareFile(context, item.file)
            }

            override fun onShareWhatsAppClicked() {
                shareOnWhatsApp(context, item.file)
            }

            override fun onRenameClicked() {
                showRenameDialog(context, item, position)
            }

            override fun onDeleteClicked() {
                showDeleteConfirmationDialog(context, item)
            }

            override fun onPostOpenInstagram() {
                item.url.openInstagramPostInApp(context)
            }
        })

        bottomSheetFragment.show(
            (context as AppCompatActivity).supportFragmentManager,
            bottomSheetFragment.tag
        )
    }

    private fun showDeleteConfirmationDialog(context: Context, item: MergeDownloadItem) {
        val dialog = DeleteConfirmationDialogFragment("1") {
            deleteFile(context, item)
        }
        dialog.show(
            (context as AppCompatActivity).supportFragmentManager,
            "DeleteConfirmationDialog"
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteFile(context: Context, item: MergeDownloadItem) {
        val contentUri = MediaStore.Files.getContentUri("external")
        val selection = "${MediaStore.Files.FileColumns.DATA} = ?"
        val selectionArgs = arrayOf(item.file.absolutePath)
        val rowsDeleted = context.contentResolver.delete(contentUri, selection, selectionArgs)
        if (rowsDeleted > 0) {
            delCallback(true)
//            files.remove(item.file)
            notifyDataSetChanged()
        }
    }

    private fun showRenameDialog(context: Context, item: MergeDownloadItem, position: Int) {
        val renameDialogFragment = RenameDialogFragment().apply {
            arguments = Bundle().apply {
                putString("fileName", item.file.name)
                putInt("position", position)
            }
            setRenameListener(object : RenameDialogFragment.RenameListener {
                override fun onRenameConfirmed(newName: String) {
                    renameFile(context, item, newName, position)
                }
            })
        }
        renameDialogFragment.show((context as AppCompatActivity).supportFragmentManager, "RenameDialog")
    }

    private fun renameFile(context: Context, item: MergeDownloadItem, newName: String, position: Int) {
        val newFile = File(item.file.parent, newName)
        if (item.file.renameTo(newFile)) {
            mergeItems[position].file = newFile
            notifyItemChanged(position)
        } else {
            Toast.makeText(context, "Failed to rename file", Toast.LENGTH_SHORT).show()
        }
    }

//    fun setUsernameAndCaption(username: String, caption: String) {
//        this.username = username
//        this.caption = caption
//    }

    inner class DownloadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val playIcon: ImageView = itemView.findViewById(R.id.ivPlayIcon)
        private val textViewFileName: TextView = itemView.findViewById(R.id.textViewFileName)
        val ivMenuIcon: ImageView = itemView.findViewById(R.id.ivMenuIcon)
        private val textViewCaption: TextView = itemView.findViewById(R.id.textViewCaption)
        private val ivInstagramIcon: ImageView = itemView.findViewById(R.id.ivInstagramIcon)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        private val profileImage: ShapeableImageView = itemView.findViewById(R.id.ivProfile)

//        private lateinit var file: File

        @SuppressLint("SetTextI18n")
        fun bind(item: MergeDownloadItem) {
//            this.file = mergeItem.file

            checkBox.visibility = if (isSelectionModeEnabled) View.VISIBLE else View.GONE
            ivMenuIcon.visibility = if (isSelectionModeEnabled) View.GONE else View.VISIBLE
            ivInstagramIcon.visibility = if (isSelectionModeEnabled) View.GONE else View.VISIBLE

            checkBox.isChecked = selectedFiles.contains(item)

            itemView.setOnLongClickListener {
                if (isSelectionModeEnabled) return@setOnLongClickListener  false
                isSelectionModeEnabled = true
                toggleSelection(item) {
                    isAllSelected = mergeItems.size == selectedFiles.size
                }
                true
            }

            itemView.setOnClickListener {
                if (isSelectionModeEnabled) {
                    toggleSelection(item) {
                        isAllSelected = mergeItems.size == selectedFiles.size
                    }
                } else {
                    if (isVideoFile(item.file)) {
                        playVideo(itemView.context, adapterPosition, mergeItems.map { it.file })
                    } else {
                        showImage(itemView.context, adapterPosition, mergeItems.map { it.file })
                    }
                }
            }

            if (isVideoFile(item.file)) {
                playIcon.visibility = View.VISIBLE
                playIcon.contentDescription = "Play video"
                textViewFileName.text = item.username/*file.name*/
            } else {
                playIcon.visibility = View.GONE
                textViewFileName.text = item.username/*file.name*/
            }

            textViewCaption.text = item.caption/*"This is for the testing purpose"*/
            ivInstagramIcon.setOnClickListener {
                item.url.openInstagramPostInApp(itemView.context)
            }

            Glide.with(itemView.context)
                .load(item.profile)
                .into(profileImage)

            Glide.with(itemView.context)
                .load(item.file)
                .into(imageView)
        }
    }
}