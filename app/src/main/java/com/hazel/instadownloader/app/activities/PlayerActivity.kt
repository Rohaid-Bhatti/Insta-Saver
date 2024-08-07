package com.hazel.instadownloader.app.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.hazel.instadownloader.R
import com.hazel.instadownloader.app.adapters.VideoPagerAdapter
import com.hazel.instadownloader.app.callbacks.ToolbarVisibilityListener
import com.hazel.instadownloader.core.extensions.isVideoFile
import com.hazel.instadownloader.core.extensions.shareFile
import com.hazel.instadownloader.core.extensions.shareFileToInstagram
import com.hazel.instadownloader.core.extensions.shareOnWhatsApp
import com.hazel.instadownloader.databinding.ActivityPlayerBinding
import com.hazel.instadownloader.features.dialogBox.DeleteConfirmationDialogFragment
import java.io.File


class PlayerActivity : AppCompatActivity(), ToolbarVisibilityListener {
    private var binding: ActivityPlayerBinding? = null
    private var videoPagerAdapter: VideoPagerAdapter? = null
    private var videoUris: List<String>? = null
    private var currentPosition : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        videoUris = intent.getStringArrayListExtra("videoUri") ?: emptyList()
        val position = intent.getIntExtra("positionVideo", 0)

        showVisibility()
        clickListener()

        videoPagerAdapter = VideoPagerAdapter(videoUris!!, this, this)
        binding?.videoPager?.adapter = videoPagerAdapter
        binding?.videoPager?.setCurrentItem(position, false)
    }

    private fun showVisibility() {
        binding?.toolbarVideo?.tvTitleToolbar?.text = ""
        binding?.toolbarVideo?.ivMenuBack?.setImageResource(R.drawable.ic_arrow_back)
        binding?.toolbarVideo?.ivHelpMenu?.visibility = View.GONE
        binding?.toolbarVideo?.ivPremiumMenu?.visibility = View.GONE
        binding?.toolbarVideo?.ivWhatsappMenu?.visibility = View.VISIBLE
        binding?.toolbarVideo?.ivShareMenu?.visibility = View.VISIBLE
        binding?.toolbarVideo?.ivRepostMenu?.visibility = View.VISIBLE
        binding?.toolbarVideo?.ivDeleteMenu?.visibility = View.VISIBLE
    }

    private fun clickListener() {
        binding?.toolbarVideo?.ivMenuBack?.setOnClickListener {
            finish()
        }

        binding?.toolbarVideo?.ivWhatsappMenu?.setOnClickListener {
            currentPosition = binding!!.videoPager.currentItem
            shareOnWhatsApp(this, File(Uri.parse(videoUris!![currentPosition!!]).path.toString()))
        }

        binding?.toolbarVideo?.ivShareMenu?.setOnClickListener {
            currentPosition = binding!!.videoPager.currentItem
            shareFile(this, File(Uri.parse(videoUris!![currentPosition!!]).path.toString()))
        }

        binding?.toolbarVideo?.ivRepostMenu?.setOnClickListener {
            currentPosition = binding!!.videoPager.currentItem
            val isVideo = isVideoFile(File(Uri.parse(videoUris!![currentPosition!!]).path.toString()))
            shareFileToInstagram(
                this,
                File(Uri.parse(videoUris!![currentPosition!!]).path.toString()),
                isVideo
            )
        }

        binding?.toolbarVideo?.ivDeleteMenu?.setOnClickListener {
            currentPosition = binding!!.videoPager.currentItem
            showDeleteConfirmationDialog(currentPosition!!)
        }
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
        val file = File(Uri.parse(videoUris!![position]).path!!)
        if (file.exists()) {
            file.delete()
            videoUris = videoUris?.filterIndexed { index, _ -> index != position }
            videoPagerAdapter?.notifyDataSetChanged()
        }
    }

    override fun onToolbarVisibilityChanged(isLock: Boolean) {
        if (!isLock) {
            binding?.toolbarVideo?.myToolbar?.let { slideUp(it) }
        } else {
            binding?.toolbarVideo?.myToolbar?.let { slideDown(it) }
        }
    }

    override fun swipeLock(swappable: Boolean) {
        binding?.videoPager?.isUserInputEnabled = swappable
    }

    private fun slideUp(view: View) {
        view.visibility = View.VISIBLE
        val animate = TranslateAnimation(
            0f,
            0f,
            0f,
            -view.height.toFloat()
        )
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    private fun slideDown(view: View) {
        val animate = TranslateAnimation(
            0f,
            0f,
            -view.height.toFloat(),
            0f
        )
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }
}