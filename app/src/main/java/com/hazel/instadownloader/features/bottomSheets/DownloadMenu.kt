package com.hazel.instadownloader.features.bottomSheets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hazel.instadownloader.R

class DownloadMenu : BottomSheetDialogFragment() {
    private var listener: OnOptionClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_download_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val closeIcon = view.findViewById<ImageView>(R.id.closeIcon)
        val viewOnInsta = view.findViewById<View>(R.id.layoutView)
//        val postUrl = "https://www.instagram.com/p/C2FSkkYsW7B/?chaining=true"
        val repostButton = view.findViewById<View>(R.id.layoutRepost)
        val shareButton = view.findViewById<View>(R.id.layoutShare)
        val whatsappButton = view.findViewById<View>(R.id.layoutWhatsapp)
        val renameButton = view.findViewById<View>(R.id.layoutRename)
        val deleteButton = view.findViewById<View>(R.id.layoutDelete)

        closeIcon.setOnClickListener {
            dismiss()
        }

        repostButton.setOnClickListener{
            listener?.onRepostInstagramClicked()
            dismiss()
        }

        shareButton.setOnClickListener {
            listener?.onShareClicked()
            dismiss()
        }

        whatsappButton.setOnClickListener {
            listener?.onShareWhatsAppClicked()
            dismiss()
        }

        renameButton.setOnClickListener {
            listener?.onRenameClicked()
            dismiss()
        }

        deleteButton.setOnClickListener {
            listener?.onDeleteClicked()
            dismiss()
        }

        viewOnInsta.setOnClickListener {
            listener?.onPostOpenInstagram()
            dismiss()
        }
    }

    interface OnOptionClickListener {
        fun onRepostInstagramClicked()
        fun onShareClicked()
        fun onShareWhatsAppClicked()
        fun onRenameClicked()
        fun onDeleteClicked()
        fun onPostOpenInstagram()
    }

    fun setOnOptionClickListener(listener: OnOptionClickListener) {
        this.listener = listener
    }
}