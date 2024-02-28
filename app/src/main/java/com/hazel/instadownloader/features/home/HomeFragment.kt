package com.hazel.instadownloader.features.home

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.hazel.instadownloader.R
import com.hazel.instadownloader.core.database.DownloadedUrl
import com.hazel.instadownloader.core.database.DownloadedUrlViewModel
import com.hazel.instadownloader.core.extensions.formatVideoDuration
import com.hazel.instadownloader.core.extensions.getVideoDuration
import com.hazel.instadownloader.core.extensions.isImageFile
import com.hazel.instadownloader.core.extensions.isVideoFile
import com.hazel.instadownloader.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var latestDownloadedMediaFile: File? = null
//    private var downloadedUrlViewModel: DownloadedUrlViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        downloadedUrlViewModel = ViewModelProvider(this)[DownloadedUrlViewModel::class.java]

        if (!Python.isStarted()) {
            activity?.let { AndroidPlatform(it) }?.let { Python.start(it) }
        }

        val py = Python.getInstance()
        val module = py.getModule("script")
        val downloader = module["download"]
        val posts = module["post_count"]
        val linkDownloader = module["download_post_from_link"]

        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                copyUrl()
                /*if (binding.etUrl.text?.isEmpty() == false) {
                    downloadFun(linkDownloader, posts, downloader)
                }*/
            }
        })

        binding.etUrl.onFocusChangeListener = OnFocusChangeListener { _, focused ->
            val keyboard =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (focused) keyboard.showSoftInput(
                binding.etUrl,
                0
            ) else keyboard.hideSoftInputFromWindow(
                binding.etUrl.windowToken,
                0
            )
        }

        binding.tvDownload.setOnClickListener {
//            fileName = generateFileNameFromUrl(url)
            downloadFun(linkDownloader, posts, downloader)
        }

    }

    private fun updateLatestDownloadedMediaFile() {
        latestDownloadedMediaFile =
            getLatestDownloadedFile("/storage/emulated/0/Download/InstaDownloader")

        Log.d("DOWNLOAD_MEDIA", "updateLatestDownloadedMediaFile: it is calling")
        latestDownloadedMediaFile?.let { file ->
            displayDownloadedMedia(file)
            binding.layoutVideo.visibility = View.VISIBLE
            binding.StatusText.visibility = View.GONE
        } ?: run {
            binding.layoutVideo.visibility = View.GONE
            binding.StatusText.visibility = View.VISIBLE
        }
    }

    private fun getLatestDownloadedFile(directoryPath: String): File? {
        val directory = File(directoryPath)
        val files = directory.listFiles()

        if (files.isNullOrEmpty()) {
            return null
        }

        val sortedFiles = files.sortedByDescending { it.lastModified() }
        return sortedFiles.firstOrNull()
    }

    private fun downloadFun(linkDownloader: PyObject?, posts: PyObject?, downloader: PyObject?) {
//        CoroutineScope(Dispatchers.IO).launch {
        try {
            if (binding.etUrl.text.toString() != "") {
                Toast.makeText(requireContext(), "Download Started", Toast.LENGTH_LONG).show()

                if (binding.etUrl.text.toString()
                        .startsWith("https://www.instagram.com/")
                ) { // checks if the text is a valid instagram link
                    // Post shortcode is a part of the Post URL, https://www.instagram.com/p/SHORTCODE/
                    val url = binding.etUrl.text.toString()
                    var postShortcode = ""
                    if (url.startsWith("https://www.instagram.com/p/")) {
                        postShortcode =
                            url.substringAfter("https://www.instagram.com/p/")
                                .substringBefore("/")
                    } else if (url.startsWith("https://www.instagram.com/reel/")) {
                        postShortcode = url.substringAfter("https://www.instagram.com/reel/")
                            .substringBefore("/")
                    }

                    Log.d("DOWNLOAD_LINK", "onCreate: $linkDownloader")

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            linkDownloader?.call(postShortcode)
                            requireActivity().runOnUiThread {
                                Toast.makeText(
                                    requireContext(),
                                    "Download Finished",
                                    Toast.LENGTH_LONG
                                ).show()
                                updateLatestDownloadedMediaFile()
                                //   val downloadedUrl = DownloadedUrl(url = url, fileName = "fileName")
                                //    downloadedUrlViewModel?.insertDownloadedUrl(downloadedUrl)
                                binding.StatusText.text = "Download Status: Finished"
                            }

                            Log.d("DOWNLOAD_LINK", "onCreate: $linkDownloader")

                        } catch (error: Throwable) {
                            activity?.runOnUiThread {
                                Toast.makeText(
                                    requireContext(),
                                    "Something went wrong",
                                    Toast.LENGTH_LONG
                                ).show()
                                val errorName = error.toString().split(":")
                                binding.StatusText.text = errorName.toString()
                            }
                        }
                    }
                } else { // if the text is not a link, it must  be an instagram username
                    try {
                        binding.StatusText.text =
                            "Found ${posts?.call(binding.etUrl.text.toString())} posts, Downloading..."
                    } catch (error: Throwable) {
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        val errorName = error.toString().split(":")
                        binding.StatusText.text = errorName[errorName.size - 1]
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            downloader?.call(binding.etUrl.text.toString())
                            activity?.runOnUiThread {
                                Toast.makeText(
                                    requireContext(),
                                    "Download Finished",
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.StatusText.text = "Download Status: Finished"
                            }
                        } catch (error: Throwable) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(
                                    requireContext(),
                                    "Something went wrong",
                                    Toast.LENGTH_LONG
                                ).show()
                                val errorName = error.toString().split(":")
                                binding.StatusText.text = errorName[errorName.size - 1]
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Empty Field", Toast.LENGTH_LONG).show()
            }
        } catch (error: Throwable) {
            Log.e("DOWNLOAD_FUNCTION", "downloadFun: ", error)
        }
        /* }*/
    }

    private fun displayDownloadedMedia(mediaUri: File) {
        val downloadItemView =
            LayoutInflater.from(requireContext()).inflate(R.layout.item_download, null)
        val imageView = downloadItemView.findViewById<ImageView>(R.id.imageView)
//        val playIconBg = downloadItemView.findViewById<LinearLayout>(R.id.playIconBg)
        val ivPlayIcon = downloadItemView.findViewById<ImageView>(R.id.ivPlayIcon)
//        val textViewDuration = downloadItemView.findViewById<TextView>(R.id.textViewDuration)
        val textViewFileName = downloadItemView.findViewById<TextView>(R.id.textViewFileName)
        val ivMenuIcon = downloadItemView.findViewById<ImageView>(R.id.ivMenuIcon)

        if (isImageFile(mediaUri)) {
            binding.layoutVideo.visibility = View.VISIBLE
            Glide.with(this)
                .load(mediaUri)
                .into(imageView)
            imageView.visibility = View.VISIBLE
//            playIconBg.visibility = View.GONE
            ivPlayIcon.visibility = View.GONE
//            textViewDuration.visibility = View.GONE
        } else if (isVideoFile(mediaUri)) {
            binding.layoutVideo.visibility = View.VISIBLE
            Glide.with(this)
                .load(mediaUri)
                .into(imageView)
            imageView.visibility = View.VISIBLE
//            playIconBg.visibility = View.VISIBLE
            ivPlayIcon.visibility = View.VISIBLE
//            textViewDuration.visibility = View.VISIBLE
//            val duration = getVideoDuration(mediaUri)
//            val formattedDuration = formatVideoDuration(duration)
//            textViewDuration.text = formattedDuration
        } else {
            binding.layoutVideo.visibility = View.GONE
        }

        textViewFileName.text = mediaUri.name
        binding.container.addView(downloadItemView)
    }

    private fun copyUrl() {
        val clipboard = (activity?.getSystemService(Context.CLIPBOARD_SERVICE)) as? ClipboardManager
        val textToPaste = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()

        if (!textToPaste.isNullOrEmpty() && textToPaste.startsWith("https://www.instagram.com/")) {
            binding.etUrl.setText(textToPaste)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (binding.etUrl.text.toString().trim().isEmpty()) {
            copyUrl()
        }
    }
}