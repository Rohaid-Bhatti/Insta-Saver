package com.hazel.instadownloader.features.download.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.hazel.instadownloader.R
import com.hazel.instadownloader.core.extensions.isVideoFile
import com.hazel.instadownloader.core.extensions.openInstagramPostInApp
import com.hazel.instadownloader.core.extensions.playVideo
import com.hazel.instadownloader.core.extensions.showImage
import com.hazel.instadownloader.features.download.model.MergeDownloadItem

class DownloadAdapter(
    private val mergeItems: List<MergeDownloadItem>,
    private var deleteFileCallback: DeleteFileCallback
) : RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder>() {

    val selectedFiles = HashSet<MergeDownloadItem>()
    var isSelectionModeEnabled = false
    private var selectionModeListener: SelectionModeListener? = null
    var isAllSelected = false

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
            deleteFileCallback.onShowingMenu(mergeItem, position, selectedFiles)
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
        deleteFileCallback.onDeleteFileSelected(true)
    }

    override fun getItemCount(): Int = mergeItems.size

    inner class DownloadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val playIcon: ImageView = itemView.findViewById(R.id.ivPlayIcon)
        private val textViewFileName: TextView = itemView.findViewById(R.id.textViewFileName)
        val ivMenuIcon: ImageView = itemView.findViewById(R.id.ivMenuIcon)
        private val textViewCaption: TextView = itemView.findViewById(R.id.textViewCaption)
        private val ivInstagramIcon: ImageView = itemView.findViewById(R.id.ivInstagramIcon)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        private val profileImage: ShapeableImageView = itemView.findViewById(R.id.ivProfile)

        @SuppressLint("SetTextI18n")
        fun bind(item: MergeDownloadItem) {

            checkBox.visibility = if (isSelectionModeEnabled) View.VISIBLE else View.GONE
            ivMenuIcon.visibility = if (isSelectionModeEnabled) View.GONE else View.VISIBLE
            ivInstagramIcon.visibility = if (isSelectionModeEnabled) View.GONE else View.VISIBLE

            checkBox.isChecked = selectedFiles.contains(item)

            itemView.setOnLongClickListener {
                if (isSelectionModeEnabled) return@setOnLongClickListener false
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