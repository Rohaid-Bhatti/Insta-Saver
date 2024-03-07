package com.hazel.instadownloader.app.activities

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.hazel.instadownloader.R
import com.hazel.instadownloader.app.adapters.VideoPagerAdapter
import com.hazel.instadownloader.core.extensions.isVideoFile
import com.hazel.instadownloader.core.extensions.shareFile
import com.hazel.instadownloader.core.extensions.shareFileToInstagram
import com.hazel.instadownloader.core.extensions.shareOnWhatsApp
import com.hazel.instadownloader.databinding.ActivityPlayerBinding
import com.hazel.instadownloader.features.dialogBox.DeleteConfirmationDialogFragment
import java.io.File


class PlayerActivity : AppCompatActivity() {
    private var binding: ActivityPlayerBinding? = null
    private  var videoPagerAdapter: VideoPagerAdapter?=null
    private lateinit var videoUris: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.toolbarVideo?.myToolbar?.title = ""
        setSupportActionBar(binding?.toolbarVideo?.myToolbar)
        setSupportActionBar(binding?.toolbarVideo?.root)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        videoUris = intent.getStringArrayListExtra("videoUri") ?: emptyList()
        val position = intent.getIntExtra("positionVideo", 0)

        videoPagerAdapter = VideoPagerAdapter(videoUris, this)
        binding?.videoPager?.adapter = videoPagerAdapter
        binding?.videoPager?.setCurrentItem(position, false)
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val dialog = DeleteConfirmationDialogFragment("1") {
            deleteVideo(position)
        }
        dialog.show(
            supportFragmentManager,
            "DeleteConfirmationDialog"
        )
    }

    private fun deleteVideo(position: Int) {
        val file = File(Uri.parse(videoUris[position]).path!!)
        if (file.exists()) {
            file.delete()
            videoUris = videoUris.filterIndexed { index, _ -> index != position }
            videoPagerAdapter?.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.image_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
            R.id.whatsAppIcon -> {
                val currentPosition = binding!!.videoPager.currentItem
                shareOnWhatsApp(this, File(Uri.parse(videoUris[currentPosition]).path.toString()))
                return true
            }
            R.id.shareIcon -> {
                val currentPosition = binding!!.videoPager.currentItem
                shareFile(this, File(Uri.parse(videoUris[currentPosition]).path.toString()))
                return true
            }
            R.id.repostIcon -> {
                val currentPosition = binding!!.videoPager.currentItem
                val isVideo = isVideoFile(File(Uri.parse(videoUris[currentPosition]).path.toString()))
                shareFileToInstagram(this, File(Uri.parse(videoUris[currentPosition]).path.toString()), isVideo)
                return true
            }
            R.id.deleteIcon -> {
                val currentPosition = binding!!.videoPager.currentItem
                showDeleteConfirmationDialog(currentPosition)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}