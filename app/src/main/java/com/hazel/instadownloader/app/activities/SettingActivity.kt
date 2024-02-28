package com.hazel.instadownloader.app.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.hazel.instadownloader.R
import com.hazel.instadownloader.core.extensions.debounce
import com.hazel.instadownloader.core.extensions.shareApp
import com.hazel.instadownloader.databinding.ActivitySettingBinding
import com.hazel.instadownloader.features.bottomSheets.HelpFragment

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBar.myToolbar)
        setSupportActionBar(binding.appBar.root)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        binding.appBar.myToolbar.title = getString(R.string.setting_title)

        binding.layoutLanguage.setOnClickListener{
            val intent = Intent(this, LanguageActivity::class.java)
            startActivity(intent)
        }

        binding.layoutHelp.setOnClickListener {
            val bottomSheetFragment = HelpFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.layoutShare.setOnClickListener {
            debounce(binding.root) {
                shareApp()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}