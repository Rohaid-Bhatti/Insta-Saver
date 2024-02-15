package com.hazel.instadownloader.features.home

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.hazel.instadownloader.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clipboard: ClipboardManager? =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        try {
            val urlText = clipboard?.primaryClip?.getItemAt(0)?.text!!
            Log.d("CHECKING_URL", "onViewCreated: $urlText")
        } catch (e: Exception) {
            return
        }

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

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(requireContext()))
        }

        val py = Python.getInstance()
        val module = py.getModule("script")
        val downloader = module["download"]
        val posts = module["post_count"]
        val linkDownloader = module["download_post_from_link"]

        binding.ivDownloadIcon.setOnClickListener {
            downloadFun(linkDownloader, posts, downloader)
        }
    }

    private fun downloadFun(linkDownloader: PyObject?, posts: PyObject?, downloader: PyObject?) {
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
                        url.substringAfter("https://www.instagram.com/p/").substringBefore("/")
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
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}