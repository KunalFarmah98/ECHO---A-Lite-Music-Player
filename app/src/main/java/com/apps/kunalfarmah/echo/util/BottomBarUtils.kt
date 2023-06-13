package com.apps.kunalfarmah.echo.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import androidx.fragment.app.FragmentManager
import com.apps.kunalfarmah.echo.App
import com.apps.kunalfarmah.echo.EchoNotification
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.apps.kunalfarmah.echo.activity.SongPlayingActivity
import com.apps.kunalfarmah.echo.adapter.MainScreenAdapter
import com.apps.kunalfarmah.echo.databinding.BottomBarBinding
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment
import com.apps.kunalfarmah.echo.util.MediaUtils.mediaPlayer
import com.apps.kunalfarmah.echo.util.SongHelper.currentSongHelper
import com.bumptech.glide.Glide
import java.lang.Exception

@Keep
object BottomBarUtils {
    var bottomBarBinding: BottomBarBinding  ?= null


    fun bottomBarSetup(activity: Activity, main: MainActivity, fragmentManager: FragmentManager, bottomBarBinding: BottomBarBinding) {
        bottomBarClickHandler(activity, main, fragmentManager, bottomBarBinding)
        this.bottomBarBinding = bottomBarBinding
        if (!MediaUtils.isMediaPlayerPlaying() && !isMyServiceRunning(
                EchoNotification::class.java,
                App.context
            )
        ) {
            bottomBarBinding.bottomBar.visibility = View.GONE
            return
        }
        bottomBarBinding.bottomBar.visibility = View.VISIBLE
        if (MediaUtils.isMediaPlayerPlaying()) {
            bottomBarBinding.playPause.setImageDrawable(App.context.resources.getDrawable(R.drawable.pause_icon))
        } else {
            bottomBarBinding.playPause.setImageDrawable(App.context.resources.getDrawable(R.drawable.play_icon))
        }
        bottomBarBinding.songTitle.text = currentSongHelper.songTitle
        var artist = currentSongHelper.songArtist
        if (artist.equals("<unknown>", ignoreCase = true))
            bottomBarBinding.songArtist.visibility = View.GONE
        else
            bottomBarBinding.songArtist.text = artist
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            setAlbumArt(currentSongHelper.songAlbum)
        }
        else{
            loadAlbumArt(currentSongHelper.albumArt)
        }

    }

    private fun bottomBarClickHandler(
        myActivity: Activity,
        main: MainActivity,
        fragmentManager: FragmentManager,
        bottomBarBinding: BottomBarBinding
    ) {

        bottomBarBinding.bottomBar.setOnClickListener {
            var intent = Intent(App.context,SongPlayingActivity::class.java)
            intent.putExtra("songArtist", currentSongHelper.songArtist)
            intent.putExtra("songTitle", currentSongHelper.songTitle)
            intent.putExtra("path", currentSongHelper.songpath)
            intent.putExtra("SongID", currentSongHelper.songId!!)
            intent.putExtra("songAlbum", currentSongHelper.songAlbum!!)
            intent.putExtra("songPosition", currentSongHelper.currentPosition?.toInt() as Int)
            intent.putExtra("fromBottomBar",true)
            MediaUtils.songsList = SongPlayingFragment.Statified.fetchSongs?:ArrayList()
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            App.context.startActivity(intent)
        }

        bottomBarBinding.playPause.setOnClickListener {

            if (MediaUtils.isMediaPlayerPlaying()) {

                mediaPlayer.pause()
                bottomBarBinding.playPause.setImageDrawable(myActivity.resources.getDrawable(R.drawable.play_icon))
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    var play = Intent(App.context, EchoNotification::class.java)
                    play.action = Constants.ACTION.CHANGE_TO_PLAY
                    myActivity.startService(play)
                }
                SongPlayingFragment.Staticated.updateButton("pause")
            } else {
                MainScreenAdapter.Statified.stopPlayingCalled = true
                if (!main.getnotify_val()) {
                    var trackPosition =
                        MediaUtils.getCurrentPosition()
                    mediaPlayer.seekTo(trackPosition.toLong())
                    mediaPlayer.play()

                    bottomBarBinding.playPause.setImageDrawable(App.context.resources.getDrawable(R.drawable.pause_icon))
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        var serviceIntent = Intent(myActivity, EchoNotification::class.java)
                        serviceIntent.putExtra("title", bottomBarBinding.songTitle.text.toString())
                        serviceIntent.putExtra("artist", bottomBarBinding.songArtist.text.toString())
                        serviceIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION
                        myActivity.startService(serviceIntent)

                        var play = Intent(myActivity, EchoNotification::class.java)
                        play.action = Constants.ACTION.CHANGE_TO_PAUSE
                        myActivity.startService(play)
                    }
                    SongPlayingFragment.Staticated.updateButton("play")

                } else if (main.getnotify_val()) {


                    /*If the music was already paused and we then click on the button
                * it plays the song from the same position where it was paused
                * and change the button to pause button*/
                    var trackPosition =
                        MediaUtils.getCurrentPosition()  // current postiton where the player as stopped
                    mediaPlayer.seekTo(trackPosition.toLong())
                    mediaPlayer.play()
                    bottomBarBinding.playPause.setImageDrawable(myActivity.resources.getDrawable(R.drawable.pause_icon))
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        var play = Intent(myActivity, EchoNotification::class.java)
                        play.action = Constants.ACTION.CHANGE_TO_PAUSE
                        myActivity.startService(play)
                        SongPlayingFragment.Staticated.updateButton("play")
                    }
                }
            }
        }
        val shuffle = myActivity.getSharedPreferences(Constants.APP_PREFS, Context.MODE_PRIVATE)

        val isshuffled = shuffle!!.getBoolean(Constants.SHUFFLE, false)

        bottomBarBinding.next.setOnClickListener {
            SongPlayingFragment.playNext(isshuffled)
            bottomBarBinding.playPause.setImageDrawable(myActivity.resources.getDrawable(R.drawable.pause_icon))
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setAlbumArt(songAlbum: Long?) {
        var albumId = songAlbum
        if (albumId == null || albumId <= 0L) {
            bottomBarBinding?.songImg?.setImageDrawable(
                App.context.resources?.getDrawable(
                    R.drawable.echo_icon
                )
            )
            return
        }
        try {
            val sArtworkUri: Uri = Uri
                .parse("content://media/external/audio/albumart")
            val uri: Uri = ContentUris.withAppendedId(sArtworkUri, albumId)
            Glide.with(App.context).load(uri).placeholder(R.drawable.echo_icon)
                .into(bottomBarBinding?.songImg!!)
        } catch (e: Exception) {
        }
    }

    private fun loadAlbumArt(artwork: Bitmap?) {
        if (artwork == null) {
            bottomBarBinding?.songImg?.setImageDrawable(
                    App.context.resources?.getDrawable(
                            R.drawable.echo_icon
                    )
            )
            return
        }
        try {
            Glide.with(App.context).load(artwork).placeholder(R.drawable.echo_icon)
                    .into(bottomBarBinding?.songImg!!)
        } catch (e: Exception) {
        }
    }


    private fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun setTitle(title: String? = null) {
        if(title != null){
            var titleToShow = title
            if (title == "null" || title.equals("<unknown>", true)) {
                titleToShow = "Unknown"
            }
            bottomBarBinding?.songTitle?.text = titleToShow
        }
        else if (null != currentSongHelper.songTitle && null != currentSongHelper) {
            var titleToShow = currentSongHelper?.songTitle
            if (titleToShow == "null" || titleToShow.equals("<unknown>", true)) {
                titleToShow = "Unknown"
            }
            bottomBarBinding?.songTitle?.text = titleToShow
        }
    }

    fun setArtist(artist: String? = null) {
        if(artist != null) {
            if (artist == "null" || artist.equals("<unknown>", ignoreCase = true))
                bottomBarBinding?.songArtist?.visibility = View.GONE
            else {
                bottomBarBinding?.songArtist?.visibility = View.VISIBLE
                bottomBarBinding?.songArtist?.text = artist
            }
        }
        else if (null != currentSongHelper.songArtist && null != currentSongHelper) {
            var artist = currentSongHelper.songArtist
            if (artist == "null" || artist.equals("<unknown>", ignoreCase = true))
                bottomBarBinding?.songArtist?.visibility = View.GONE
            else {
                bottomBarBinding?.songArtist?.visibility = View.VISIBLE
                bottomBarBinding?.songArtist?.text = artist
            }
        }
    }

    fun setAlbumArt(artwork: Bitmap? = null) {
        if(artwork != null){
            bottomBarBinding?.songImg?.setImageBitmap(artwork)
            return
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (null != bottomBarBinding?.songImg && null != currentSongHelper) {
                if(currentSongHelper.songAlbum == null){
                    bottomBarBinding?.songImg?.setImageResource(R.drawable.echo_icon)
                    return
                }
                val sArtworkUri: Uri = Uri
                        .parse("content://media/external/audio/albumart")
                val uri: Uri = ContentUris.withAppendedId(sArtworkUri, currentSongHelper.songAlbum!!)
                if (currentSongHelper.songAlbum!! < 0 || null == uri || uri.toString().isEmpty())
                    bottomBarBinding?.songImg?.setImageResource(R.drawable.echo_icon)
                else
                    bottomBarBinding?.songImg?.setImageURI(uri)

                if (null == bottomBarBinding?.songImg?.drawable) {
                    bottomBarBinding?.songImg?.setImageResource(R.drawable.echo_icon)
                }
            }
        }
        else{
            if (null != bottomBarBinding?.songImg && null != currentSongHelper){
                val bitmap = currentSongHelper.albumArt
                if(bitmap != null)
                    bottomBarBinding?.songImg?.setImageBitmap(bitmap)
                else
                    bottomBarBinding?.songImg?.setImageResource(R.drawable.echo_icon)
            }
        }
    }

    fun updatePlayPause(){
        if(MediaUtils.isMediaPlayerPlaying()){
            bottomBarBinding?.playPause?.setImageDrawable(App.context.resources.getDrawable(R.drawable.pause_icon))
        }
        else{
            bottomBarBinding?.playPause?.setImageDrawable(App.context.resources.getDrawable(R.drawable.play_icon))
        }
    }
}