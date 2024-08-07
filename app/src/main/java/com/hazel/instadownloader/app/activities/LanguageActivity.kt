package com.hazel.instadownloader.app.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.hazel.instadownloader.R
import com.hazel.instadownloader.app.adapters.LanguageAdapter
import com.hazel.instadownloader.app.utils.DataStores
import com.hazel.instadownloader.core.extensions.langList
import com.hazel.instadownloader.databinding.ActivityLanguageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LanguageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLanguageBinding
    private lateinit var languageAdapter: LanguageAdapter
    private var selected: String = ""
    private var selectedName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the Toolbar
        /*setSupportActionBar(binding.appBar.myToolbar)
        setSupportActionBar(binding.appBar.root)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        binding.appBar.myToolbar.title = getString(R.string.language_title)*/
        binding.appBar.tvTitleToolbar.text = getString(R.string.language_title)
        binding.appBar.ivMenuBack.setImageResource(R.drawable.ic_arrow_back)
        binding.appBar.ivMenuBack.setOnClickListener {
            finish()
        }
        binding.appBar.ivHelpMenu.visibility = View.GONE
        binding.appBar.ivPremiumMenu.visibility = View.GONE
        binding.appBar.animationSaveLang.visibility = View.VISIBLE

        binding.appBar.animationSaveLang.setOnClickListener {
            finish()
        }

        languageAdapter =
            LanguageAdapter(langList(), this, currentLang()) { s: String, s2: String ->
                selected = s
                selectedName = s2
            }


        binding.recyclerViewLanguages.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewLanguages.adapter = languageAdapter
//        binding.recyclerViewLanguages.addItemDecoration(TopSpaceItemDecoration())

        /*CoroutineScope(Dispatchers.IO).launch {
            val isButtonShown = DataStores.isSaveButtonShown(this@LanguageActivity).first()
            withContext(Dispatchers.Main) {
                if (!isButtonShown) {
                    binding.btnSave.visibility = View.GONE
                    binding.transparentSpace.visibility = View.GONE
                }
            }
        }*/

        // just for the testing purpose i am dismissing the activity
        /*binding.btnSave.setOnClickListener {
            saveLanguage()
        }

//        val get = Locale.getDefault().language
        binding.tvNext.setOnClickListener {
            if (get != selected) {
                if (selected.isNotEmpty()) {
                    changeLanguage(selected)
                    TinyDB.getInstance(this).putString("languageName", selectedName)
                    navigateToScreen()

                }
                else{

                    navigateToScreen()
                }
            }
        }*/
    }

    private fun currentLang(): String {
        return Locale.getDefault().language
    }

    /*private fun navigateToScreen() {

        val navBool  =intent.getBooleanExtra(Constants.LANG_SETTING,false)
        if (!navBool) {
            val intent = Intent(this, ChooseSelectedSoundActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }*/

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater = menuInflater
//        inflater.inflate(R.menu.menu_language_activity, menu)
//
//        val saveBtn = menu?.findItem(R.id.action_save)
//        val actionView = saveBtn?.actionView as ConstraintLayout
//        val actionSave =
//            actionView.findViewById<LottieAnimationView>(R.id.save_animation_view)
//        actionSave.setOnClickListener {
//            Toast.makeText(this, "Save button clicked", Toast.LENGTH_SHORT).show()
//            finish()
//        }
//
//        return true
//    }

    // Override onOptionsItemSelected to handle back button click
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            android.R.id.home -> {
//                onBackPressedDispatcher.onBackPressed()
//                return true
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
}