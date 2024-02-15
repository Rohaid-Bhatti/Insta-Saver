package com.hazel.instadownloader.features.browser

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hazel.instadownloader.databinding.FragmentBrowserBinding
import com.hazel.instadownloader.features.browser.downloadManager.DownloadManager
import java.io.BufferedReader
import java.io.IOException

class BrowserFragment : Fragment() {
    private var _binding: FragmentBrowserBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBrowserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val instagramUrl = "https://www.instagram.com/"
        binding.webView.loadUrl(instagramUrl)
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.addJavascriptInterface(WebAppInterface(requireContext()), "Android")
        val mobileUserAgent =
            "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.98 Mobile Safari/537.36"
        binding.webView.settings.userAgentString = mobileUserAgent

        binding.webView.canGoBack()
        binding.webView.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_UP && binding.webView.canGoBack()) {
                binding.webView.goBack()
                return@OnKeyListener true
            }
            false
        })

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                "insta_script.js".injectJavaScriptFile(binding.webView)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }
    }

    inner class WebAppInterface(
        private val context: Context,
    ) {
        @JavascriptInterface
        fun downloadMedia(message: String) {
            val fileName = message.substring(message.lastIndexOf('/') + 1)
            val downloader = DownloadManager(context)
            downloader.downloadFile(message, fileName)

            Log.d("SHOWING_IMG", "showToast: $message")
        }
    }

    private fun String.injectJavaScriptFile(webView: WebView) {
        try {
            val jsFileContent = activity?.assets?.open(this)?.bufferedReader()?.use(
                BufferedReader::readText
            )
            jsFileContent?.let { msg -> webView.evaluateJavascript(msg, null) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}