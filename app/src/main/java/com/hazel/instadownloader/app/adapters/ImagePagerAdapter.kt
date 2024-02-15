package com.hazel.instadownloader.app.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.hazel.instadownloader.databinding.ItemImageBinding

class ImagePagerAdapter(private val imageUris: List<String>, private val context: Context) :
    PagerAdapter() {

    override fun getCount(): Int = imageUris.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(context), container, false)
        Glide.with(context)
            .load(Uri.parse(imageUris[position]))
            .into(binding.imageView)
        container.addView(binding.root)
        return binding.root
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}