package com.hazel.instadownloader

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.navigation.NavigationView
import com.hazel.instadownloader.app.activities.LanguageActivity
import com.hazel.instadownloader.app.utils.DataStores
import com.hazel.instadownloader.app.utils.PermissionManager
import com.hazel.instadownloader.app.utils.switch
import com.hazel.instadownloader.core.database.DownloadedUrlViewModel
import com.hazel.instadownloader.core.extensions.debounce
import com.hazel.instadownloader.core.extensions.shareApp
import com.hazel.instadownloader.databinding.ActivityMainBinding
import com.hazel.instadownloader.features.bottomSheets.HelpFragment
import com.hazel.instadownloader.features.dialogBox.PermissionCheckDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private val downloadViewModel: DownloadedUrlViewModel by viewModels()
    private var navController: NavController? = null
    private var postUrl: String? = null
    private var permissionRequestCount = 0
    private var check: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            val isLanguageSelected = DataStores.isLanguageSelected(this@MainActivity).first()
            if (!isLanguageSelected) {
                showLanguageSelection()
                showBottomSheet()
                DataStores.storeLanguageSelected(this@MainActivity, true)
            }
        }

        // for saving the values in to data store
        CoroutineScope(Dispatchers.Main).launch {
            permissionRequestCount = DataStores.getPermissionRequestCount(this@MainActivity).first()

            if (!PermissionManager.checkPermission(this@MainActivity)) {
                handlePermissionDenied()
            } else {
                // Anything
            }
        }

        val autoDownloadView = binding.navView.getHeaderView(0)
        val switchAuto = autoDownloadView.findViewById<SwitchCompat>(R.id.switchAutoDownload)

        Log.d("TESTING_AUTO", "onCreate: $check")
        switchAuto.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            CoroutineScope(Dispatchers.Main).launch {
                switch = b
                downloadViewModel.setAutoDownloadBoolean(b)
                DataStores.storeAutoDownload(this@MainActivity, b)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            check = DataStores.isAutoDownloadShown(this@MainActivity).first()
                .also { switchAuto.isChecked = it }
            // downloadViewModel.setAutoDownloadBoolean(check)
            Log.d("TESTING_AUTO", "checking the switch: $check")
            withContext(Dispatchers.Main) {
                val intent = intent
                val action = intent.action
                val type = intent.type
                if ("android.intent.action.SEND" == action && type != null && "text/plain" == type) {
                    postUrl = intent.getStringExtra("android.intent.extra.TEXT")
                }

                val bundle = Bundle()
                bundle.putString("POST_URL", postUrl)
                bundle.putBoolean("AUTO_DOWNLOAD", check)
                val graph = navController?.navInflater?.inflate(R.navigation.nav_graph)
                if (graph != null) {
                    navController?.setGraph(graph, bundle)
                }
            }
        }


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setUpAppbar()
        binding.bottomNav.setupWithNavController(navController!!)
        binding.bottomNav.itemIconTintList = null
        binding.navView.setNavigationItemSelectedListener(this)

        val lanHeaderView = binding.navView.getHeaderView(0)
        val layoutLanguageNav = lanHeaderView.findViewById<ConstraintLayout>(R.id.layoutLanguageNav)

        layoutLanguageNav.setOnClickListener {
            val intent = Intent(this, LanguageActivity::class.java)
            startActivity(intent)
        }
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
    private fun setUpAppbar() = binding.appBar.apply {
        ivMenuBack.setOnClickListener {
            binding.drawerLayout.openDrawer(Gravity.LEFT)
        }

        tvTitleToolbar.text = getString(R.string.app_name)

        ivHelpMenu.setOnClickListener {
            debounce(binding.root) {
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
                debounce(binding.root) {
                    val bottomSheetFragment = HelpFragment()
                    bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
                }
                return true
            }

            R.id.nav_terms -> Toast.makeText(this, "Terms!", Toast.LENGTH_SHORT).show()
            R.id.nav_privacy -> Toast.makeText(this, "Privacy!", Toast.LENGTH_SHORT).show()
            R.id.nav_feedback -> Toast.makeText(this, "Feedback!", Toast.LENGTH_SHORT).show()
            R.id.nav_share -> {
                debounce(binding.root) {
                    shareApp()
                }
                return true
            }

            R.id.nav_rate -> Toast.makeText(this, "Rate!", Toast.LENGTH_SHORT).show()
            R.id.nav_version -> Toast.makeText(this, "Version!", Toast.LENGTH_SHORT).show()
            R.id.layoutLanguageNav -> Toast.makeText(this, "Version!", Toast.LENGTH_SHORT).show()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}