package com.hazel.instadownloader.app.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.SyncStateContract.Constants
import android.util.Log
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import com.hazel.instadownloader.MainActivity
import com.hazel.instadownloader.R
import com.hazel.instadownloader.app.utils.DataStores
import com.hazel.instadownloader.app.utils.switch
import com.hazel.instadownloader.core.database.DownloadedUrlViewModel
import com.hazel.instadownloader.databinding.ActivitySplashBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val downloadViewModel: DownloadedUrlViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pbLinearProgress.isIndeterminate = true

        CoroutineScope(Dispatchers.IO).launch {
            switch= DataStores.isAutoDownloadShown(this@SplashActivity).first()
            Log.d("TESTING_AUTO", "checking in the splash: $switch")
           // downloadViewModel.setAutoDownloadBoolean(check)
        }


        /*window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )*/

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}