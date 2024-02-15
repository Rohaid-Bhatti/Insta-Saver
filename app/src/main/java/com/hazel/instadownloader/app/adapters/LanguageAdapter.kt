package com.hazel.instadownloader.app.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.checkbox.MaterialCheckBox
import com.hazel.instadownloader.R
import com.hazel.instadownloader.app.models.LanguageModel
import com.hazel.instadownloader.databinding.LanguageItemBinding

class LanguageAdapter(
    private var languages: ArrayList<LanguageModel>,
    private val context: Context,
    private val currentLang: String,
    private val onItemSelect: (String, String) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    private var index: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LanguageItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val language = languages[position]

        with(holder.binding) {
            languageName.text = language.languageName
            Glide.with(context)
                .load(language.languageIcon)
                .error(R.drawable.help_toolbar)
                .dontAnimate()
                .circleCrop()
                .into(flagIcon)

            // Handle selection logic
            if (index == -1) {
                if (currentLang == language.languageCode) {
                    languageName.setTextColor(ContextCompat.getColor(context, R.color.titleColor))
                    checkBox.setImageResource(R.drawable.tick_icon)
                } else {
                    languageName.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.subtitleColor
                        )
                    )
                    checkBox.setImageResource(R.drawable.non_tick_icon)
                }
            } else {
                if (index == position) {
                    languageName.setTextColor(ContextCompat.getColor(context, R.color.titleColor))
                    checkBox.setImageResource(R.drawable.tick_icon)
                } else {
                    languageName.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.subtitleColor
                        )
                    )
                    checkBox.setImageResource(R.drawable.non_tick_icon)
                }
            }
        }

        holder.binding.root.setOnClickListener {
            index = position
            onItemSelect.invoke(language.languageCode, language.languageName)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = languages.size

    inner class ViewHolder(val binding: LanguageItemBinding) : RecyclerView.ViewHolder(binding.root)
}