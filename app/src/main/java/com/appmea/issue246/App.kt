package com.appmea.issue246

import android.app.Application
import android.content.res.Configuration
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App : Application() {
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        AppConfig.onConfigChanged(this, newConfig)
    }

    override fun onCreate() {
        super.onCreate()
        AppConfig.onConfigChanged(this, null)
    }
}