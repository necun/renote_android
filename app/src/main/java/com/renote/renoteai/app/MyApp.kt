package com.renote.renoteai.app

import android.app.Application
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
    }
}