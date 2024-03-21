package com.hazel.instadownloader.features.bottomSheets

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.hazel.instadownloader.R
import com.willy.ratingbar.ScaleRatingBar


class RateUsBottomSheetFragment : BottomSheetDialogFragment() {
    private var closeIcon: ImageView? = null
    private var btnRate: MaterialButton? = null
    private var ratingBar: ScaleRatingBar? = null
    private var ratingEmoji: ImageView? = null
    private var experienceWithApp: TextView? = null
    private var desc: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rate_us_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closeIcon = view.findViewById(R.id.crossIcons)
        btnRate = view.findViewById(R.id.btnRateUs)
        ratingBar = view.findViewById(R.id.rBar)
        ratingEmoji = view.findViewById(R.id.ratingEmoji)
        experienceWithApp = view.findViewById(R.id.tvRateExperience)
        desc = view.findViewById(R.id.tvRateDesc)

        animateRatingBar()

        /*btnRate.setOnClickListener {
            isClickable = true
            ratingBar.rating = 1F
            ratingBar.setMinimumStars(1F)
            desc.text = getString(R.string.rate_experience)
            btnRate.text = getString(R.string.feedback)
            btnRate.setBackgroundColor(resources.getColor(R.color.titleColor))
            ratingBar.isClickable = true
            ratingBar.isScrollable = true
        }

        if (isClickable) {
            ratingBar.isClickable = true
            ratingBar.isScrollable = true
        } else {
            ratingBar.isClickable = false
            ratingBar.isScrollable = false
        }*/

        closeIcon?.setOnClickListener {
            dismiss()
        }

        ratingBar?.setOnRatingChangeListener { _, rating, _ ->
            when (rating) {
                in 0.0..1.0 -> {
                    settingViews(
                        R.drawable.very_poor_review,
                        R.string.very_poor,
                        R.string.rate_experience,
                        R.string.feedback,
                        R.color.titleColor,
                        null
                    )
                }

                in 1.1..2.0 -> {
                    settingViews(
                        R.drawable.poor_review,
                        R.string.poor,
                        R.string.rate_experience,
                        R.string.feedback,
                        R.color.titleColor,
                        null
                    )
                }

                in 2.1..3.0 -> {
                    settingViews(
                        R.drawable.great_review,
                        R.string.normal,
                        R.string.rate_experience,
                        R.string.feedback,
                        R.color.titleColor,
                        null
                    )
                }

                in 3.1..4.0 -> {
                    settingViews(
                        R.drawable.normal_review,
                        R.string.great,
                        R.string.rate_experience,
                        R.string.feedback,
                        R.color.titleColor,
                        null
                    )
                }

                else -> {
                    settingViews(
                        R.drawable.heart_review,
                        R.string.love_it,
                        R.string.rate_experience ,
                        R.string.rate_us_google_play,
                        R.color.titleColor,
                        R.drawable.play_store_icon
                    )
                }
            }

        }
    }

    private fun settingViews(
        emoji: Int,
        experience: Int,
        description: Int,
        buttonText: Int,
        buttonColor: Int,
        buttonIcon: Int?
    ) {
        ratingEmoji?.setImageResource(emoji)
        experienceWithApp?.text = getString(experience)
        desc?.text = getString(description)
        btnRate?.text = getString(buttonText)
        activity?.let { ContextCompat.getColor(it, buttonColor) }
            ?.let { btnRate?.setBackgroundColor(it) }
        btnRate?.icon = activity?.let { buttonIcon?.let { it1 ->
            ContextCompat.getDrawable(it,
                it1
            )
        } }
    }

    private fun animateRatingBar() {
        val maxRating = 5.0f
        val initialRating = 0.0f
        val duration = 3000L

        // Create a ValueAnimator to animate the rating
        val animator = ValueAnimator.ofFloat(initialRating, maxRating)
        animator.duration = duration
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            Log.d("TESTING_RATING", "animateRatingBar: $animatedValue")
            ratingBar?.rating = animatedValue
        }

        // Start the animator
        animator.start()
    }
}