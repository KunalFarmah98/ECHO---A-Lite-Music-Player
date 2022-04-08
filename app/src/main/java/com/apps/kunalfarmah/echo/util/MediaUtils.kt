package com.apps.kunalfarmah.echo.util

import android.content.Intent
import androidx.annotation.Keep
import com.apps.kunalfarmah.echo.App
import com.apps.kunalfarmah.echo.EchoNotification
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment
import com.apps.kunalfarmah.echo.model.Songs
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.firebase.crashlytics.FirebaseCrashlytics


@Keep
object MediaUtils {
     var mediaPlayer:ExoPlayer = ExoPlayer.Builder(App.context).build()

     init {
          var audioAttributes = AudioAttributes.Builder()
               .setUsage(C.USAGE_MEDIA)
               .setContentType(C.CONTENT_TYPE_MUSIC)
               .build()
          mediaPlayer.setAudioAttributes(audioAttributes,true)
          mediaPlayer.addListener(object : Player.Listener{
               override fun onPlaybackStateChanged(state: Int) {
                    when(state){
                         Player.STATE_ENDED -> SongPlayingFragment.Staticated.onSongComplete()
                         Player.STATE_READY -> {
                              mediaPlayer.play()
                              SongPlayingFragment.Staticated.processInformation()
                              var serviceIntent = Intent(App.context, EchoNotification::class.java)

                              serviceIntent.putExtra("title", SongHelper.currentSongHelper.songTitle)
                              serviceIntent.putExtra("artist", SongHelper.currentSongHelper.songArtist)
                              serviceIntent.putExtra("album", SongHelper.currentSongHelper.songAlbum)

                              serviceIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION

                              // need to start it twice or media controls don't work
                              App.context.startService(serviceIntent)
                              App.context.startService(serviceIntent)
                         }

                    }
                    super.onPlaybackStateChanged(state)
               }

               override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    BottomBarUtils.updatePlayPause()
                    if (playWhenReady) {
                         SongPlayingFragment.Staticated.updateButton("play")
                         SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
                    } else {
                         SongPlayingFragment.Staticated.updateButton("pause")
                         SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.play_icon)
                    }
                    super.onPlayWhenReadyChanged(playWhenReady, reason)
               }

               override fun onPlayerError(error: PlaybackException) {
                    FirebaseCrashlytics.getInstance().log("Player Error: ${error.javaClass.name} ${error.message?:""}")
                    super.onPlayerError(error)
               }
          })
     }
     var songsList:ArrayList<Songs> = ArrayList()
     var currInd: Int = -1
     var currSong: Songs? = null
     fun isMediaPlayerPlaying(): Boolean{
          return try{
               mediaPlayer.isPlaying
          }catch (e: Exception){
               false
          }
     }

     fun getDuration(): Long{
          return try{
               mediaPlayer.duration
          }catch (e: Exception){
               0
          }
     }

     fun getCurrentPosition(): Long{
          return try{
               mediaPlayer.currentPosition
          }catch (e: Exception){
               0
          }
     }

     fun getSongIndex(): Int {
          if(currSong == null)
               return -1
          return songsList?.indexOf(currSong)!!
     }
}