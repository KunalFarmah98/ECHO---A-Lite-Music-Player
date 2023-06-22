package com.apps.kunalfarmah.echo.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.viewModel.SongsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: SongsViewModel by viewModels()
    private var permission_String = arrayOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor =
                    ContextCompat.getColor(applicationContext, R.color.splashStatusBar);
            }
        }
        catch (e:Exception){
            Log.e("ERROR","Couldn't change status bar color")
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            permission_String = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    android.Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            permission_String = arrayOf(
                    android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.ACCESS_MEDIA_LOCATION,
            )
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            permission_String = arrayOf(
                    android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.ACCESS_MEDIA_LOCATION,
                    android.Manifest.permission.READ_MEDIA_AUDIO,
                    android.Manifest.permission.POST_NOTIFICATIONS
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasPermissions(this@SplashActivity, *permission_String)) {
                ActivityCompat.requestPermissions(this@SplashActivity, permission_String, 1001)
            } else {
                DisplayActivity()
            }
        } else {
            if (!hasPermissions(this@SplashActivity, *permission_String)) {
                ActivityCompat.requestPermissions(this@SplashActivity, permission_String, 131)
            } else {
                DisplayActivity()
            }
        }
    }

    private fun grantedPermissions(grantResults: IntArray): Boolean {
        grantResults.forEach { if(it == PackageManager.PERMISSION_DENIED) return false }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            131 -> {
                if (grantResults.isNotEmpty() && grantedPermissions(grantResults)){
                    DisplayActivity()
                } else {
                    Toast.makeText(this@SplashActivity, "Please Grant All the Permissions To Continue", Toast.LENGTH_SHORT).show()
                    this.finish()
                }
            }
            1001 -> {
                if (grantResults.isNotEmpty() && grantedPermissions(grantResults)){
                    DisplayActivity()
                } else {
                    Toast.makeText(this@SplashActivity, "Please Grant All the Permissions To Continue", Toast.LENGTH_SHORT).show()
                    this.finish()
                }
            }
            else -> {
                Toast.makeText(this@SplashActivity, "Something Went Wrong", Toast.LENGTH_SHORT).show()
                this.finish()
            }
        }
    }

    fun hasPermissions(context: Context, vararg Permissions: String): Boolean {

        var hasAllPermisisons = true

        for (permission in Permissions) {
            val res = context.checkCallingOrSelfPermission(permission)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if(permission === android.Manifest.permission.READ_EXTERNAL_STORAGE || permission ===
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE){
                    continue
                }
            }
            if (res != PackageManager.PERMISSION_GRANTED)
                hasAllPermisisons = false
        }
        return hasAllPermisisons
    }


    fun DisplayActivity() {
        viewModel.getAllSongs()

        // observe song list, if it has songs, go ahead
        viewModel.songsList.observe(this, {
            if(viewModel.isDataReady.value==true) {
                val startAct = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(startAct)
                this.finish()
                viewModel.isDataReady.value = false
                return@observe
            }
            if (!viewModel.songsList.value.isNullOrEmpty()) {
                Handler().postDelayed({
                    val startAct = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(startAct)
                    this.finish()},1500)
                // if it doesn't have songs, fetch them, listen fro completion and then observe list again
            } else {
                viewModel.init()
                viewModel.isDataReady.observe(this, {
                    if (viewModel.isDataReady.value == true) {
                        viewModel.getAllSongs()
                    }
                })
            }
        })


    }

}


