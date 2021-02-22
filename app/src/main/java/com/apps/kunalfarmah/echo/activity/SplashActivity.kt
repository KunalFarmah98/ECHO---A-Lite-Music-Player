package com.apps.kunalfarmah.echo.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apps.kunalfarmah.echo.online.OnlineActivity
import com.apps.kunalfarmah.echo.R

class SplashActivity : AppCompatActivity() {

    private var permission_String = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
            android.Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (MainActivity.Statified.firstrun == false)
            setContentView(R.layout.activity_splash)
        else {
            DisplayActivity_noSplash()
            return
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            permission_String = arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.ACCESS_MEDIA_LOCATION)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if (!hasPermissions(this@SplashActivity, *permission_String)) {
            ActivityCompat.requestPermissions(this@SplashActivity, permission_String, 1001)
            } else {
                if (MainActivity.Statified.firstrun == false)
                    DisplayActivity()
            }
        }
        else{
            if (!hasPermissions(this@SplashActivity, *permission_String)) {
                ActivityCompat.requestPermissions(this@SplashActivity, permission_String, 131)
            } else {
                if (MainActivity.Statified.firstrun == false)
                    DisplayActivity()
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            130 -> {
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                ) {
                    DisplayActivity()
                } else {
                    Toast.makeText(this@SplashActivity, "Please Grant All the Permissions To Continue", Toast.LENGTH_SHORT).show()
                    this.finish()
                }

            }
            1001 -> {
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED
                ) {
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
            if (res != PackageManager.PERMISSION_GRANTED)
                hasAllPermisisons = false
        }
        return hasAllPermisisons
    }


    fun DisplayActivity() {

        var pref: SharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        var value = pref.getString("mode", "offline")
        if (value.equals("offline")) {
            Handler().postDelayed({
                val startAct = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(startAct)
                this.finish()
            }, 1500)
        } else {
            Handler().postDelayed({
                val startAct = Intent(this@SplashActivity, OnlineActivity::class.java)
                startActivity(startAct)
                this.finish()
            }, 1500)
        }

    }
    fun DisplayActivity_noSplash() {
        val startAct = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(startAct)
        this.finish()
    }

}


