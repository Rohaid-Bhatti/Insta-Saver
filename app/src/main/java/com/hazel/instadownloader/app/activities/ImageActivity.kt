package com.hazel.instadownloader.app.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.viewpager2.widget.ViewPager2
import com.hazel.instadownloader.app.adapters.ImagePagerAdapter
import com.hazel.instadownloader.core.extensions.shareFile
import com.hazel.instadownloader.databinding.ActivityImageBinding
import com.hazel.instadownloader.features.dialogBox.DeleteConfirmationDialogFragment
import java.io.File

class ImageActivity : AppCompatActivity() {
    private var imagePagerAdapter: ImagePagerAdapter? = null
    private lateinit var binding: ActivityImageBinding
    private lateinit var imageUris: MutableList<String>
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imageUris = intent.getStringArrayListExtra("imageUris")!!
        val position = intent.getIntExtra("position", 0)

        viewPager = binding.viewPager
        imagePagerAdapter = ImagePagerAdapter(imageUris, this)
        viewPager.adapter = imagePagerAdapter
        viewPager.setCurrentItem(position, false)

        binding.detailsButton.setOnClickListener {
            showImageDetails(viewPager.currentItem)
        }

        binding.shareButton.setOnClickListener {
            shareFile(this, File(Uri.parse(imageUris[viewPager.currentItem]).path.toString()))
        }

        binding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(viewPager.currentItem)
        }
    }

    private fun showImageDetails(position: Int) {
        val file = File(Uri.parse(imageUris[position]).path!!)
        val details = "Name: ${file.name}\nSize: ${file.length()} bytes"
        AlertDialog.Builder(this)
            .setTitle("Image Details")
            .setMessage(details)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val dialog = DeleteConfirmationDialogFragment("1") {
            deleteImage(position)
        }
        dialog.show(
            (this as AppCompatActivity).supportFragmentManager,
            "DeleteConfirmationDialog"
        )
    }

    private fun deleteImage(position: Int) {
        val file = File(Uri.parse(imageUris[position]).path!!)
        if (file.exists()) {
            file.delete()
            viewPager.currentItem = position-1
            imageUris.removeAt(position)
            imagePagerAdapter?.deleteItem(imageUris)
            imagePagerAdapter?.notifyDataSetChanged()
        }
    }
}