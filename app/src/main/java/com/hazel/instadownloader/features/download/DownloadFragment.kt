package com.hazel.instadownloader.features.download

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hazel.instadownloader.app.utils.DataStores
import com.hazel.instadownloader.app.utils.PermissionManager.checkPermission
import com.hazel.instadownloader.core.database.DownloadedItem
import com.hazel.instadownloader.core.database.DownloadedUrlViewModel
import com.hazel.instadownloader.core.extensions.isVideoFile
import com.hazel.instadownloader.core.extensions.openInstagramPostInApp
import com.hazel.instadownloader.core.extensions.shareFile
import com.hazel.instadownloader.core.extensions.shareFileToInstagram
import com.hazel.instadownloader.core.extensions.shareOnWhatsApp
import com.hazel.instadownloader.databinding.FragmentDownloadBinding
import com.hazel.instadownloader.features.bottomSheets.DownloadMenu
import com.hazel.instadownloader.features.dialogBox.DeleteConfirmationDialogFragment
import com.hazel.instadownloader.features.dialogBox.PermissionCheckDialogFragment
import com.hazel.instadownloader.features.dialogBox.RenameDialogFragment
import com.hazel.instadownloader.features.download.adapter.DeleteFileCallback
import com.hazel.instadownloader.features.download.adapter.DownloadAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class DownloadFragment : Fragment(), DownloadAdapter.SelectionModeListener, DeleteFileCallback {
    private val downloadedUrlViewModel: DownloadedUrlViewModel by activityViewModels()
    private var downloadAdapter: DownloadAdapter? = null
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

        lifecycleScope.launch {
            permissionRequestCount = DataStores.getPermissionRequestCount(requireContext()).first()

            if (!activity?.let { checkPermission(it) }!!) {
                handlePermissionDenied()
            } else {
                loadFiles()
            }
        }

        binding.btnDelete.setOnClickListener {
            showDeleteDialog()
            toggleDeleteButtonVisibility(false)
        }

        binding.checkBoxSelectAll.setOnClickListener {
            if (!isSelect) {
                isSelect = true
                downloadAdapter?.selectAllItems(this.isSelect)
            } else {
                isSelect = false
                downloadAdapter?.selectAllItems(this.isSelect)
            }
        }

        initBackPressDispatcher()
    }

    private fun showItemMenuBottomSheet(context: Context, item: DownloadedItem, position: Int) {
        val bottomSheetFragment = DownloadMenu()

        bottomSheetFragment.setOnOptionClickListener(object : DownloadMenu.OnOptionClickListener {
            override fun onRepostInstagramClicked() {
                shareFileToInstagram(context, File(item.filePath), isVideoFile(File(item.filePath)))
            }

            override fun onShareClicked() {
                shareFile(context, File(item.filePath))
            }

            override fun onShareWhatsAppClicked() {
                shareOnWhatsApp(context, File(item.filePath))
            }

            override fun onRenameClicked() {
                showRenameDialog(context, item, position)
            }

            override fun onDeleteClicked() {
                showDeleteConfirmationDialog(context, item)
            }

            override fun onPostOpenInstagram() {
                item.url.openInstagramPostInApp(context)
            }
        })

        bottomSheetFragment.show(
            (context as AppCompatActivity).supportFragmentManager, bottomSheetFragment.tag
        )
    }

    private fun showDeleteConfirmationDialog(context: Context, item: DownloadedItem) {
        val dialog = DeleteConfirmationDialogFragment("1") {
            deleteFile(context, item)
        }
        dialog.show(
            (context as AppCompatActivity).supportFragmentManager, "DeleteConfirmationDialog"
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteFile(context: Context, item: DownloadedItem) {
        val contentUri = MediaStore.Files.getContentUri("external")
        val selection = "${MediaStore.Files.FileColumns.DATA} = ?"
        val selectionArgs = arrayOf(File(item.filePath).absolutePath)
        Log.d("TESTING_NAMES", "fileList: $selectionArgs")

        val rowsDeleted = context.contentResolver.delete(contentUri, selection, selectionArgs)

        downloadedUrlViewModel.allDownloadedItems?.value?.firstOrNull { it.fileName == item.fileName.split(".")[0] }?.let { item ->
            downloadedUrlViewModel.deleteDownloadedItem(item.fileName)
        }

        if (rowsDeleted > 0) {
            onDeleteFileSelected(true)
            downloadAdapter?.notifyDataSetChanged()
        }
    }

    private fun showRenameDialog(context: Context, item: DownloadedItem, position: Int) {
        val renameDialogFragment = RenameDialogFragment().apply {
            arguments = Bundle().apply {
                putString("fileName", item.fileName)
                putInt("position", position)
            }
            setRenameListener(object : RenameDialogFragment.RenameListener {
                override fun onRenameConfirmed(newName: String) {
                    renameFile(context, item, newName, position)
                }
            })
        }
        renameDialogFragment.show(
            (context as AppCompatActivity).supportFragmentManager, "RenameDialog"
        )
    }

    private fun renameFile(
        context: Context, item: DownloadedItem, newName: String, position: Int
    ) {
        val newFile = File(File(item.filePath).parent, newName)
        val oldName = item.fileName.split(".")[0]
        val nName = newName.split(".")[0]

        if (File(item.filePath).renameTo(newFile)) {
            downloadedUrlViewModel.updateFileName(oldName, nName, newFile.absolutePath)
            downloadAdapter?.notifyItemChanged(position)
        } else {
            Toast.makeText(context, "Failed to rename file", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showDeleteDialog() {
        val dialog =
            DeleteConfirmationDialogFragment(downloadAdapter?.selectedFiles?.size.toString()) {

                downloadAdapter?.selectedFiles?.forEach { item ->
                    File(item.filePath).delete()
                    downloadedUrlViewModel.deleteDownloadedItem(item.fileName.split(".")[0])
                }

                downloadAdapter?.selectedFiles?.clear()
                downloadAdapter?.isSelectionModeEnabled = false
                downloadAdapter?.notifyDataSetChanged()
                onDeleteFileSelected(true)

            }
        dialog.show(
            (context as AppCompatActivity).supportFragmentManager, "DeleteConfirmationDialog"
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

            "${downloadAdapter?.selectedFiles?.size} Selected".also {
                binding.textViewSelectedCount.text = it
            }
            val allFilesSelected = downloadAdapter?.isAllSelected
            binding.checkBoxSelectAll.isChecked = allFilesSelected!!
        } else {
            binding.btnDelete.visibility = View.GONE
            binding.textViewSelectedCount.visibility = View.GONE
            binding.checkBoxSelectAll.visibility = View.GONE
        }
    }

    private fun loadFiles() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(500)

            downloadedUrlViewModel.allDownloadedItems?.observe(viewLifecycleOwner) {downloadedItems ->
                val sortedList = downloadedItems.sortedByDescending { it.id }
                updateUI(sortedList)
            }
        }
    }

    private fun updateUI(mergeItems: List<DownloadedItem>) {
        binding.progressBar.visibility = View.GONE

        if (mergeItems.isNotEmpty()) {
            downloadAdapter = DownloadAdapter(mergeItems, this)
            downloadAdapter?.setSelectionModeListener(this)

            binding.recyclerView.layoutManager = LinearLayoutManager(context)
            binding.recyclerView.adapter = downloadAdapter
            binding.btnDelete.visibility = View.GONE
            binding.textViewSelectedCount.visibility = View.GONE
            binding.checkBoxSelectAll.visibility = View.GONE

            toggleDeleteButtonVisibility(false)

            binding.ivDownloadBox.visibility = View.GONE
            binding.materialTextView.visibility = View.GONE
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
                it.supportFragmentManager, "PermissionDeniedDialogFragment"
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
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (downloadAdapter?.isSelectionModeEnabled == true) {
                        downloadAdapter?.selectAllItems(false)
                        downloadAdapter?.isSelectionModeEnabled = false
                        downloadAdapter?.notifyDataSetChanged()
                        Log.d(
                            "TESTING_FILES",
                            "handleOnBackPressed: ${downloadAdapter?.isSelectionModeEnabled}"
                        )
                    } else {
                        findNavController().navigateUp()
                    }
                }
            })
    }

    override fun onShowingMenu(mergeItem: DownloadedItem, position: Int) {
        activity?.let { showItemMenuBottomSheet(it, mergeItem, position) }
    }

    override fun onDeleteFileSelected(isFileSelected: Boolean) {
        if (isFileSelected) {
            loadFiles()
        }
        downloadAdapter?.isSelectionModeEnabled?.let { toggleDeleteButtonVisibility(it) }

    }
}