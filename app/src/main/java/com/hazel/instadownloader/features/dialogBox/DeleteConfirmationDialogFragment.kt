package com.hazel.instadownloader.features.dialogBox

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hazel.instadownloader.R

class DeleteConfirmationDialogFragment(private val fileSize: String, private val deleteListener: () -> Unit) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_delete_confirmation_dialog, container, false)

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

        val tvFileSize = view.findViewById<TextView>(R.id.tvFileSize)
        tvFileSize.text = "$fileSize files"

        val deleteButton = view.findViewById<Button>(R.id.btnDelete)
        deleteButton.setOnClickListener {
            deleteListener.invoke()
            dismiss()
        }
    }
}