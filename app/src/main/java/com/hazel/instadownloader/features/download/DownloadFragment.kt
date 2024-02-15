package com.hazel.instadownloader.features.download

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.hazel.instadownloader.app.utils.DataStores
import com.hazel.instadownloader.app.utils.PermissionManager.checkPermission
import com.hazel.instadownloader.databinding.FragmentDownloadBinding
import com.hazel.instadownloader.features.dialogBox.PermissionCheckDialogFragment
import com.hazel.instadownloader.features.download.adapter.DownloadAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class DownloadFragment : Fragment() {
    private var _binding: FragmentDownloadBinding? = null
    private val binding get() = _binding!!

    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap ->

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if (permissionsMap[Manifest.permission.READ_MEDIA_IMAGES] == true && permissionsMap[Manifest.permission.READ_MEDIA_VIDEO] == true) {

                    lifecycleScope.launch {
                        val files = loadFilesInBackground()
                        updateUI(files)
                    }
                } else {

                    binding.progressBar.visibility = View.GONE
                    binding.ivDownloadBox.visibility = View.VISIBLE
                    binding.materialTextView.visibility = View.VISIBLE
                }
            } else {

                if (permissionsMap[Manifest.permission.READ_EXTERNAL_STORAGE] == true && permissionsMap[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
                    lifecycleScope.launch {
                        val files = loadFilesInBackground()
                        updateUI(files)
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.ivDownloadBox.visibility = View.VISIBLE
                    binding.materialTextView.visibility = View.VISIBLE
                }
            }
        }

    private lateinit var downloadAdapter: DownloadAdapter
    private var permissionRequestCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.VISIBLE
        binding.ivDownloadBox.visibility = View.GONE
        binding.materialTextView.visibility = View.GONE
        loadFilesAsync()
    }

    private fun loadFilesAsync() {
        lifecycleScope.launch {
            permissionRequestCount = DataStores.getPermissionRequestCount(requireContext()).first()

            if (!checkPermission(requireContext())) {
                handlePermissionDenied()
            } else {
                val files = loadFilesInBackground()
                updateUI(files)
            }
        }
    }

    private suspend fun loadFilesInBackground(): List<File> {
        kotlinx.coroutines.delay(250)

        val downloadFolder =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath)

        return downloadFolder.listFiles { file ->
            file.isFile &&
                    (file.extension.equals("jpg", ignoreCase = true) ||
                            file.extension.equals("png", ignoreCase = true) ||
                            file.extension.equals("jpeg", ignoreCase = true) ||
                            file.extension.equals("mp4", ignoreCase = true) ||
                            file.extension.equals("3gp", ignoreCase = true) ||
                            file.extension.equals("mkv", ignoreCase = true))
        }?.toList() ?: emptyList()
    }

    private fun updateUI(files: List<File>) {
        binding.progressBar.visibility = View.GONE

        if (files.isNotEmpty()) {
            downloadAdapter = DownloadAdapter(files as ArrayList<File>)
            binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
            binding.recyclerView.adapter = downloadAdapter

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
        if (permissionRequestCount >= 2) {
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
        dialogFragment.show(
            requireActivity().supportFragmentManager,
            "PermissionDeniedDialogFragment"
        )
    }

    override fun onResume() {
        super.onResume()

        if (!checkPermission(requireContext())) {
            binding.progressBar.visibility = View.GONE
            binding.ivDownloadBox.visibility = View.VISIBLE
            binding.materialTextView.visibility = View.VISIBLE
        } else {
            lifecycleScope.launch {
                val files = loadFilesInBackground()
                updateUI(files)
            }
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
            DataStores.storePermissionRequestCount(permissionRequestCount, requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}