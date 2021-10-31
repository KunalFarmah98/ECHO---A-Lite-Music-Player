package com.apps.kunalfarmah.echo.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import com.apps.kunalfarmah.echo.App
import com.apps.kunalfarmah.echo.EchoNotification
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.apps.kunalfarmah.echo.adapter.MainScreenAdapter
import com.apps.kunalfarmah.echo.databinding.BottomBarBinding
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment
import com.apps.kunalfarmah.echo.util.MediaUtils.mediaPlayer
import com.apps.kunalfarmah.echo.util.SongHelper.currentSongHelper
import com.bumptech.glide.Glide
import java.lang.Exception

object BottomBarUtils {
    var trackPosition = mediaPlayer.currentPosition
    lateinit var bottomBarBinding: BottomBarBinding


    fun bottomBarSetup(activity: Activity, main: MainActivity, fragmentManager: FragmentManager, bottomBarBinding: BottomBarBinding) {
        bottomBarClickHandler(activity, main, fragmentManager, bottomBarBinding)
        this.bottomBarBinding = bottomBarBinding
        if (!mediaPlayer.isPlaying && !isMyServiceRunning(
                EchoNotification::class.java,
                App.context
            )
        ) {
            bottomBarBinding.bottomBar.visibility = View.GONE
            return
        }
        bottomBarBinding.bottomBar.visibility = View.VISIBLE
        if (mediaPlayer.isPlaying) {
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
        setAlbumArt(currentSongHelper.songAlbum)

    }

    private fun bottomBarClickHandler(
        myActivity: Activity,
        main: MainActivity,
        fragmentManager: FragmentManager,
        bottomBarBinding: BottomBarBinding
    ) {

        bottomBarBinding.bottomBar.setOnClickListener {
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", currentSongHelper.songArtist)
            args.putString("songTitle", currentSongHelper.songTitle)
            args.putString("path", currentSongHelper.songpath)
            args.putLong("SongID", currentSongHelper.songId!!)
            args.putLong("songAlbum", currentSongHelper.songAlbum!!)
            args.putInt("songPosition", currentSongHelper.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)
            args.putBoolean("fromBottomBar",true)
            songPlayingFragment.arguments = args
            fragmentManager.beginTransaction()
                .replace(R.id.details_fragment, songPlayingFragment)
                .addToBackStack("SongPlayingFragment")
                .commit()
        }

        bottomBarBinding.playPause.setOnClickListener {

            if (mediaPlayer.isPlaying) {

                mediaPlayer.pause()
                bottomBarBinding.playPause.setImageDrawable(myActivity.resources.getDrawable(R.drawable.play_icon))
                var play = Intent(App.context, EchoNotification::class.java)
                play.action = Constants.ACTION.CHANGE_TO_PLAY
                myActivity.startService(play)
                SongPlayingFragment.Staticated.updateButton("pause")
            } else {
                MainScreenAdapter.Statified.stopPlayingCalled = true
                if (!main.getnotify_val()) {
                    trackPosition =
                        mediaPlayer.currentPosition
                    mediaPlayer.seekTo(trackPosition)
                    if (SongPlayingFragment.Staticated.requestAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                        mediaPlayer.start()

                    bottomBarBinding.playPause.setImageDrawable(App.context.resources.getDrawable(R.drawable.pause_icon))
                    var serviceIntent = Intent(myActivity, EchoNotification::class.java)

                    serviceIntent.putExtra("title", bottomBarBinding.songTitle.text.toString())
                    serviceIntent.putExtra("artist", bottomBarBinding.songArtist.text.toString())
                    serviceIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION


                    myActivity.startService(serviceIntent)

                    var play = Intent(myActivity, EchoNotification::class.java)
                    play.action = Constants.ACTION.CHANGE_TO_PAUSE
                    myActivity.startService(play)
                    SongPlayingFragment.Staticated.updateButton("play")

                } else if (main.getnotify_val()) {


                    /*If the music was already paused and we then click on the button
                * it plays the song from the same position where it was paused
                * and change the button to pause button*/
                    trackPosition =
                        mediaPlayer.currentPosition as Int  // current postiton where the player as stopped
                    mediaPlayer.seekTo(trackPosition)
                    if (SongPlayingFragment.Staticated.requestAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                        mediaPlayer.start()
                    bottomBarBinding.playPause.setImageDrawable(myActivity.resources.getDrawable(R.drawable.pause_icon))

                    var play = Intent(myActivity, EchoNotification::class.java)
                    play.action = Constants.ACTION.CHANGE_TO_PAUSE
                    myActivity.startService(play)
                    SongPlayingFragment.Staticated.updateButton("play")
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
            bottomBarBinding.songImg.setImageDrawable(
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
                .into(bottomBarBinding.songImg)
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

    fun setTitle() {
        if (null != currentSongHelper.songTitle && null != currentSongHelper)
            bottomBarBinding.songTitle.text = currentSongHelper?.songTitle
    }

    fun setArtist() {
        if (null != currentSongHelper.songArtist && null != currentSongHelper) {
            var artist = currentSongHelper.songArtist
            if (artist.equals("<unknown>", ignoreCase = true))
                bottomBarBinding.songArtist.visibility = View.GONE
            else {
                bottomBarBinding.songArtist.visibility = View.VISIBLE
                bottomBarBinding.songArtist.text = artist
            }
        }
    }

    fun setAlbumArt() {
        if (null != bottomBarBinding.songImg && null != currentSongHelper) {
            val sArtworkUri: Uri = Uri
                .parse("content://media/external/audio/albumart")
            val uri: Uri = ContentUris.withAppendedId(sArtworkUri, currentSongHelper.songAlbum!!)
            if (currentSongHelper.songAlbum!! < 0 || null == uri || uri.toString().isEmpty())
                bottomBarBinding.songImg.setImageResource(R.drawable.echo_icon)
            else
                bottomBarBinding.songImg.setImageURI(uri)

            if (null == bottomBarBinding.songImg.drawable) {
                bottomBarBinding.songImg.setImageResource(R.drawable.echo_icon)
            }
        }
    }

    fun updatePlayPause(){
        if(mediaPlayer.isPlaying){
            bottomBarBinding.playPause.setImageDrawable(App.context.resources.getDrawable(R.drawable.pause_icon))
        }
        else{
            bottomBarBinding.playPause.setImageDrawable(App.context.resources.getDrawable(R.drawable.play_icon))
        }
    }
}