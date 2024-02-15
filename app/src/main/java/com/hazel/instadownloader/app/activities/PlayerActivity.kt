package com.hazel.instadownloader.app.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hazel.instadownloader.R
import com.hazel.instadownloader.app.adapters.VideoPagerAdapter
import com.hazel.instadownloader.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private var binding: ActivityPlayerBinding? = null
    private  var videoPagerAdapter: VideoPagerAdapter?=null
    private lateinit var videoUris: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        videoUris = intent.getStringArrayListExtra("videoUri") ?: emptyList()
        val position = intent.getIntExtra("positionVideo", 0)

        videoPagerAdapter = VideoPagerAdapter(videoUris, this)
        binding?.videoPager?.adapter = videoPagerAdapter
        binding?.videoPager?.setCurrentItem(position, false)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}