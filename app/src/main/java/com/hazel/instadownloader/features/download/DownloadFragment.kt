package com.hazel.instadownloader.features.download

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hazel.instadownloader.app.utils.DataStores
import com.hazel.instadownloader.app.utils.PermissionManager.checkPermission
import com.hazel.instadownloader.core.database.AppDatabase
import com.hazel.instadownloader.core.database.DownloadedUrlRepository
import com.hazel.instadownloader.core.database.DownloadedUrlViewModel
import com.hazel.instadownloader.databinding.FragmentDownloadBinding
import com.hazel.instadownloader.features.dialogBox.DeleteConfirmationDialogFragment
import com.hazel.instadownloader.features.dialogBox.PermissionCheckDialogFragment
import com.hazel.instadownloader.features.download.adapter.DownloadAdapter
import com.hazel.instadownloader.features.download.model.MergeDownloadItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.log

class DownloadFragment : Fragment(), DownloadAdapter.SelectionModeListener {
    private val downloadedUrlViewModel: DownloadedUrlViewModel by viewModels()
    //    private val viewModel: DownloadViewModel by activityViewModels()
    private lateinit var downloadAdapter: DownloadAdapter
    private var _binding: FragmentDownloadBinding? = null
    private val binding get() = _binding!!

    private val activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap ->

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if (permissionsMap[Manifest.permission.READ_MEDIA_IMAGES] == true && permissionsMap[Manifest.permission.READ_MEDIA_VIDEO] == true) {

                    loadFiles()
                } else {

                    binding.progressBar.visibility = View.GONE
                    binding.ivDownloadBox.visibility = View.VISIBLE
                    binding.materialTextView.visibility = View.VISIBLE
                }
            } else {

                if (permissionsMap[Manifest.permission.READ_EXTERNAL_STORAGE] == true && permissionsMap[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
                    loadFiles()
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.ivDownloadBox.visibility = View.VISIBLE
                    binding.materialTextView.visibility = View.VISIBLE
                }
            }
        }

    private var permissionRequestCount = 0
    private var isSelect = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { downloadedUrlViewModel.init(it) }

        binding.progressBar.visibility = View.VISIBLE
        binding.ivDownloadBox.visibility = View.GONE
        binding.materialTextView.visibility = View.GONE
        binding.btnDelete.visibility = View.GONE
        binding.checkBoxSelectAll.visibility = View.GONE
        binding.textViewSelectedCount.visibility = View.GONE

//        /*viewModel*/downloadedUrlViewModel.filesLiveData.observe(viewLifecycleOwner) { files ->
//            updateUI(files)
//        }

        downloadedUrlViewModel.allDownloadedItems?.observe(viewLifecycleOwner) { downloadedItems ->
            val files = downloadedItems.map { File(it.caption) }
            Log.d("TESTING_FILES", "onViewCreated: $files")
        }

        lifecycleScope.launch {
            permissionRequestCount = DataStores.getPermissionRequestCount(requireContext()).first()

            if (!activity?.let { checkPermission(it) }!!) {
                handlePermissionDenied()
            } else {
                delayBeforeLoadingFiles()
            }
        }

        binding.btnDelete.setOnClickListener {
//            downloadAdapter.deleteSelectedFiles()
            showDeleteDialog()
            toggleDeleteButtonVisibility(false)
        }

        binding.checkBoxSelectAll.setOnClickListener {
            if (!isSelect) {
                isSelect = true
                downloadAdapter.selectAllItems(this.isSelect)
            } else {
                isSelect = false
                downloadAdapter.selectAllItems(this.isSelect)
            }
        }

        initBackPressDispatcher()
    }

    private fun showDeleteDialog() {
        val dialog =
            DeleteConfirmationDialogFragment(downloadAdapter.selectedFiles.size.toString()) {
                downloadAdapter.deleteSelectedFiles()
            }
        dialog.show(
            (context as AppCompatActivity).supportFragmentManager,
            "DeleteConfirmationDialog"
        )
    }

    private fun toggleDeleteButtonVisibility(isVisible: Boolean) {
        binding.btnDelete.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.textViewSelectedCount.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.checkBoxSelectAll.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onSelectionModeEnabled(enabled: Boolean) {
        if (enabled) {
            binding.btnDelete.visibility = View.VISIBLE
            binding.textViewSelectedCount.visibility = View.VISIBLE
            binding.checkBoxSelectAll.visibility = View.VISIBLE

            "${downloadAdapter.selectedFiles.size} Selected".also {
                binding.textViewSelectedCount.text = it
            }
            val allFilesSelected = downloadAdapter.isAllSelected
            binding.checkBoxSelectAll.isChecked = allFilesSelected
        } else {
            binding.btnDelete.visibility = View.GONE
            binding.textViewSelectedCount.visibility = View.GONE
            binding.checkBoxSelectAll.visibility = View.GONE
        }
    }

//    private fun loadFiles() {
//        /*viewModel*/downloadedUrlViewModel.loadFiles()
//    }

    private fun loadFiles() {
        downloadedUrlViewModel.loadFiles()

        downloadedUrlViewModel.filesLiveData.observe(viewLifecycleOwner) { files ->
            downloadedUrlViewModel.allDownloadedItems?.observe(viewLifecycleOwner) { downloadedItems ->
                val mergeItems = mutableListOf<MergeDownloadItem>()

                files.forEach { file ->
                    val filenameWithoutExt = file.name.split(".")[0]

                    val downloadedItem = downloadedItems.find { it.fileName == filenameWithoutExt }
                    val username = downloadedItem?.username ?: ""
                    val caption = downloadedItem?.caption ?: ""
                    val url = downloadedItem?.url ?: ""
                    val postUrl = downloadedItem?.postUrl ?: ""

                    val mergeItem = MergeDownloadItem(file, username, caption, url, postUrl)
                    mergeItems.add(mergeItem)
                }

                updateUI(mergeItems)
            }
        }
    }

    private fun delayBeforeLoadingFiles() {
        lifecycleScope.launch {
            delay(1000)
            loadFiles()
        }
    }

    private fun updateUI(mergeItems: List<MergeDownloadItem>) {
        binding.progressBar.visibility = View.GONE
//        val arrayList = ArrayList(files)

        if (mergeItems.isNotEmpty()) {
            downloadAdapter = DownloadAdapter(mergeItems) { isDel ->
                if (isDel) {
                    loadFiles()
                }
                toggleDeleteButtonVisibility(downloadAdapter.isSelectionModeEnabled)
            }
            downloadAdapter.setSelectionModeListener(this)

            binding.recyclerView.layoutManager = LinearLayoutManager(context)
            binding.recyclerView.adapter = downloadAdapter
            binding.btnDelete.visibility = View.GONE
            binding.textViewSelectedCount.visibility = View.GONE
            binding.checkBoxSelectAll.visibility = View.GONE

            toggleDeleteButtonVisibility(false)

            binding.ivDownloadBox.visibility = View.GONE
            binding.materialTextView.visibility = View.GONE

//            Log.d("TESTING_FILES", "updateUI: before the for loop")
//            for (file in files) {
//                Log.d("TESTING_FILES", "updateUI: in the for loop")
//                Log.d("TESTING_FILES", "updateUI: ${file.name}")
//                val filename = file.name.split(".")[0]
//                downloadedUrlViewModel.getDownloadedItemByFileName(filename)
//                    .observe(requireActivity()) { downloadedItems ->
//                        for (i in downloadedItems.indices){
//                            Log.d("TESTING_FILES", "updateUI: $downloadedItems")
//                            Log.d("TESTING_FILES", "updateUI: ${downloadedItems[i].caption}")
//                            downloadAdapter.setUsernameAndCaption(
//                                downloadedItems[i].username,
//                                downloadedItems[i].caption
//                            )
//                            downloadAdapter.notifyItemChanged(i)
//                        }
//                    }
//            }
        } else {
            if (!checkPermission(requireContext())) {
                binding.ivDownloadBox.visibility = View.VISIBLE
                binding.materialTextView.visibility = View.VISIBLE
            } else {
                binding.ivDownloadBox.visibility = View.VISIBLE
                binding.materialTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun handlePermissionDenied() {
        if (permissionRequestCount >= 1) {
            showCustomPermissionDeniedDialog()

            binding.progressBar.visibility = View.GONE
            binding.ivDownloadBox.visibility = View.VISIBLE
            binding.materialTextView.visibility = View.VISIBLE
        } else {
            requestPermission()
        }
    }

    private fun showCustomPermissionDeniedDialog() {

        val dialogFragment = PermissionCheckDialogFragment()
        activity?.let {
            dialogFragment.show(
                it.supportFragmentManager,
                "PermissionDeniedDialogFragment"
            )
        }
    }

    override fun onResume() {
        super.onResume()

        if (!checkPermission(requireContext())) {
            binding.progressBar.visibility = View.GONE
            binding.ivDownloadBox.visibility = View.VISIBLE
            binding.materialTextView.visibility = View.VISIBLE
        } else {
            loadFiles()
        }
    }

    private fun requestPermission() {
        val permissionRequest: MutableList<String> = ArrayList()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionRequest.add(
                Manifest.permission.READ_MEDIA_IMAGES,
            )
            permissionRequest.add(
                Manifest.permission.READ_MEDIA_VIDEO,
            )
        } else {
            permissionRequest.add(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )

            permissionRequest.add(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        if (permissionRequest.isNotEmpty()) {
            activityResultLauncher.launch(permissionRequest.toTypedArray())
        }

        CoroutineScope(Dispatchers.IO).launch {
            permissionRequestCount++
            activity?.let { DataStores.storePermissionRequestCount(permissionRequestCount, it) }
        }
    }

    private fun initBackPressDispatcher() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (downloadAdapter.isSelectionModeEnabled) {
                        downloadAdapter.selectAllItems(false)
                        downloadAdapter.isSelectionModeEnabled = false
                        downloadAdapter.notifyDataSetChanged()
                        Log.d(
                            "TESTING_FILES",
                            "handleOnBackPressed: ${downloadAdapter.isSelectionModeEnabled}"
                        )
                    } else {
                        findNavController().navigateUp()
                    }
                }
            })
    }
}