package com.hazel.instadownloader.features.download

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
    private lateinit var viewModel: DownloadViewModel
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DownloadViewModel::class.java]

        binding.progressBar.visibility = View.VISIBLE
        binding.ivDownloadBox.visibility = View.GONE
        binding.materialTextView.visibility = View.GONE

        viewModel.filesLiveData.observe(viewLifecycleOwner) { files ->
            updateUI(files)
        }

        lifecycleScope.launch {
            permissionRequestCount = DataStores.getPermissionRequestCount(requireContext()).first()

            if (!checkPermission(requireContext())) {
                handlePermissionDenied()
            } else {
                loadFiles()
            }
        }
    }

    private fun loadFiles() {
        viewModel.loadFiles()
    }

    private fun updateUI(files: List<File>) {
        binding.progressBar.visibility = View.GONE
        val arrayList = ArrayList(files)
        Log.d("TESTING_ADAPTER", "updateUI: ${arrayList.size}")

        if (files.isNotEmpty()) {
            downloadAdapter = DownloadAdapter(arrayList) { isDel ->
                if (isDel) {
                    loadFiles()
                }
            }
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
}