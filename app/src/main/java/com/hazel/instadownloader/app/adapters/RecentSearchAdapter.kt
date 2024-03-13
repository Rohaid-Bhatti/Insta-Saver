package com.hazel.instadownloader.app.adapters

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.hazel.instadownloader.R
import com.hazel.instadownloader.core.database.RecentSearchItem


class RecentSearchAdapter(
    private val recentItems: List<RecentSearchItem>,
    private val onItemClick: (RecentSearchItem) -> Unit
    ) :
    RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recent_search_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = recentItems[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            onItemClick(item) // Call the click listener
        }
    }

    override fun getItemCount(): Int = recentItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvProfileRecentItem)
        private val ivProfile : ShapeableImageView = itemView.findViewById(R.id.ivProfileRecentItem)

        fun bind(recentItem: RecentSearchItem) {
            tvUsername.text = recentItem.username

            Glide.with(itemView.context)
                .load(recentItem.profile)
                .into(ivProfile)
        }
    }
}