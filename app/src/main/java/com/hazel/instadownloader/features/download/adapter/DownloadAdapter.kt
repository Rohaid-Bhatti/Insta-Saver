package com.hazel.instadownloader.features.download.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hazel.instadownloader.R
import com.hazel.instadownloader.core.extensions.isVideoFile
import com.hazel.instadownloader.core.extensions.playVideo
import com.hazel.instadownloader.core.extensions.shareFile
import com.hazel.instadownloader.core.extensions.shareFileToInstagram
import com.hazel.instadownloader.core.extensions.shareOnWhatsApp
import com.hazel.instadownloader.core.extensions.showImage
import com.hazel.instadownloader.features.bottomSheets.DownloadMenu
import com.hazel.instadownloader.features.dialogBox.DeleteConfirmationDialogFragment
import java.io.File

class DownloadAdapter(
    private val files: ArrayList<File>,
    private val delCallback: (isDel: Boolean) -> Unit
) :
    RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder>() {

    val selectedFiles = HashSet<File>()
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
        return DownloadViewHolder(view, files)
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        val file = files[position]
        holder.bind(file)

        holder.ivMenuIcon.setOnClickListener {
            showBottomSheet(holder.itemView.context, files[position], position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun toggleSelection(file: File, onAllAdded: () -> Unit) {
        if (selectedFiles.contains(file)) {
            selectedFiles.remove(file)
            onAllAdded()
        } else {
            selectedFiles.add(file)
            onAllAdded()
        }

        selectionModeListener?.onSelectionModeEnabled(selectedFiles.isNotEmpty())
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAllItems(selectAll: Boolean) {
        if (selectAll) {
            selectedFiles.addAll(files)
        } else {
            selectedFiles.clear()
        }
        updateAllSelectedStatus()
        notifyDataSetChanged()
        selectionModeListener?.onSelectionModeEnabled(selectedFiles.isNotEmpty())
    }

    private fun updateAllSelectedStatus() {
        isAllSelected = selectedFiles.size == files.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteSelectedFiles() {
        selectedFiles.forEach { file ->
            file.delete()
        }
        selectedFiles.clear()
        isSelectionModeEnabled = false
        notifyDataSetChanged()
        delCallback(true)
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
        val dialog = DeleteConfirmationDialogFragment("1") {
            deleteFile(context, file)
        }
        dialog.show(
            (context as AppCompatActivity).supportFragmentManager,
            "DeleteConfirmationDialog"
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteFile(context: Context, file: File) {
        val contentUri = MediaStore.Files.getContentUri("external")
        val selection = "${MediaStore.Files.FileColumns.DATA} = ?"
        val selectionArgs = arrayOf(file.absolutePath)
        val rowsDeleted = context.contentResolver.delete(contentUri, selection, selectionArgs)
        if (rowsDeleted > 0) {
            delCallback(true)
            files.remove(file)
            notifyDataSetChanged()
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

    inner class DownloadViewHolder(itemView: View, private var files: List<File>) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val playIcon: ImageView = itemView.findViewById(R.id.ivPlayIcon)
        private val textViewFileName: TextView = itemView.findViewById(R.id.textViewFileName)
        val ivMenuIcon: ImageView = itemView.findViewById(R.id.ivMenuIcon)
        private val textViewCaption: TextView = itemView.findViewById(R.id.textViewCaption)
        private val ivInstagramIcon: ImageView = itemView.findViewById(R.id.ivInstagramIcon)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

        private lateinit var file: File

        init {
            itemView.setOnClickListener(this)
        }

        @SuppressLint("SetTextI18n")
        fun bind(file: File) {
            this.file = file

            checkBox.visibility = if (isSelectionModeEnabled) View.VISIBLE else View.GONE
            ivMenuIcon.visibility = if (isSelectionModeEnabled) View.GONE else View.VISIBLE
            ivInstagramIcon.visibility = if (isSelectionModeEnabled) View.GONE else View.VISIBLE

            checkBox.isChecked = selectedFiles.contains(file)

            itemView.setOnLongClickListener {
                isSelectionModeEnabled = true
                toggleSelection(file) {
                    isAllSelected = files.size == selectedFiles.size
                }
                true
            }

            if (isSelectionModeEnabled) {
                itemView.setOnClickListener {
                    toggleSelection(file) {
                       isAllSelected = files.size == selectedFiles.size
                    }
                }
            }

            if (isVideoFile(file)) {
                playIcon.visibility = View.VISIBLE
                playIcon.contentDescription = "Play video"
                textViewFileName.text = file.name
            } else {
                playIcon.visibility = View.GONE
                textViewFileName.text = file.name
            }

            textViewCaption.text = "This is for the testing purpose"
            ivInstagramIcon.setOnClickListener {
                Toast.makeText(
                    itemView.context,
                    "View on instagram functionality",
                    Toast.LENGTH_SHORT
                ).show()
            }

            Glide.with(itemView.context)
                .load(file)
                .into(imageView)
        }

        override fun onClick(v: View?) {
            if (isVideoFile(file)) {
                playVideo(itemView.context, adapterPosition, files)
            } else {
                showImage(itemView.context, adapterPosition, files)
            }
        }
    }
}