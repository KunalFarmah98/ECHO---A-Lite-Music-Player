package com.apps.kunalfarmah.echo.util

import android.media.MediaPlayer
import androidx.annotation.Keep

@Keep
object MediaUtils {
     var mediaPlayer:MediaPlayer = MediaPlayer()
     fun isMediaPlayerPlaying(): Boolean{
          return try{
               mediaPlayer.isPlaying
          }catch (e: Exception){
               false
          }
     }
}