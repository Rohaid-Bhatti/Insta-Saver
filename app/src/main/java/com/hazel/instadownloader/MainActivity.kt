package com.hazel.instadownloader

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.hazel.instadownloader.app.activities.LanguageActivity
import com.hazel.instadownloader.app.activities.SettingActivity
import com.hazel.instadownloader.app.utils.DataStores
import com.hazel.instadownloader.core.extensions.debounce
import com.hazel.instadownloader.core.extensions.shareApp
import com.hazel.instadownloader.databinding.ActivityMainBinding
import com.hazel.instadownloader.features.bottomSheets.HelpFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
//    private var permissionRequestCount = 0

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
        /*CoroutineScope(Dispatchers.Main).launch {
            permissionRequestCount = DataStores.getPermissionRequestCount(this@MainActivity).first()

            if (!PermissionManager.checkPermission(this@MainActivity)) {
                handlePermissionDenied()
            } else {
                // Anything
            }
        }*/

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setUpAppbar()
        binding.bottomNav.setupWithNavController(navController)
        binding.bottomNav.itemIconTintList = null
        binding.navView.setNavigationItemSelectedListener(this)

        val intent = intent
        val action = intent.action
        val type = intent.type
        if ("android.intent.action.SEND" == action && type != null && "text/plain" == type) {
            val intent1 = intent.getStringExtra("android.intent.extra.TEXT")
            Log.d("TESTING_FILES", "onCreate: $intent1")
        }
    }

    /*private fun handlePermissionDenied() {
        if (permissionRequestCount >= 2) {
//            showCustomPermissionDeniedDialog()
        } else {
            requestPermission()
        }
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
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                    ), PERMISSION_REQUEST_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    ), PERMISSION_REQUEST_CODE
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
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
    }*/

    //for appbar
    private fun setUpAppbar() = binding.appBar.apply {
        myToolbar.title = getString(R.string.app_name)
        setSupportActionBar(root)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.hamburger_icon)
        }

        val toggle = ActionBarDrawerToggle(
            this@MainActivity,
            binding.drawerLayout,
            binding.appBar.myToolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    //for toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //for toolbar options
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.help_icon -> {
                showBottomSheet()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
            R.id.nav_language -> {
                val intent = Intent(this, LanguageActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.nav_terms -> Toast.makeText(this, "Terms!", Toast.LENGTH_SHORT).show()
            R.id.nav_privacy -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.nav_feedback -> Toast.makeText(this, "Feedback!", Toast.LENGTH_SHORT).show()
            R.id.nav_share -> {
                debounce(binding.root) {
                    shareApp()
                }
                return true
            }

            R.id.nav_rate -> Toast.makeText(this, "Rate!", Toast.LENGTH_SHORT).show()
            R.id.nav_version -> Toast.makeText(this, "Version!", Toast.LENGTH_SHORT).show()
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