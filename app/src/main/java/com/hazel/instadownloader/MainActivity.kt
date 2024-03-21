package com.hazel.instadownloader

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import com.hazel.instadownloader.app.activities.LanguageActivity
import com.hazel.instadownloader.app.adapters.ViewPagerAdapter
import com.hazel.instadownloader.app.utils.DataStores
import com.hazel.instadownloader.app.utils.PermissionManager
import com.hazel.instadownloader.app.utils.switch
import com.hazel.instadownloader.core.database.DownloadedUrlViewModel
import com.hazel.instadownloader.core.extensions.debounce
import com.hazel.instadownloader.core.extensions.shareApp
import com.hazel.instadownloader.databinding.ActivityMainBinding
import com.hazel.instadownloader.features.bottomSheets.HelpFragment
import com.hazel.instadownloader.features.bottomSheets.RateUsBottomSheetFragment
import com.hazel.instadownloader.features.dialogBox.PermissionCheckDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var binding: ActivityMainBinding? = null
    private val downloadViewModel: DownloadedUrlViewModel by viewModels()
    private var postUrl: String? = null
    private var permissionRequestCount = 0
    private var checkingSwitch: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        showDataStoreFun()
        switchFun()

        val intent = intent
        val action = intent.action
        val type = intent.type
        if ("android.intent.action.SEND" == action && type != null && "text/plain" == type) {
            postUrl = intent.getStringExtra("android.intent.extra.TEXT")
        }

        setUpAppViewPager(postUrl)
        setUpAppbar()

        binding?.bottomNav?.itemIconTintList = null
        binding?.navView?.setNavigationItemSelectedListener(this)

        val lanHeaderView = binding?.navView?.getHeaderView(0)
        val layoutLanguageNav =
            lanHeaderView?.findViewById<ConstraintLayout>(R.id.layoutLanguageNav)

        layoutLanguageNav?.setOnClickListener {
            val toLanActivity = Intent(this, LanguageActivity::class.java)
            startActivity(toLanActivity)
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressed()
            }
        })
    }

    private fun switchFun() {
        val autoDownloadView = binding?.navView?.getHeaderView(0)
        val switchAuto = autoDownloadView?.findViewById<SwitchCompat>(R.id.switchAutoDownload)
        switchAuto?.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            CoroutineScope(Dispatchers.Main).launch {
                switch = b
                downloadViewModel.setAutoDownloadBoolean(b)
                DataStores.storeAutoDownload(this@MainActivity, b)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            checkingSwitch = DataStores.isAutoDownloadShown(this@MainActivity).first()
                .also { switchAuto?.isChecked = it }
            Log.d("TESTING_AUTO", "checking the switch: $checkingSwitch")
        }
    }

    private fun showDataStoreFun() {
        CoroutineScope(Dispatchers.IO).launch {
            val isLanguageSelected = DataStores.isLanguageSelected(this@MainActivity).first()
            if (!isLanguageSelected) {
                showLanguageSelection()
                showBottomSheet()
                DataStores.storeLanguageSelected(this@MainActivity, true)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            permissionRequestCount = DataStores.getPermissionRequestCount(this@MainActivity).first()

            if (!PermissionManager.checkPermission(this@MainActivity)) {
                handlePermissionDenied()
            } else {
                // Anything
            }
        }
    }

    private fun setUpAppViewPager(postUrl: String?) {
        val adapter = ViewPagerAdapter(this, postUrl){
            binding?.fragmentViewPager?.currentItem = 2
        }
        binding?.fragmentViewPager?.adapter = adapter
        binding?.fragmentViewPager?.isUserInputEnabled = false
        binding?.fragmentViewPager?.offscreenPageLimit = 3
        binding?.bottomNav?.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> binding?.fragmentViewPager?.currentItem = 0
                R.id.browserFragment -> binding?.fragmentViewPager?.currentItem = 1
                R.id.downloadFragment -> binding?.fragmentViewPager?.currentItem = 2
            }
            true
        }

        binding?.fragmentViewPager?.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding?.bottomNav?.menu?.getItem(position)?.isChecked = true
            }
        })
    }

    private fun handlePermissionDenied() {
        if (permissionRequestCount >= 1) {
            showCustomPermissionDeniedDialog()
        } else {
            requestPermission()
        }
    }

    private fun showCustomPermissionDeniedDialog() {
        val dialogFragment = PermissionCheckDialogFragment()
        dialogFragment.show(
            this.supportFragmentManager, "PermissionDeniedDialogFragment"
        )
    }

    //for permissions
    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VIDEO,
                    ), PERMISSION_REQUEST_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    ), PERMISSION_REQUEST_CODE
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ), PERMISSION_REQUEST_CODE
            )
        }

        CoroutineScope(Dispatchers.IO).launch {
            permissionRequestCount++
            DataStores.storePermissionRequestCount(permissionRequestCount, this@MainActivity)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                loadMediaFromFolder()
            } else {
                Toast.makeText(
                    this, "Permission denied. Cannot load media.", Toast.LENGTH_SHORT
                ).show()
                //     finish()
            }
        }
    }

    //for appbar
    private fun setUpAppbar() = binding?.appBar?.apply {
        ivMenuBack.setOnClickListener {
            binding?.drawerLayout?.openDrawer(Gravity.LEFT)
        }

        tvTitleToolbar.text = getString(R.string.app_name)

        ivHelpMenu.setOnClickListener {
            debounce(binding!!.root) {
                showBottomSheet()
            }
        }

        ivPremiumMenu.setOnClickListener {
            Toast.makeText(this@MainActivity, "Premium Clicked!", Toast.LENGTH_SHORT).show()
        }
    }

    // for showing the help bottom sheet
    private fun showBottomSheet() {
        val bottomSheetFragment = HelpFragment()
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    // for showing the language activity for the first time of the app
    private fun showLanguageSelection() {
        val intent = Intent(this, LanguageActivity::class.java)
        startActivity(intent)
    }

    //for navigation drawer option selection
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_help -> {
                debounce(binding!!.root) {
                    val bottomSheetFragment = HelpFragment()
                    bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
                }
                return true
            }

            R.id.nav_terms -> Toast.makeText(this, "Terms!", Toast.LENGTH_SHORT).show()
            R.id.nav_privacy -> Toast.makeText(this, "Privacy!", Toast.LENGTH_SHORT).show()
            R.id.nav_feedback -> Toast.makeText(this, "Feedback!", Toast.LENGTH_SHORT).show()
            R.id.nav_share -> {
                debounce(binding!!.root) {
                    shareApp()
                }
                return true
            }

            R.id.nav_rate -> {
                debounce(binding!!.root){
                    val rateUsBottomSheet = RateUsBottomSheetFragment()
                    rateUsBottomSheet.show(supportFragmentManager, "RateUsBottomSheet")
                }
                return true
            }
            R.id.nav_version -> Toast.makeText(this, "Version!", Toast.LENGTH_SHORT).show()
            R.id.layoutLanguageNav -> Toast.makeText(this, "Version!", Toast.LENGTH_SHORT).show()
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    fun backPressed() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else if (binding?.fragmentViewPager?.currentItem != 0) {
            binding?.fragmentViewPager?.currentItem = 0
        } else {
            finish()
        }
    }
}