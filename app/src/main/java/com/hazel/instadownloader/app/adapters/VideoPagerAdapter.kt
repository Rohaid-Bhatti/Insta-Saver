package com.hazel.instadownloader.app.adapters

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.hazel.instadownloader.R
import com.hazel.instadownloader.app.callbacks.ToolbarVisibilityListener
import java.util.Locale

class VideoPagerAdapter(
    private val videoUris: List<String>,
    private val context: Context,
    private val toolbarListener: ToolbarVisibilityListener
) :
    RecyclerView.Adapter<VideoPagerAdapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoUris[position])
    }

    override fun getItemCount(): Int = videoUris.size

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val simpleVideoView: VideoView = itemView.findViewById(R.id.video_view)
        private val btnSkipBackward: ImageButton = itemView.findViewById(R.id.btn_skip_backward)
        private val btnSkipForward: ImageButton = itemView.findViewById(R.id.btn_skip_forward)
        private val btnPlayPause: ImageButton = itemView.findViewById(R.id.btn_play_pause)
        private val btnRotate: ImageButton = itemView.findViewById(R.id.btn_rotate)
        private val btnLock: ImageButton = itemView.findViewById(R.id.btn_lock)
        private val btnUnlock: ImageButton = itemView.findViewById(R.id.btn_unlock)
        private val seekBar: SeekBar = itemView.findViewById(R.id.seek_bar)
        private val controlsLayout: View = itemView.findViewById(R.id.layoutControls)
        private val tvTimer: TextView = itemView.findViewById(R.id.tv_timer)
        private val tvTimerTotal: TextView = itemView.findViewById(R.id.tv_timer_total)
        private var isTracking = false
        private val controlFadeDelayMillis = 3000L
        private var controlFadeHandler = Handler(Looper.getMainLooper())
        private var controlFadeRunnable = Runnable { hideControls() }
        private var controlsVisible = true
        private var isUnlockEnable = true

        init {
            changeBarListener()

            btnPlayPause.setOnClickListener {
                playPauseFun()
            }

            btnRotate.setOnClickListener {
                toggleOrientation()
            }

            btnLock.setOnClickListener {
                toggleLockState()
                isUnlockEnable = false
                toolbarListener.swipeLock(false)
            }

            btnUnlock.setOnClickListener {
                toggleUnlockState()
                isUnlockEnable = true
                toolbarListener.swipeLock(true)
            }

            btnSkipBackward.setOnClickListener {
                simpleVideoView.seekTo(simpleVideoView.currentPosition - 5000)
            }

            btnSkipForward.setOnClickListener {
                simpleVideoView.seekTo(simpleVideoView.currentPosition + 5000)
            }

            videoPreparedListener()

            startControlFadeOut()
        }

        private val timerRunnable = object : Runnable {
            override fun run() {
                if (!isTracking) {
                    seekBar.progress = simpleVideoView.currentPosition
                }
                updateTimer()
                Handler(Looper.getMainLooper()).postDelayed(this, 50)
            }
        }

        private fun updateTimer() {
            val totalDuration = simpleVideoView.duration
            val currentDuration = simpleVideoView.currentPosition

            val totalSeconds = totalDuration / 1000
            val currentSeconds = currentDuration / 1000

            val totalMinutes = totalSeconds / 60
            val currentMinutes = currentSeconds / 60

            val totalTimeStr =
                String.format(Locale.getDefault(), "%02d:%02d", totalMinutes, totalSeconds % 60)
            val currentTimeStr =
                String.format(Locale.getDefault(), "%02d:%02d", currentMinutes, currentSeconds % 60)

            tvTimer.text = currentTimeStr
            tvTimerTotal.text = totalTimeStr
        }

        private fun changeBarListener() {
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        simpleVideoView.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    isTracking = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    isTracking = false
                }
            })
        }

        private fun videoPreparedListener() {
            simpleVideoView.setOnClickListener {
                if (isUnlockEnable) {
                    toggleControlsVisibility()
                }
            }

            simpleVideoView.setOnCompletionListener {
                btnPlayPause.setImageResource(R.drawable.ic_play)
            }

            simpleVideoView.setOnPreparedListener {
                it.start()
                if (simpleVideoView.isPlaying) {
                    btnPlayPause.setImageResource(R.drawable.ic_pause)
                } else {
                    btnPlayPause.setImageResource(R.drawable.ic_play)
                }
                seekBar.max = simpleVideoView.duration
                updateTimer()
                Handler(Looper.getMainLooper()).postDelayed(timerRunnable, 50)
            }
        }

        private fun startControlFadeOut() {
            controlFadeHandler.removeCallbacks(controlFadeRunnable)
            controlFadeHandler.postDelayed(controlFadeRunnable, controlFadeDelayMillis)
        }

        private fun showControls() {
            controlsVisible = true
            slideUp(controlsLayout)
            toolbarListener.onToolbarVisibilityChanged(true)
//            startControlFadeOut()
        }

        private fun hideControls() {
            controlsVisible = false
            toolbarListener.onToolbarVisibilityChanged(false)
            slideDown(controlsLayout)
        }

        private fun toggleControlsVisibility() {
            if (controlsVisible) {
                hideControls()
            } else {
                showControls()
            }
        }

        private fun playPauseFun() {
            if (simpleVideoView.isPlaying) {
                simpleVideoView.pause()
                btnPlayPause.setImageResource(R.drawable.ic_play)
            } else {
                simpleVideoView.start()
                btnPlayPause.setImageResource(R.drawable.ic_pause)
            }
        }

        private fun toggleLockState() {

            slideDown(controlsLayout)
            btnUnlock.visibility = View.VISIBLE
            toolbarListener.onToolbarVisibilityChanged(false)
        }

        private fun toggleUnlockState() {
            slideUp(controlsLayout)
            btnUnlock.visibility = View.GONE
            toolbarListener.onToolbarVisibilityChanged(true)
        }

        private fun toggleOrientation() {
            val activity = context as Activity
            val newOrientation =
                if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            activity.requestedOrientation = newOrientation
        }

        private fun slideUp(view: View) {
            view.visibility = View.VISIBLE
            val animate = TranslateAnimation(
                0f,
                0f,
                view.height.toFloat(),
                0f
            )
            animate.duration = 500
            animate.fillAfter = true
            view.startAnimation(animate)
        }

        private fun slideDown(view: View) {
            val animate = TranslateAnimation(
                0f,
                0f,
                0f,
                view.height.toFloat()
            )
            animate.duration = 500
            animate.fillAfter = true
            view.startAnimation(animate)
        }

        fun bind(videoUri: String) {
            simpleVideoView.setVideoURI(Uri.parse(videoUri))
        }
    }
}