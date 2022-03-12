package com.apps.kunalfarmah.echo.util

import android.media.MediaPlayer
import androidx.annotation.Keep
import com.apps.kunalfarmah.echo.model.Songs

@Keep
object MediaUtils {
     var mediaPlayer:MediaPlayer = MediaPlayer()
     var songsList:ArrayList<Songs> = ArrayList()
     var currInd: Int = -1
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