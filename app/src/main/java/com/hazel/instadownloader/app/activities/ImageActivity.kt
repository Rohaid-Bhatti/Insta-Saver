package com.hazel.instadownloader.app.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.viewpager.widget.ViewPager
import com.hazel.instadownloader.R
import com.hazel.instadownloader.app.adapters.ImagePagerAdapter
import com.hazel.instadownloader.databinding.ActivityImageBinding
import java.io.File

class ImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageBinding
    private lateinit var imageUris: List<String>
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imageUris = intent.getStringArrayListExtra("imageUris")!!
        val position = intent.getIntExtra("position", 0)

        // Set up ViewPager
        viewPager = binding.viewPager
        viewPager.adapter = ImagePagerAdapter(imageUris, this)
        viewPager.currentItem = position

        binding.detailsButton.setOnClickListener {
            showImageDetails(viewPager.currentItem)
        }

        binding.shareButton.setOnClickListener {
            shareImage(Uri.parse(imageUris[viewPager.currentItem]))
        }

        binding.deleteButton.setOnClickListener {
            confirmDeleteImage(viewPager.currentItem)
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

    private fun shareImage(imageUri: Uri) {
        val file = File(imageUri.path!!)
        val fileUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share Image"))
    }

    private fun confirmDeleteImage(position: Int) {
        File(Uri.parse(imageUris[position]).path!!)
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this image?")
            .setPositiveButton("Delete") { _, _ -> deleteImage(position) }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun deleteImage(position: Int) {
        val file = File(Uri.parse(imageUris[position]).path!!)
        if (file.exists()) {
            file.delete()
            // Remove the deleted image from the ViewPager and update the adapter
            imageUris = imageUris.toMutableList().apply { removeAt(position) }
            viewPager.adapter?.notifyDataSetChanged()
        }
    }
}