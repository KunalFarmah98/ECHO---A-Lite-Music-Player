package com.apps.kunalfarmah.echo

import android.media.MediaPlayer
import androidx.multidex.MultiDexApplication
import com.apps.kunalfarmah.echo.util.MediaUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: MultiDexApplication(){
    override fun onCreate() {
        super.onCreate()
        MediaUtils.mediaPlayer = MediaPlayer()
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }
}