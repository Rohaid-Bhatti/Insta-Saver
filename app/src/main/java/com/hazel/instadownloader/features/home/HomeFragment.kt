package com.hazel.instadownloader.features.home

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.imageview.ShapeableImageView
import com.hazel.instadownloader.R
import com.hazel.instadownloader.app.activities.RecentHistoryActivity
import com.hazel.instadownloader.app.adapters.RecentSearchAdapter
import com.hazel.instadownloader.app.utils.switch
import com.hazel.instadownloader.core.database.DownloadedItem
import com.hazel.instadownloader.core.database.DownloadedUrlViewModel
import com.hazel.instadownloader.core.database.RecentSearchItem
import com.hazel.instadownloader.core.extensions.debounce
import com.hazel.instadownloader.core.extensions.isImageFile
import com.hazel.instadownloader.core.extensions.isVideoFile
import com.hazel.instadownloader.core.extensions.openInstagramProfile
import com.hazel.instadownloader.core.extensions.playVideo
import com.hazel.instadownloader.core.extensions.shareFile
import com.hazel.instadownloader.core.extensions.shareFileToInstagram
import com.hazel.instadownloader.core.extensions.shareOnWhatsApp
import com.hazel.instadownloader.core.extensions.showImage
import com.hazel.instadownloader.databinding.FragmentHomeBinding
import com.hazel.instadownloader.features.bottomSheets.DownloadMenu
import com.hazel.instadownloader.features.dialogBox.DeleteConfirmationDialogFragment
import com.hazel.instadownloader.features.dialogBox.RenameDialogFragment
import com.hazel.instadownloader.features.dialogBox.VerifyingUrlDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar

class HomeFragment() : Fragment() {
    var postURL: String? = null
    var navigateFun : (() -> Unit)? = null

    constructor(postURL: String?, navigateFun: () -> Unit) : this() {
        this.postURL = postURL
        this.navigateFun = navigateFun
    }

    private var drawableStart: Drawable? = null
    private var linkDownloader: PyObject? = null
    private var posts: PyObject? = null
    private var downloader: PyObject? = null
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var cleanUsername: String? = null
    private var cleanCaption: String? = null
    private var cleanPostUrl: String? = null
    private var cleanProfilePic: String? = null
    private var cleanIsVideo: String? = null
    private var filePath: String? = null
    private val viewModel: DownloadedUrlViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialization()
        downloaderScrapper()
        setValues(view)

        binding.rvRecentSearchItem.layoutManager = GridLayoutManager(activity, 4)
        setListeners()
        setObserver()

        binding.tvRecentViewAll.setOnClickListener {
            val intent = Intent(activity, RecentHistoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initialization() {
        activity?.let { viewModel.init(it) }
    }

    private fun setValues(view: View) = binding.apply {
        if (postURL == null) {
            view.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    copyUrl()

                    if (etUrl.text?.isEmpty() == false && switch) {
                        downloadFun(linkDownloader, posts, downloader)
                    }
                }
            })
        } else {
            etUrl.setText(postURL)
            if (etUrl.text?.isEmpty() == false && switch) {
                downloadFun(linkDownloader, posts, downloader)
            }
        }
        drawableStart = activity?.let {
            ContextCompat.getDrawable(
                it,
                R.drawable.url_link_icon
            )
        }
        binding.etUrl.setCompoundDrawablesRelativeWithIntrinsicBounds(
            drawableStart, null, null, null
        )
    }

    private fun downloadButtonVisibilityController(isTurnOn: Boolean) = binding.apply {
        if (isTurnOn) {
            cvButtons.visibility = View.GONE
        } else {
            cvButtons.visibility = View.VISIBLE
        }
    }

    private fun downloaderScrapper() {
        if (!Python.isStarted()) {
            activity?.let { AndroidPlatform(it) }?.let { Python.start(it) }
        }

        val py = Python.getInstance()
        val module = py.getModule("script")
        downloader = module["download"]
        posts = module["post_count"]
        linkDownloader = module["download_post_from_link"]
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        binding.etUrl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hasText = !s.isNullOrEmpty() // Check if text is not null or empty
                val drawable = if (hasText) activity?.let {
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.ic_close
                    )
                } else null

                binding.etUrl.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    drawableStart, null, drawable, null
                )
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Adding click listener to the close icon drawable
        binding.etUrl.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.etUrl.compoundDrawablesRelative[2]
                if (drawableEnd != null && event.rawX >= (binding.etUrl.right - drawableEnd.bounds.width())) {
                    binding.etUrl.setText("")
                    return@setOnTouchListener true
                }
            }
            false
        }

        binding.tvPaste.setOnClickListener {
            copyUrl()
        }

        binding.etUrl.onFocusChangeListener = OnFocusChangeListener { _, focused ->
            val keyboard =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (focused) keyboard.showSoftInput(
                binding.etUrl, 0
            ) else keyboard.hideSoftInputFromWindow(
                binding.etUrl.windowToken, 0
            )
        }

        binding.tvDownload.setOnClickListener {
            val url = binding.etUrl.text.toString()

            val verifyingDialog = VerifyingUrlDialogFragment()
            verifyingDialog.show(childFragmentManager, "VerifyingUrlDialog")

            lifecycleScope.launch(Dispatchers.Main) {
                delay(2000)

                viewModel.checkIfUrlExists(url) { urlExists ->
                    verifyingDialog.dismiss()
                    if (urlExists != null) {
                        Toast.makeText(activity, "Item is already downloaded", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        downloadFun(linkDownloader, posts, downloader)
                    }
                }
            }
        }

        binding.tvViewDownloads.setOnClickListener {
//            findNavController().navigate(R.id.downloadFragment)
            navigateFun?.invoke()
        }
    }

    private fun setObserver() {

        downloadButtonVisibilityController(switch)
        if (switch != viewModel.autoDownloadingLiveData.value) {
            viewModel.setAutoDownloadBoolean(switch)
        } else {
            viewModel.autoDownloadingLiveData.observe(viewLifecycleOwner) { isTurnOn ->
                downloadButtonVisibilityController(isTurnOn)
            }
        }

        viewModel.allSearchedItems?.observe(viewLifecycleOwner) { recentItems ->
            if (recentItems.isNotEmpty()) {
                binding.layoutRecentSearch.visibility = View.VISIBLE
                val sortedList = recentItems.sortedByDescending { it.id }.take(8)
                val adapter = RecentSearchAdapter(sortedList) { item ->
                    activity?.let { openInstagramProfile(item, it) }
                }
                binding.rvRecentSearchItem.adapter = adapter
            } else {
                binding.layoutRecentSearch.visibility = View.GONE
            }
        }
    }

    private fun updateLatestDownloadedMediaFile() {
        viewModel.allDownloadedItems?.observe(viewLifecycleOwner) { downloadedItems ->
            if (downloadedItems.isNotEmpty()) {
                displayDownloadedMedia(downloadedItems)
                binding.container.visibility = View.VISIBLE
                binding.tvRecentDownloads.visibility = View.VISIBLE
                binding.tvViewDownloads.visibility = View.VISIBLE
                binding.StatusText.visibility = View.GONE
            } else {
                binding.container.visibility = View.GONE
                binding.tvRecentDownloads.visibility = View.GONE
                binding.tvViewDownloads.visibility = View.GONE
                binding.StatusText.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun downloadFun(linkDownloader: PyObject?, posts: PyObject?, downloader: PyObject?) {
        try {
            if (binding.etUrl.text.toString() != "") {
                Toast.makeText(requireContext(), "Download Started", Toast.LENGTH_LONG).show()
                binding.viewLayoutBg.visibility = View.VISIBLE
                binding.animationView.visibility = View.VISIBLE
                binding.tvRecentDownloads.visibility = View.VISIBLE
                binding.tvViewDownloads.visibility = View.VISIBLE

                if (binding.etUrl.text.toString().startsWith("https://www.instagram.com/")) {
                    val url = binding.etUrl.text.toString()
                    var postShortcode = ""
                    if (url.startsWith("https://www.instagram.com/p/")) {
                        postShortcode =
                            url.substringAfter("https://www.instagram.com/p/").substringBefore("/")
                    } else if (url.startsWith("https://www.instagram.com/reel/")) {
                        postShortcode = url.substringAfter("https://www.instagram.com/reel/")
                            .substringBefore("/")
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val date = Calendar.getInstance().time
                            val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
                            val formattedDate = formatter.format(date)

                            val testing = linkDownloader?.call(postShortcode, formattedDate)

                            val resultString = testing?.toString()
                            Log.d("TESTING_AUTO", "list: $resultString")

                            val (caption, username, postUrl, profile, isVideo) = resultString!!.split(
                                "', '", "', "
                            )
                            cleanUsername = username.replace("'", "")
                            cleanCaption =
                                caption.replace("(", "").replace("'", "").replace("\\n", "")
                            cleanPostUrl = postUrl.replace("'", "")
                            cleanProfilePic = profile.replace("'", "")
                            cleanIsVideo = isVideo.replace(")", "")


                            Log.d("TESTING_AUTO", "downloadFun: $isVideo")
                            filePath = if (cleanIsVideo == "False") {
                                "/storage/emulated/0/Download/InstaDownloader/$formattedDate.jpg"
                            } else {
                                "/storage/emulated/0/Download/InstaDownloader/$formattedDate.mp4"
                            }

                            val downloadedItem = DownloadedItem(
                                url = binding.etUrl.text.toString(),
                                fileName = formattedDate,
                                postUrl = cleanPostUrl ?: "",
                                caption = cleanCaption ?: "",
                                username = cleanUsername ?: "",
                                profile = cleanProfilePic ?: "",
                                filePath = filePath ?: ""
                            )

                            val searchItem = RecentSearchItem(
                                fileName = formattedDate,
                                username = cleanUsername ?: "",
                                profile = cleanProfilePic ?: ""
                            )

                            viewModel.insertDownloadedItem(downloadedItem)

                            val itemCount = viewModel.getCountOfSearchItems()
                            if (itemCount >= 20) {
                                viewModel.deleteOldestSearchItems(itemCount - 7)
                            }

                            viewModel.insertOrUpdateRecentItem(searchItem)

                            requireActivity().runOnUiThread {
                                binding.viewLayoutBg.visibility = View.GONE
                                binding.animationView.visibility = View.GONE
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.download_finished), Toast.LENGTH_LONG
                                ).show()
                                updateLatestDownloadedMediaFile()
                                binding.StatusText.text =
                                    getString(R.string.download_status_finished)
                            }

                        } catch (error: Throwable) {
                            activity?.runOnUiThread {
                                binding.viewLayoutBg.visibility = View.GONE
                                binding.animationView.visibility = View.GONE
                                Toast.makeText(
                                    requireContext(), "Something went wrong", Toast.LENGTH_LONG
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
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_LONG
                        ).show()
                        val errorName = error.toString().split(":")
                        binding.StatusText.text = errorName[errorName.size - 1]
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            downloader?.call(binding.etUrl.text.toString())
                            activity?.runOnUiThread {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.download_finished),
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.StatusText.text =
                                    getString(R.string.download_status_finished)
                            }
                        } catch (error: Throwable) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.something_went_wrong), Toast.LENGTH_LONG
                                ).show()
                                val errorName = error.toString().split(":")
                                binding.StatusText.text = errorName[errorName.size - 1]
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.empty_field), Toast.LENGTH_LONG)
                    .show()
            }
        } catch (error: Throwable) {
            Log.e("DOWNLOAD_FUNCTION", "downloadFun: ", error)
        }
    }

    private fun displayDownloadedMedia(mediaUri: List<DownloadedItem>) {
        val latestDownloadedItem = mediaUri.lastOrNull()

        latestDownloadedItem?.let { downloadedItem ->
            val downloadItemView =
                LayoutInflater.from(requireContext()).inflate(R.layout.recently_download_view, null)
            val ivThumbnail = downloadItemView.findViewById<ImageView>(R.id.ivThumbnail)
            val ivRecentPlayIcon = downloadItemView.findViewById<ImageView>(R.id.ivRecentPlayIcon)
            val tvRecentName = downloadItemView.findViewById<TextView>(R.id.tvRecentName)
            val ivRecentMenu = downloadItemView.findViewById<ImageView>(R.id.ivRecentMenu)
            val ivProfile = downloadItemView.findViewById<ShapeableImageView>(R.id.ivProfileRecent)
            val ivRecentShare = downloadItemView.findViewById<ImageView>(R.id.ivRecentShare)
            val isVideo = isVideoFile(File(downloadedItem.filePath))

            if (isImageFile(File(downloadedItem.filePath))) {
                binding.container.visibility = View.VISIBLE
                binding.tvRecentDownloads.visibility = View.VISIBLE
                binding.tvViewDownloads.visibility = View.VISIBLE

                Glide.with(this).load(File(downloadedItem.filePath)).into(ivThumbnail)

                ivThumbnail.visibility = View.VISIBLE
                ivRecentPlayIcon.visibility = View.GONE
            } else if (isVideoFile(File(downloadedItem.filePath))) {
                binding.container.visibility = View.VISIBLE
                binding.tvRecentDownloads.visibility = View.VISIBLE
                binding.tvViewDownloads.visibility = View.VISIBLE

                Glide.with(this).load(File(downloadedItem.filePath)).into(ivThumbnail)

                ivThumbnail.visibility = View.VISIBLE
                ivRecentPlayIcon.visibility = View.VISIBLE
            } else {
                binding.container.visibility = View.GONE
                binding.tvRecentDownloads.visibility = View.GONE
                binding.tvViewDownloads.visibility = View.GONE
            }

            ivThumbnail.setOnClickListener {
                if (isVideo) {
                    activity?.let { it1 ->
                        playVideo(
                            it1,
                            0,
                            listOf(File(downloadedItem.filePath))
                        )
                    }
                } else {
                    activity?.let { it1 ->
                        showImage(
                            it1,
                            0,
                            listOf(File(downloadedItem.filePath))
                        )
                    }
                }
            }

            ivRecentMenu.setOnClickListener {
                val dialog = DownloadMenu()
                dialog.show(parentFragmentManager, "DownloadMenu")

                dialog.setOnOptionClickListener(object : DownloadMenu.OnOptionClickListener {
                    override fun onRepostInstagramClicked() {
                        activity?.let { it1 ->
                            shareFileToInstagram(
                                it1,
                                File(downloadedItem.filePath),
                                isVideo
                            )
                        }
                    }

                    override fun onShareClicked() {
                        activity?.let { it1 -> shareFile(it1, File(downloadedItem.filePath)) }
                    }

                    override fun onShareWhatsAppClicked() {
                        activity?.let { it1 -> shareOnWhatsApp(it1, File(downloadedItem.filePath)) }
                    }

                    override fun onRenameClicked() {
                        val renameDialogFragment = RenameDialogFragment().apply {
                            arguments = Bundle().apply {
                                putString("fileName", downloadedItem.fileName)
                            }

                            setRenameListener(object : RenameDialogFragment.RenameListener {
                                override fun onRenameConfirmed(newName: String) {
                                    //                                renameFile(context, item, newName, position)
                                    renameFile(File(downloadedItem.filePath), newName)
                                }
                            })
                        }
                        renameDialogFragment.show(
                            (context as AppCompatActivity).supportFragmentManager, "RenameDialog"
                        )
                    }

                    override fun onDeleteClicked() {
                        val dialog = DeleteConfirmationDialogFragment("1") {
                            deleteFile(File(downloadedItem.filePath))
                        }
                        activity?.let { it1 ->
                            dialog.show(
                                it1.supportFragmentManager, "DeleteConfirmationDialog"
                            )
                        }
                    }

                    override fun onPostOpenInstagram() {

                    }
                })
            }

            ivRecentShare.setOnClickListener {
                debounce(binding.root) {
                    activity?.let { it1 -> shareFile(it1, File(downloadedItem.filePath)) }
                }
            }

            tvRecentName.text = downloadedItem.fileName

            viewModel.allDownloadedItems?.observe(viewLifecycleOwner) { downloadedItems ->
                val matchedItem = downloadedItems.find { it.fileName == downloadedItem.fileName }
                val username = matchedItem?.username ?: ""
                val nameToShow = username.ifEmpty { downloadedItem.fileName }
                tvRecentName.text = nameToShow

                Glide.with(this).load(matchedItem?.profile).into(ivProfile)
            }

            binding.container.addView(downloadItemView)
        }
    }

    private fun renameFile(file: File, newName: String) {
        val directory = file.parentFile
        val newFile = File(directory, newName)

        val oldName = file.name.split(".")[0]
        val nName = newName.split(".")[0]

        if (file.renameTo(newFile)) {
            updateFileNameInDatabase(oldName, nName, newFile.absolutePath)
        } else {
            Toast.makeText(context, "Failed to rename file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFileNameInDatabase(oldName: String, newName: String, newPath: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.updateFileName(oldName, newName, newPath)
        }
    }

    private fun deleteFile(file: File) {
        if (file.exists()) {
            val nameWithoutExt = file.name.split(".")[0]
            viewModel.allDownloadedItems?.value?.firstOrNull { it.fileName == nameWithoutExt }
                ?.let { item ->
                    deleteFromDatabase(item)
                }

            binding.container.visibility = View.GONE
            binding.tvRecentDownloads.visibility = View.GONE
            binding.tvViewDownloads.visibility = View.GONE
            file.delete()
        }
    }

    private fun deleteFromDatabase(item: DownloadedItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteDownloadedItem(item.fileName)
        }
    }

    private fun copyUrl() {
        val clipboard = (activity?.getSystemService(Context.CLIPBOARD_SERVICE)) as? ClipboardManager
        val textToPaste = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()

        if (!textToPaste.isNullOrEmpty() && textToPaste.startsWith("https://www.instagram.com/")) {
            binding.etUrl.setText(textToPaste)
        }
    }
}