package com.renote.renoteai.app

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.analytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.remoteconfig.remoteConfig
import com.renote.renoteai.R
import com.renote.renoteai.di.appComponents
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(appComponents)
        }
        FirebaseApp.initializeApp(this)

        //initialized firebase crash analytics
        Firebase.analytics

        //initialized firebase remote config for force update
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 5 // For development purposes
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Set default Remote Config values
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

    }
}