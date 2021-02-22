package com.apps.kunalfarmah.echo

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MultiDexApplication : MultiDexApplication(){
    override fun onCreate() {
        super.onCreate()
    }
}