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
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.hazel.instadownloader.R

class RenameDialogFragment : DialogFragment() {
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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rename_dialog, container, false)

        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE);
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            renameListener?.onRenameConfirmed(newName)
            dismiss()
        }
    }
}