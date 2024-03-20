package com.renote.renoteai.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.renote.renoteai.R
import com.renote.renoteai.ui.activities.camera.CameraActivity
import com.renote.renoteai.ui.presentation.home.HomeFragment
import com.renote.renoteai.ui.fragments.imports.ImportFragment
import com.renote.renoteai.ui.presentation.home.viewmodel.HomeFragmentViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.renote.renoteai.database.dao.DocumentDao
import com.renote.renoteai.databinding.ActivityMainBinding
import com.renote.renoteai.ui.presentation.home.workers.DocumentSyncWorker
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {

    private var currentFragment: Fragment? = null
    var frameLayout: FrameLayout? = null
    var fragmentManager: FragmentManager? = null
    var fragmentTransaction: FragmentTransaction? = null

    private lateinit var btnProfile: ImageView
    private lateinit var uri: Uri
    private var _binding: ActivityMainBinding? = null
    private lateinit var navView: BottomNavigationView
    private val binding get() = _binding
    private val REQUEST_CODE_OPEN_DOCUMENT = 123
    private val viewModel: HomeFragmentViewModel by inject()
    lateinit var recyclerView: RecyclerView
    // private val noteAdapter by lazy { NoteAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        navView = findViewById(R.id.bottomNavigationView)

        println(currentFragment)

        fetchRemoteConfig()

        Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)

        val homeFragment = HomeFragment()
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.frameLayout,
                homeFragment
            ) // Ensure you have a FrameLayout in your layout where the fragment should be placed
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            //.addToBackStack(null) // Optional: Add transaction to the back stack
            .commit()

        currentFragment = homeFragment

        navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navImport -> {

//                    if (currentFragment !is ImportFragment) {
//                        //recyclerView.visibility = View.GONE
//                        // Replace Toast with Fragment transaction
//                        val importFragment = ImportFragment()
//                        supportFragmentManager.beginTransaction()
//                            .replace(
//                                R.id.frameLayout,
//                                importFragment
//                            ) // Ensure you have a FrameLayout in your layout where the fragment should be placed
//                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                            //.addToBackStack(null) // Optional: Add transaction to the back stack
//                            .commit()
////                    recyclerView.visibility = View.GONE
//                        currentFragment = importFragment
//                        Firebase.crashlytics.log("This is a test log message.")
//                        Firebase.crashlytics.setCustomKey("UserId", "12345")
//                        Firebase.crashlytics.setCustomKey("SubscriptionType", "Premium")
//                        throw RuntimeException("Test Crash")
                        decideActionBasedOnRemoteConfig()
                  //  }
                }

                R.id.navHome -> {
                    if (currentFragment !is HomeFragment) {
                        val homeFragment = HomeFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.frameLayout,
                                homeFragment
                            ) // Ensure you have a FrameLayout in your layout where the fragment should be placed
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            //.addToBackStack(null) // Optional: Add transaction to the back stack
                            .commit()

                        currentFragment = homeFragment
                    }
                }
            }
            true
        }

        binding!!.btnCamNav.setOnClickListener {
            startActivity(Intent(this@MainActivity, CameraActivity::class.java))
            finish()
        }

    }

    private fun fetchRemoteConfig() {
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val updated = task.result
                println("Config params updated: $updated")
            } else {
                println("Fetch failed")
            }
        }
    }

    private fun decideActionBasedOnRemoteConfig() {
        val featureEnableToast = Firebase.remoteConfig.getBoolean("feature_enable_toast")
        if (featureEnableToast) {
            // Show toast message from Remote Config
            val toastMessage = Firebase.remoteConfig.getString("toast_message")
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
        } else {
            // Navigate to a new screen
//            val intent = Intent(this, NewActivity::class.java) // Replace NewActivity with your target activity
//            startActivity(intent)
            if (currentFragment !is ImportFragment) {
                //recyclerView.visibility = View.GONE
                // Replace Toast with Fragment transaction
                val importFragment = ImportFragment()
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.frameLayout,
                        importFragment
                    ) // Ensure you have a FrameLayout in your layout where the fragment should be placed
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    //.addToBackStack(null) // Optional: Add transaction to the back stack
                    .commit()
//                    recyclerView.visibility = View.GONE
                currentFragment = importFragment
                Firebase.crashlytics.log("This is a test log message.")
                Firebase.crashlytics.setCustomKey("UserId", "12345")
                Firebase.crashlytics.setCustomKey("SubscriptionType", "Premium")
               // throw RuntimeException("Test Crash")
            }

        }
    }


    private fun openFile(fileUri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, contentResolver.getType(fileUri))
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(Intent.createChooser(intent, "Open File"))
        } catch (e: Exception) {
            Log.e("MainActivity", "Error opening file: ${e.message}")
            Toast.makeText(this, "Error opening file", Toast.LENGTH_SHORT).show()
        }
    }

}
