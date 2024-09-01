package com.apps.kunalfarmah.echo.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.databinding.ActivitySplashBinding
import com.apps.kunalfarmah.echo.viewModel.SongsViewModel
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: SongsViewModel by viewModels()
    private var permissionString = arrayOf("")
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // using splash screen api for S and above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            // making it overlap the UI of splash activity till it asks for permissions
            installSplashScreen().setKeepOnScreenCondition { true }
        }
        super.onCreate(savedInstanceState)
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {

            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)
            // showing gradient splash on pre Lollipop devices
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        window.statusBarColor =
                            ContextCompat.getColor(applicationContext, R.color.splashStatusBar);
                    }
                }
                catch (_:Exception){
                }
                binding.splashV20.visibility = View.VISIBLE
                binding.card.visibility = View.GONE
            }
            // imitating splash screen api drawing of splash screen from Lollipop till R
            // manually as splash screen api will crop the launcher icon
            else {
                binding.splashV20.visibility = View.GONE
                binding.root.background = ContextCompat.getDrawable(applicationContext, R.color.colorPrimary)
                binding.card.visibility = View.VISIBLE
            }
        }

        handlePermissions()
    }

    private fun handlePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissionString = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.MODIFY_AUDIO_SETTINGS)
            // request RECORD_AUDIO at runtime following rationale
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                permissionString.plus(android.Manifest.permission.RECORD_AUDIO)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissionString = arrayOf(
                android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_MEDIA_LOCATION,
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionString = arrayOf(
                android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                android.Manifest.permission.ACCESS_MEDIA_LOCATION,
                android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasPermissions(this@SplashActivity, *permissionString)) {
                ActivityCompat.requestPermissions(this@SplashActivity, permissionString, 1001)
            } else {
                displayActivity()
            }
        } else {
            if (!hasPermissions(this@SplashActivity, *permissionString)) {
                ActivityCompat.requestPermissions(this@SplashActivity, permissionString, 131)
            } else {
                displayActivity()
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
                    displayActivity()
                } else {
                    Toast.makeText(this@SplashActivity, "Please Grant All the Permissions To Continue", Toast.LENGTH_SHORT).show()
                    this.finish()
                }
            }
            1001 -> {
                if (grantResults.isNotEmpty() && grantedPermissions(grantResults)){
                    displayActivity()
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

    private fun hasPermissions(context: Context, vararg Permissions: String): Boolean {

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


    private fun displayActivity() {
        viewModel.getAllSongs()
        // observe song list, if it has songs, go ahead
        viewModel.songsList.observe(this) {
            if (viewModel.isDataReady.value == true) {
                val startAct = Intent(this@SplashActivity, MainActivity::class.java).apply {
                    `package` = this@SplashActivity.packageName
                }
                startActivity(startAct)
                this.finish()
                viewModel.isDataReady.value = false
                return@observe
            }
            // delay launch only on pre android 12 devices
            Handler(mainLooper).postDelayed({
                val startAct = Intent(this@SplashActivity, MainActivity::class.java).apply {
                    `package` = this@SplashActivity.packageName
                }
                startActivity(startAct)
                this.finish()
            }, if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) 1500 else 0)
        }
    }

}


