package com.hazel.instadownloader.app.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hazel.instadownloader.features.browser.BrowserFragment
import com.hazel.instadownloader.features.download.DownloadFragment
import com.hazel.instadownloader.features.home.HomeFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity, var postUrl: String?, var navigateFun: () -> Unit) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment(postUrl, navigateFun)
            1 -> BrowserFragment()
            2 -> DownloadFragment()
            else -> HomeFragment(postUrl, navigateFun)
        }
    }
}