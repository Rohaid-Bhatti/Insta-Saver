package com.hazel.instadownloader.app.activities

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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

        binding?.toolbarVideo?.ivMenuBack?.setImageResource(R.drawable.ic_arrow_back)
        binding?.toolbarVideo?.ivMenuBack?.setOnClickListener {
            finish()
        }
        binding?.toolbarVideo?.ivHelpMenu?.visibility = View.GONE
        binding?.toolbarVideo?.ivPremiumMenu?.visibility = View.GONE
        binding?.toolbarVideo?.ivWhatsappMenu?.visibility = View.VISIBLE
        binding?.toolbarVideo?.ivShareMenu?.visibility = View.VISIBLE
        binding?.toolbarVideo?.ivRepostMenu?.visibility = View.VISIBLE
        binding?.toolbarVideo?.ivDeleteMenu?.visibility = View.VISIBLE

        binding?.toolbarVideo?.ivWhatsappMenu?.setOnClickListener {
            val currentPosition = binding!!.videoPager.currentItem
            shareOnWhatsApp(this, File(Uri.parse(videoUris[currentPosition]).path.toString()))
        }
        binding?.toolbarVideo?.ivShareMenu?.setOnClickListener {
            val currentPosition = binding!!.videoPager.currentItem
            shareFile(this, File(Uri.parse(videoUris[currentPosition]).path.toString()))
        }
        binding?.toolbarVideo?.ivRepostMenu?.setOnClickListener {
            val currentPosition = binding!!.videoPager.currentItem
            val isVideo = isVideoFile(File(Uri.parse(videoUris[currentPosition]).path.toString()))
            shareFileToInstagram(this, File(Uri.parse(videoUris[currentPosition]).path.toString()), isVideo)
        }
        binding?.toolbarVideo?.ivDeleteMenu?.setOnClickListener {
            val currentPosition = binding!!.videoPager.currentItem
            showDeleteConfirmationDialog(currentPosition)
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
}