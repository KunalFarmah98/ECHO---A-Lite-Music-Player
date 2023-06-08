package com.apps.kunalfarmah.echo.util

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import androidx.annotation.Keep
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerNotificationManager
import com.apps.kunalfarmah.echo.App
import com.apps.kunalfarmah.echo.EchoNotification
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.adapter.PlayerDescriptionAdapter
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment
import com.apps.kunalfarmah.echo.model.Songs
import com.apps.kunalfarmah.echo.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.firebase.crashlytics.FirebaseCrashlytics


@Keep
object MediaUtils {
     var mediaPlayer = ExoPlayer.Builder(App.context).build()
     var mediaSession: MediaSession? = null
     lateinit var playerNotificationManager :PlayerNotificationManager
     lateinit var controllerFuture: ListenableFuture<MediaController>
     lateinit var controller: MediaController
     val sessionToken = SessionToken(App.context, ComponentName(App.context, PlaybackService::class.java))
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
                              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                   controllerFuture = MediaController.Builder(App.context, sessionToken).buildAsync()
                                   controllerFuture.addListener(
                                           {
                                                controller = controllerFuture.get()
                                                // call playback command methods on the controller like `controller.play()`
                                           },
                                           MoreExecutors.directExecutor()
                                   )
                                   setCurrentSong()
                              }

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

               override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    SongPlayingFragment.Staticated.updateTextViews(mediaMetadata.title.toString(), mediaMetadata.artist.toString())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                         setCurrentSong()
                    }
                    super.onMediaMetadataChanged(mediaMetadata)
               }

               override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    BottomBarUtils.updatePlayPause()
                    if(reason == Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST || reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY || reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS) {
                         if (playWhenReady) {
                              SongPlayingFragment.Staticated.updateButton("play")
                              SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
                         } else {
                              SongPlayingFragment.Staticated.updateButton("pause")
                              SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.play_icon)
                         }
                    }
                    super.onPlayWhenReadyChanged(playWhenReady, reason)
               }

               override fun onPlayerError(error: PlaybackException) {
                    FirebaseCrashlytics.getInstance().log("Player Error: ${error.javaClass.name} ${error.message ?: ""}")
                    playerNotificationManager.setPlayer(null)
                    super.onPlayerError(error)
               }
          })
     }

     var songsList:ArrayList<Songs> = ArrayList()
     var mediaItemsList = ArrayList<MediaItem>()
     var currInd: Int = -1
     var currSong: Songs? = null
     fun isMediaPlayerPlaying(): Boolean{
          return try{
               mediaPlayer.isPlaying
          }catch (e: Exception){
               false
          }
     }

     fun setMediaItems(){
          songsList.forEach {
               mediaItemsList.add(MediaItem.fromUri(it.songData))
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

     fun setCurrentSong() {
          val metadata = mediaPlayer.currentMediaItem?.mediaMetadata
          SongHelper.currentSongHelper.songTitle = metadata?.title.toString()
          SongHelper.currentSongHelper.songArtist = metadata?.artist.toString()
          SongHelper.currentSongHelper.album = metadata?.albumTitle.toString()
          SongHelper.currentSongHelper.albumArt = metadata?.artworkUri
     }
}