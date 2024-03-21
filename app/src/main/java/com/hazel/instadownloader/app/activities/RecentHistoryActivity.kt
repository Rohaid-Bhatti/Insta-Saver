package com.hazel.instadownloader.app.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.hazel.instadownloader.R
import com.hazel.instadownloader.app.adapters.RecentSearchAdapter
import com.hazel.instadownloader.core.database.DownloadedUrlViewModel
import com.hazel.instadownloader.core.extensions.openInstagramProfile
import com.hazel.instadownloader.databinding.ActivityRecentHistoryBinding
import com.hazel.instadownloader.features.dialogBox.ClearHistoryDialogFragment

class RecentHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecentHistoryBinding
    private val viewModel: DownloadedUrlViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.init(this)

        binding.appBar.ivMenuBack.setImageResource(R.drawable.ic_arrow_back)
        binding.appBar.ivMenuBack.setOnClickListener {
            finish()
        }

        binding.appBar.tvTitleToolbar.text = getString(R.string.recently_search)
        binding.appBar.ivHelpMenu.visibility = View.GONE
        binding.appBar.ivPremiumMenu.visibility = View.GONE

        viewModel.allSearchedItems?.observe(this) { recentItems ->
            if (recentItems.isNotEmpty()) {
                val sortedList = recentItems.sortedByDescending { it.id }
                val adapter = RecentSearchAdapter(sortedList) { item ->
                    openInstagramProfile(item, this)
                }
                binding.rvRecentSearchItemHistory.adapter = adapter
            } else {
                binding.rvRecentSearchItemHistory.visibility = View.GONE
                binding.tvNoHistory.visibility = View.VISIBLE
                binding.btnClear.visibility = View.GONE
            }
        }
        binding.rvRecentSearchItemHistory.layoutManager = GridLayoutManager(this, 4)

        val dialogClear = ClearHistoryDialogFragment {
            viewModel.clearRecentSearchHistory()

            viewModel.allSearchedItems?.observe(this) { recentItems ->
                if (recentItems.isNotEmpty()) {
                    val sortedList = recentItems.sortedByDescending { it.id }
                    val adapter = RecentSearchAdapter(sortedList) { item ->
                        openInstagramProfile(item, this)
                    }
                    binding.rvRecentSearchItemHistory.adapter = adapter
                } else {
                    binding.rvRecentSearchItemHistory.visibility = View.GONE
                    binding.tvNoHistory.visibility = View.VISIBLE
                    binding.btnClear.visibility = View.GONE
                }
            }
        }

        binding.btnClear.setOnClickListener {
            dialogClear.show(supportFragmentManager, "ClearHistoryDialog")
        }
    }
}