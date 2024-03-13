package com.hazel.instadownloader.features.dialogBox

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.hazel.instadownloader.R
import com.hazel.instadownloader.core.database.DownloadedUrlViewModel
import com.hazel.instadownloader.features.download.model.MergeDownloadItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class RenameDialogFragment() : DialogFragment() {
    private var renameListener: RenameListener? = null

    interface RenameListener {
        fun onRenameConfirmed(newName: String)
    }

    fun setRenameListener(listener: RenameListener) {
        renameListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rename_dialog, container, false)

        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val closeIcon = view.findViewById<ImageView>(R.id.closeIcon)
        closeIcon.setOnClickListener {
            dismiss()
        }

        val saveButton = view.findViewById<Button>(R.id.btnSave)
        val etRename = view.findViewById<EditText>(R.id.etRename)
        val fileName = arguments?.getString("fileName")
        etRename.setText(fileName)

        saveButton.setOnClickListener {
            val newName = etRename.text.toString().trim()
//            renameListener?.onRenameConfirmed(newName)
//            dismiss()
            if (newName.isNotEmpty()) {
                val hasExtension = newName.contains('.')
                val newFileName = if (hasExtension) {
                    newName
                } else {
                    val fileType = getFileType(newName)
                    newName + if (fileType == FileType.VIDEO) ".jpg" else ".mp4"
                }
                renameListener?.onRenameConfirmed(newFileName)
                dismiss()
            } else {
                etRename.error = "Please enter a valid file name"
            }
        }

    }

    private fun getFileType(fileName: String): FileType {
        return if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg")) {
            FileType.IMAGE
        } else {
            FileType.VIDEO
        }
    }

    enum class FileType {
        IMAGE, VIDEO
    }
}