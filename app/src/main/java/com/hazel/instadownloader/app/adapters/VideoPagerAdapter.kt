package com.hazel.instadownloader.app.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.hazel.instadownloader.R

class VideoPagerAdapter(private val videoUris: List<String>, private val context: Context) :
    RecyclerView.Adapter<VideoPagerAdapter.VideoViewHolder>() {
    var mediaControls: MediaController? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoUris[position])
    }
    //hello
    override fun getItemCount(): Int = videoUris.size

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val simpleVideoView: VideoView = itemView.findViewById(R.id.simpleVideoView)

        init {
            mediaControls = MediaController(context)
            mediaControls?.setAnchorView(simpleVideoView)
        }

        fun bind(videoUri: String) {
            simpleVideoView.setMediaController(mediaControls)
            simpleVideoView.setVideoURI(Uri.parse(videoUri))
            simpleVideoView.requestFocus()
            simpleVideoView.start()
        }
    }
}