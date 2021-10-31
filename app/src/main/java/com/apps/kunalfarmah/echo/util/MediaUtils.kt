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

     fun getDuration(): Int{
          return try{
               mediaPlayer.duration
          }catch (e: Exception){
               0
          }
     }

     fun getCurrentPosition(): Int{
          return try{
               mediaPlayer.currentPosition
          }catch (e: Exception){
               0
          }
     }
}