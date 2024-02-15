package com.hazel.instadownloader.app.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.viewpager2.widget.ViewPager2
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

        /*binding?.videoPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                videoPagerAdapter?.simpleVideoView?.start()
            }
        })*/
    }
}