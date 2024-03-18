package com.hazel.instadownloader.app.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.viewpager2.widget.ViewPager2
import com.hazel.instadownloader.R
import com.hazel.instadownloader.app.adapters.ImagePagerAdapter
import com.hazel.instadownloader.core.extensions.debounce
import com.hazel.instadownloader.core.extensions.isVideoFile
import com.hazel.instadownloader.core.extensions.shareFile
import com.hazel.instadownloader.core.extensions.shareFileToInstagram
import com.hazel.instadownloader.core.extensions.shareOnWhatsApp
import com.hazel.instadownloader.databinding.ActivityImageBinding
import com.hazel.instadownloader.features.dialogBox.DeleteConfirmationDialogFragment
import java.io.File

class ImageActivity : AppCompatActivity() {
    private var imagePagerAdapter: ImagePagerAdapter? = null
    private lateinit var binding: ActivityImageBinding
    private lateinit var imageUris: MutableList<String>
    private lateinit var viewPager: ViewPager2
    private var isVideo : Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarImage.ivMenuBack.setImageResource(R.drawable.ic_arrow_back)
        binding.toolbarImage.ivMenuBack.setOnClickListener {
            finish()
        }
        binding.toolbarImage.ivHelpMenu.visibility = View.GONE
        binding.toolbarImage.ivPremiumMenu.visibility = View.GONE
        binding.toolbarImage.ivWhatsappMenu.visibility = View.VISIBLE
        binding.toolbarImage.ivShareMenu.visibility = View.VISIBLE
        binding.toolbarImage.ivRepostMenu.visibility = View.VISIBLE
        binding.toolbarImage.ivDeleteMenu.visibility = View.VISIBLE

        binding.toolbarImage.ivWhatsappMenu.setOnClickListener {
            shareOnWhatsApp(this, File(Uri.parse(imageUris[viewPager.currentItem]).path.toString()))
        }
        binding.toolbarImage.ivShareMenu.setOnClickListener {
            shareFile(this, File(Uri.parse(imageUris[viewPager.currentItem]).path.toString()))
        }
        binding.toolbarImage.ivRepostMenu.setOnClickListener {
            shareFileToInstagram(this, File(Uri.parse(imageUris[viewPager.currentItem]).path.toString()), isVideo!!)
        }
        binding.toolbarImage.ivDeleteMenu.setOnClickListener {
            showDeleteConfirmationDialog(viewPager.currentItem)
        }

        imageUris = intent.getStringArrayListExtra("imageUris")!!
        val position = intent.getIntExtra("position", 0)

        viewPager = binding.viewPager
        imagePagerAdapter = ImagePagerAdapter(imageUris, this)
        viewPager.adapter = imagePagerAdapter
        viewPager.setCurrentItem(position, false)

        isVideo = isVideoFile(File(Uri.parse(imageUris[viewPager.currentItem]).path.toString()))

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