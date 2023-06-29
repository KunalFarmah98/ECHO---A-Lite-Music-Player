package com.apps.kunalfarmah.echo.util

import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.media3.session.SessionToken
import com.apps.kunalfarmah.echo.App
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.fragment.AlbumTracksFragment
import com.apps.kunalfarmah.echo.fragment.FavoriteFragment
import com.apps.kunalfarmah.echo.fragment.MainScreenFragment
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment
import com.apps.kunalfarmah.echo.model.Songs
import com.apps.kunalfarmah.echo.service.EchoNotification
import com.apps.kunalfarmah.echo.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.firebase.crashlytics.FirebaseCrashlytics


@Keep
object MediaUtils {
     var mediaPlayer = ExoPlayer.Builder(App.context).build()
     private val sessionToken = SessionToken(App.context, ComponentName(App.context, PlaybackService::class.java))
     lateinit var controllerFuture: ListenableFuture<MediaController>
     lateinit var controller: MediaController
     var isAlbumPlaying = false
     var isAllSongsPLaying = false
     var isFavouritesPlaying = false
     var currAlbum = -1L
     var isShuffle = AppUtil.getAppPreferences(App.context).getBoolean(Constants.SHUFFLE, false)

     init {
          AppUtil.getAppPreferences(App.context).registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
               if (key.equals(Constants.SHUFFLE)) {
                    val state = sharedPreferences?.getBoolean(key, false)
                    PlaybackService.mInstance?.setCustomLayoutForShuffle(state)
                    SongPlayingFragment.Staticated.setSeekButtonsControl()
               }
          }
          var audioAttributes = AudioAttributes.Builder()
                  .setUsage(C.USAGE_MEDIA)
                  .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                  .build()
          if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
               controllerFuture = MediaController.Builder(App.context, MediaUtils.sessionToken).buildAsync()
               controllerFuture.addListener(
                       {
                            controller = controllerFuture.get()
                            // call playback command methods on the controller like `controller.play()`
                       },
                       MoreExecutors.directExecutor()
               )
          }
          mediaPlayer.setAudioAttributes(audioAttributes, true)
          mediaPlayer.addListener(object : Player.Listener {
               override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                         Player.STATE_READY -> {
                              SongPlayingFragment.Staticated.processInformation()
                              updateCurrentSongIndex()
                              mediaPlayer.play()
                              val mediaMetadata = mediaPlayer.currentMediaItem?.mediaMetadata
                              SongPlayingFragment.Staticated.updateViews(mediaMetadata?.title.toString(), mediaMetadata?.artist.toString(), getBitmap(mediaMetadata?.artworkData))
                              setCurrentSong(mediaMetadata)
                              if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
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
                    super.onMediaMetadataChanged(mediaMetadata)
                    SongPlayingFragment.sharedPreferences!!.edit().putBoolean(Constants.LOOP, false).apply()
                    SongPlayingFragment.Statified.loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
                    updateCurrentSongIndex()
                    if (mediaMetadata.title == null && mediaMetadata.albumArtist == null && mediaMetadata.albumTitle == null) {
                         val index = mediaPlayer.currentMediaItemIndex
                         val currSong = songsList[index]
                         val metaData = MediaMetadata.Builder().setAlbumArtist(currSong.artist
                                 ?: "").setAlbumTitle(currSong.album
                                 ?: "").setTitle(currSong.songTitle ?: "").build()
                         setCurrentSong(metaData)
                         SongPlayingFragment.Staticated.updateViews(metaData.title.toString(), metaData.artist.toString(), null)
                    } else if (mediaMetadata.title != null) {
                         setCurrentSong(mediaMetadata)
                         SongPlayingFragment.Staticated.updateViews(mediaMetadata.title.toString(), mediaMetadata.artist.toString(), getBitmap(mediaMetadata.artworkData))
                    }
               }

               override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    super.onPlayWhenReadyChanged(playWhenReady, reason)
                    if(reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM)
                         return
                    if (playWhenReady) {
                         if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                              var play = Intent(App.context, EchoNotification::class.java)
                              play.action = Constants.ACTION.CHANGE_TO_PAUSE
                              App.context?.startService(play)
                         }
                         SongPlayingFragment.Staticated.updateButton("play")
                         SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
                    } else {
                         if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                              var play = Intent(App.context, EchoNotification::class.java)
                              play.action = Constants.ACTION.CHANGE_TO_PLAY
                              App.context?.startService(play)
                         }
                         SongPlayingFragment.Staticated.updateButton("pause")
                         SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.play_icon)
                    }
                    BottomBarUtils.updatePlayPause()
               }

               override fun onPlayerError(error: PlaybackException) {
                    FirebaseCrashlytics.getInstance().log("Player Error: ${error.javaClass.name} ${error.message ?: ""}")
                    super.onPlayerError(error)
               }
          })
          mediaPlayer.shuffleModeEnabled = isShuffle
     }

     fun updateCurrentSongIndex() {
          if(currInd >= 0){
               MainScreenFragment.mInstance?.mainScreenAdapter?.notifyItemChanged(currInd)
               FavoriteFragment.mInstance?.favouriteAdapter?.notifyItemChanged(currInd)
               AlbumTracksFragment.mInstance?.tracksAdapter?.notifyItemChanged(currInd)
          }
          currInd = mediaPlayer.currentMediaItemIndex
          MainScreenFragment.mInstance?.mainScreenAdapter?.notifyItemChanged(currInd)
          FavoriteFragment.mInstance?.favouriteAdapter?.notifyItemChanged(currInd)
          AlbumTracksFragment.mInstance?.tracksAdapter?.notifyItemChanged(currInd)
     }

     var songsList:ArrayList<Songs> = ArrayList()
     var allSongsList: ArrayList<Songs> = ArrayList()
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

     fun getBitmap(artworkData: ByteArray?): Bitmap?{
          var bitmap: Bitmap ?= null
          artworkData.let {
               if(it!=null){
                    bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
               }
          }
          return bitmap
     }

     fun setMediaItems(){
          mediaItemsList.clear()
          songsList.forEach {
               val metadata = MediaMetadata.Builder()
                       .setTitle(it.songTitle)
                       .setAlbumTitle(it.album)
                       .setArtist(it.artist)
                       .build()
               mediaItemsList.add(
                       MediaItem.Builder()
                         .setUri(it.songData)
                         .setMediaMetadata(metadata)
                         .build()
               )
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
          if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
               if (currSong == null)
                    return -1
               return songsList?.indexOf(currSong)!!
          }
          else{
               return mediaPlayer.currentMediaItemIndex
          }
     }

     fun setCurrentSong(metadata: MediaMetadata?) {
          if(metadata != null && metadata.title != null) {
               val index = mediaPlayer.currentMediaItemIndex
               SongHelper.currentSongHelper.songTitle = metadata?.title.toString()
               SongHelper.currentSongHelper.songArtist = metadata?.artist.toString()
               SongHelper.currentSongHelper.album = metadata?.albumTitle.toString()
               SongHelper.currentSongHelper.songAlbum = songsList[index].songAlbum
               SongHelper.currentSongHelper.currentPosition = index
               SongHelper.currentSongHelper.songId = songsList[index].songID
               SongHelper.currentSongHelper.songpath = songsList[index].songData
               SongHelper.currentSongHelper.albumArt = getBitmap(metadata.artworkData)
          }
     }
}