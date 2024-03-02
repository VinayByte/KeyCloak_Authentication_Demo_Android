package com.egnize.keycloak

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.HiltAndroidApp


/**
 * Created by Vinay on 12/11/2023.
 * vinay.kumar@hpe.com
 */
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        sharedPreferences = getSharedPreferences("gain-app", MODE_PRIVATE)

    }

    companion object {
        lateinit var appContext: Context
        fun get(): App = appContext as App

        lateinit var sharedPreferences: SharedPreferences
    }
}