package com.apps.kunalfarmah.echo.util

import android.content.Intent
import android.os.Build
import androidx.annotation.Keep
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import com.apps.kunalfarmah.echo.App
import com.apps.kunalfarmah.echo.EchoNotification
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.adapter.PlayerDescriptionAdapter
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment
import com.apps.kunalfarmah.echo.model.Songs
import com.google.firebase.crashlytics.FirebaseCrashlytics


@Keep
object MediaUtils {
     var mediaPlayer = ExoPlayer.Builder(App.context).build()
     var mediaSession: MediaSession? = null
     lateinit var playerNotificationManager :PlayerNotificationManager
     init {
          var audioAttributes = AudioAttributes.Builder()
                  .setUsage(C.USAGE_MEDIA)
                  .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                  .build()
          mediaPlayer.setAudioAttributes(audioAttributes, true)
          mediaPlayer.addListener(object : Player.Listener {
               override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                         Player.STATE_ENDED -> SongPlayingFragment.Staticated.onSongComplete()
                         Player.STATE_READY -> {
                              mediaPlayer.play()
                              SongPlayingFragment.Staticated.processInformation()
                              if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
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
                    FirebaseCrashlytics.getInstance().log("Player Error: ${error.javaClass.name} ${error.message ?: ""}")
                    playerNotificationManager.setPlayer(null)
                    super.onPlayerError(error)
               }
          })

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
               mediaSession = MediaSession.Builder(App.context, mediaPlayer)
                       .build()
               playerNotificationManager = PlayerNotificationManager.Builder(
                       App.context,
                       Constants.NOTIFICATION_ID.PLAYER_NOTIFICATION_MANAGER,
                       "Echo_Music_ExoPlayer"
               ).setMediaDescriptionAdapter(PlayerDescriptionAdapter()).build()
               playerNotificationManager.setUseFastForwardAction(false)
               playerNotificationManager.setUseRewindAction(false)
               playerNotificationManager.setUseFastForwardActionInCompactView(false)
               playerNotificationManager.setUseRewindActionInCompactView(false)
               playerNotificationManager.setPlayer(mediaPlayer)
               playerNotificationManager.setMediaSessionToken(mediaSession!!.sessionCompatToken)
          }
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