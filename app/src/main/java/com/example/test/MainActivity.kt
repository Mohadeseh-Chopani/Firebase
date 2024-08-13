package com.example.test

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings


class MainActivity : AppCompatActivity() {
    private lateinit var remoteConfig : FirebaseRemoteConfig

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnTest = findViewById<Button>(R.id.btnTest)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val searchBux = findViewById<EditText>(R.id.searchBux)
        val btnRemoteConfig = findViewById<Button>(R.id.btnTestRemoteConfig)
//        val btnCrashlytics = findViewById<Button>(R.id.btnCrashlytics)


        // set Analytics
        val firebaseAnalytics: FirebaseAnalytics by lazy {
            FirebaseAnalytics.getInstance(this)
        }

        val bundle = Bundle()
        btnTest.setOnClickListener {
            val params = Bundle()
            params.putString("invalid_url", "local622222")

            bundle.putString(FirebaseAnalytics.Param.METHOD, "google")
            bundle.putString(FirebaseAnalytics.Param.ACLID, "Click")

                firebaseAnalytics
                .logEvent(FirebaseAnalytics.Event.LOGIN, bundle)

                firebaseAnalytics.logEvent("customEvent", params)
        }

        btnSearch.setOnClickListener{
            bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, searchBux.text.toString())
            FirebaseAnalytics.getInstance(this)
                .logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle)
        }


        // test crashlytics
        val crashButton = Button(this)
        crashButton.text = "Test Crash"
        crashButton.setOnClickListener {
            Firebase.crashlytics.log("crash happening")
            throw RuntimeException("Test Crash") // Force a crash
        }
        addContentView(crashButton, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT))


        //test remoteConfig firebase
        btnRemoteConfig.setOnClickListener{
            testRemoteConfigFirebase()
        }
    }

    override fun onResume() {
        super.onResume()
        // rename screen_view in analytics
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "customName")
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.javaClass.name)
        FirebaseAnalytics.getInstance(this)
            .logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }


    fun testRemoteConfigFirebase() {
        remoteConfig = Firebase.remoteConfig

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Set default parameter values
        remoteConfig.setDefaultsAsync(R.xml.remote_config_default)

        // Fetch and activate remote config values
        fetchRemoteConfig()
    }

    private fun fetchRemoteConfig() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Apply the new configuration if fetched successfully
                    val updated = task.result
                    Log.d("MainActivity", "Config params updated: $updated")
                    applyRemoteConfig()
                } else {
                    // Handle error
                    Log.e("MainActivity", "Fetch failed")
                }
            }
    }

    private fun applyRemoteConfig() {
        // Example: Fetch a string value from Remote Config
        val welcomeMessage = remoteConfig.getString("welcome_message")
        // Apply the fetched value in your UI or logic
        Log.d("MainActivity", "Welcome Message: $welcomeMessage")
    }
}