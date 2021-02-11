package com.apps.kunalfarmah.echo.activities

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
import com.apps.kunalfarmah.echo.Online.OnlineActivity
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
        }
        if (!hasPermissions(this@SplashActivity, *permission_String)) {
            ActivityCompat.requestPermissions(this@SplashActivity, permission_String, 130)
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                DisplayActivity()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_MEDIA_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@SplashActivity, arrayOf(android.Manifest.permission.ACCESS_MEDIA_LOCATION), 1001)
        } else {
            if (MainActivity.Statified.firstrun == false)
                DisplayActivity()
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
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                        DisplayActivity()

                } else {
                    Toast.makeText(this@SplashActivity, "Please Grant All the Permissions To Continue", Toast.LENGTH_SHORT).show()
                    this.finish()
                }

                return
            }
            1001 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    DisplayActivity()
                else {
                    Toast.makeText(this@SplashActivity, "Please Grant All the Permissions To Continue", Toast.LENGTH_SHORT).show()
                    this.finish()
                }
                return
            }
            else -> {
                Toast.makeText(this@SplashActivity, "Something Went Wrong", Toast.LENGTH_SHORT).show()
                this.finish()
                return
            }
        }
    }

    fun hasPermissions(context: Context, vararg Permissions: String): Boolean {          //vararg converts array to arguments


        var hasAllPermisisons = true

        for (permission in Permissions) {                                      // if it has granted all permisisons continiue or return false
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


//        Handler().postDelayed({
        val startAct = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(startAct)
        this.finish()
//        }, 1500)

    }

}


