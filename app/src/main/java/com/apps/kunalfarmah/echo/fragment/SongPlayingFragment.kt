package com.apps.kunalfarmah.echo.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.view.*
import android.widget.*
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.apps.kunalfarmah.echo.*
import com.apps.kunalfarmah.echo.adapter.MainScreenAdapter
import com.apps.kunalfarmah.echo.database.EchoDatabase
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.getAlbumart
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.mLastShakeTime
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.mSensorListener
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.mSensorManager
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.onSongComplete
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.playPrevious
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.processInformation
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.reuestAudiofocus
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.updateTextViews
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.ALbumArt
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.art
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.audioVisualization
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.currentPosition
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.currentSongHelper
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.fab
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.favoriteContent
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.fetchSongs
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.glView
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.inform
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.loopbutton
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.mediaPlayer
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.myActivity
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.nextbutton
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.playpausebutton
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.previousbutton
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.seekBar
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.shufflebutton
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.updateSongTime
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.wasPlaying
import com.apps.kunalfarmah.echo.model.Songs
import com.apps.kunalfarmah.echo.util.Constants
import com.apps.kunalfarmah.echo.util.CurrentSongHelper
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.FileDescriptor
import java.util.*
import java.util.concurrent.TimeUnit

class SongPlayingFragment : Fragment() {

    var play: Boolean = false
    var repeat:SharedPreferences?=null
    var shuffle: SharedPreferences?=null


//    var receiver:BroadcastReceiver?=null


    @SuppressLint("StaticFieldLeak")
/*Here you may wonder that why did we create two objects namely Statified and Staticated respectively
    * These objects are created as the variables and functions will be used from another class
    * Now, the question is why did we make two different objects and not one single object
    * This is because we created the Statified object which contains all the variables and
    * the Staticated object which contain all the functions*/



    object Statified {

        val TAG = "SongPLayingFragment"
        var myActivity: Activity? = null
        var mediaPlayer: MediaPlayer? = null
        var inform:Boolean=false
        var wasPlaying = false
        var mediaSession: MediaSession ?= null

        var favoriteContent: EchoDatabase? = null


        var songTitle: TextView? = null
        var songArtist: TextView? = null
        var startTime: TextView? = null
        var endTime: TextView? = null

        var seekBar: SeekBar? = null

        var playpausebutton: ImageButton? = null
        var previousbutton: ImageButton? = null
        var nextbutton: ImageButton? = null
        var loopbutton: ImageButton? = null
        var shufflebutton: ImageButton? = null

        var ALbumArt : ImageView? = null
        var fab: ImageButton? = null
        var art: ImageButton? = null

        var currentPosition: Int = 0
        var currentSongHelper: CurrentSongHelper? = null

        var fetchSongs: ArrayList<Songs>? = null

        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null


        /**
         * creating an object to allow multi threading
         */


        var updateSongTime = object : Runnable {

            override fun run() {

                /*Retrieving the current time position of the media player*/

                if (mediaPlayer != null) {

                    val getCurrent = mediaPlayer?.currentPosition

                    /*The start time is set to the current position of the song
                * The TimeUnit class changes the units to minutes and milliseconds and applied to the string
                * The %d:%d is used for formatting the time strings as 03:45 so that it appears like time*/

                    var seconds = TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong()))

                    if (seconds >= 10) {

                        startTime?.text = String.format("%d:%d",

                                TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong()),

                                TimeUnit.MILLISECONDS.toSeconds(getCurrent.toLong()) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong())))

                    } else if (seconds < 10) {
                        startTime?.text = String.format("%d:0%d",

                                TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong()),

                                TimeUnit.MILLISECONDS.toSeconds(getCurrent.toLong()) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong())))


                    }
                    seekBar?.progress = getCurrent.toInt()

                    /*Since updating the time at each second will take a lot of processing, so we perform this task on the different thread using Handler*/
                    Handler().postDelayed(this, 1000)
                }
            }
        }


    }


    object Staticated {

        lateinit var sharedPreferences: SharedPreferences
        lateinit var currSong: Songs

        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"


        /*Sensor Variables*/
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null
        var MY_PREFS_NAME = "ShakeFeature"
        var mLastShakeTime:Long? =0

         fun getAlbumart(album_id: Long): Bitmap? {
            var bm: Bitmap? = null
            try {
                val sArtworkUri: Uri = Uri
                        .parse("content://media/external/audio/albumart")
                val uri: Uri = ContentUris.withAppendedId(sArtworkUri, album_id)
                val pfd: ParcelFileDescriptor? = myActivity!!.contentResolver
                        .openFileDescriptor(uri, "r")
                if (pfd != null) {
                    val fd: FileDescriptor = pfd.fileDescriptor
                    bm = BitmapFactory.decodeFileDescriptor(fd)
                }
            } catch (e: java.lang.Exception) {
            }
            return bm
        }
        
        fun setData(){
            currSong = Songs(songID = currentSongHelper?.songId!!, songTitle = currentSongHelper?.songTitle!!, album = currentSongHelper?.album!!, artist = currentSongHelper?.songArtist!!,
                    songAlbum = currentSongHelper?.songAlbum!!, dateAdded = 0L, songData = "")
        }

        /*The function playPrevious() is used to play the previous song again*/
        fun playPrevious(check: String) {


            /*Decreasing the current position by 1 to get the position of the previous song*/
            if (check.equals("PlayNextNormal", true)) {
                currentPosition = currentPosition - 1

            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var n  = fetchSongs?.size?.minus(1) as Int
                if(n==0) {
                    currentPosition = 0
                }
                else {
                    var randomObject = Random()                                                              // initialising a random object of the random class
                    var randomPosition = randomObject.nextInt(fetchSongs?.size?.minus(1) as Int)                // setting range of random to size+1
                    currentPosition = randomPosition
                }
            }

            /*If the current position becomes less than 1, we make it 0 as there is no index as -1*/
            if (currentPosition == -1) {
                currentPosition = 0
            }
            if (currentSongHelper?.isPlaying as Boolean) {
                playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
            } else {
                playpausebutton?.setBackgroundResource(R.drawable.play_icon)
            }
            currentSongHelper?.isLoop = false

            /*Similar to the playNext() function defined above*/
            var nextSong = fetchSongs?.get(currentPosition)
            currentSongHelper?.songpath = nextSong?.songData
            currentSongHelper?.songTitle = nextSong?.songTitle
            currentSongHelper?.songArtist = nextSong?.artist
            currentSongHelper?.songAlbum = nextSong?.songAlbum
            currentSongHelper?.album = nextSong?.album
            currentSongHelper?.songId = nextSong?.songID as Long

            updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

            Statified.mediaPlayer?.reset()
            try {
                Statified.mediaPlayer?.setDataSource(myActivity as Activity, Uri.parse(currentSongHelper?.songpath))
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                processInformation(Statified.mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }



            if (favoriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                fab?.setBackgroundResource(R.drawable.favorite_on)
            } else {
                fab?.setBackgroundResource(R.drawable.favorite_off)
            }

            playpausebutton?.setBackgroundResource(R.drawable.pause_icon)

            MainScreenFragment.Staticated.setTitle()
            MainScreenFragment.Staticated.setArtist()

            FavoriteFragment.Staticated.setTitle()
            FavoriteFragment.Staticated.setArtist()

            AlbumTracksFragment.setTitle()
            AlbumTracksFragment.setArtist()
            AlbumTracksFragment.setAlbumArt()

            OfflineAlbumsFragment.setTitle()
            OfflineAlbumsFragment.setArtist()
            OfflineAlbumsFragment.setAlbumArt()


            var play = Intent(myActivity, EchoNotification::class.java)
            play.action = Constants.ACTION.PREV_UPDATE
            play.putExtra("title", currentSongHelper?.songTitle)
            play.putExtra("artist", currentSongHelper?.songArtist)
            play.putExtra("album", currentSongHelper?.songAlbum)
            myActivity?.startService(play)
//            setData()
           }

        fun previous_song() {
            if (currentSongHelper?.isShuffle as Boolean) {
                playPrevious("PlayNextLikeNormalShuffle")
            } else {
                playPrevious("PlayNextNormal")
            }
        }


        /*Function to handle the event where the song completes playing*/
        fun onSongComplete() {
//            else {
            /*If shuffle was on then play a random next song*/
            if (currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
                currentSongHelper?.isPlaying = true
            } else {

                /*If loop was ON, then play the same song again*/
                if (myActivity!!.getSharedPreferences(MY_PREFS_LOOP, MODE_PRIVATE).getBoolean("feature", false) as Boolean) {
                    currentSongHelper?.isPlaying = true
                    var nextSong = fetchSongs?.get(currentPosition)
                    currentSongHelper?.currentPosition = currentPosition
                    currentSongHelper?.songpath = nextSong?.songData
                    currentSongHelper?.songTitle = nextSong?.songTitle
                    currentSongHelper?.songArtist = nextSong?.artist
                    currentSongHelper?.songAlbum = nextSong?.songAlbum
                    currentSongHelper?.album = nextSong?.album
                    currentSongHelper?.songId = nextSong?.songID as Long

                    updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)


                    Statified.mediaPlayer?.reset()

                    try {
                        Statified.mediaPlayer?.setDataSource(myActivity as Activity, Uri.parse(currentSongHelper?.songpath))
                        Statified.mediaPlayer?.prepare()
                        Statified.mediaPlayer?.start()
                        processInformation(Statified.mediaPlayer as MediaPlayer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {

                    /*If loop was OFF then normally play the next song*/
                    playNext("PlayNextNormal")
                    currentSongHelper?.isPlaying = true
                }


                if (favoriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                    fab?.setBackgroundResource(R.drawable.favorite_on)
                } else {
                    fab?.setBackgroundResource(R.drawable.favorite_off)
                }
            }
//            }

            // preventing next song from playing if activity was destroyed

            if (MainScreenFragment.Statified.noNext == false){
                previous_song()
                MainScreenFragment.Statified.noNext = true
            }

            else if(FavoriteFragment.Statified.noNext == false){
                previous_song()
                FavoriteFragment.Statified.noNext = true
            }


            var play = Intent(myActivity, EchoNotification::class.java)
            play.action = Constants.ACTION.NEXT_UPDATE
            play.putExtra("title", currentSongHelper?.songTitle)
            play.putExtra("artist", currentSongHelper?.songArtist)
            play.putExtra("album", currentSongHelper?.songAlbum)

            try {
                myActivity?.startService(play)
//                setData()
            }
            catch (e: Exception){ }
        }


        val focusChangeListener = object : AudioManager.OnAudioFocusChangeListener {
            override fun onAudioFocusChange(focusChange: Int) {
//            val am = myActivity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                when (focusChange) {
                    (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) ->
                        // Lower the volume while ducking.
                        mediaPlayer?.setVolume(0.2f, 0.2f)
                    (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) -> {
                        if (Statified.mediaPlayer!!.isPlaying) {
                            wasPlaying = true
                            mediaPlayer?.pause()
                            playpausebutton?.setBackgroundResource(R.drawable.play_icon)

                            var play = Intent(myActivity, EchoNotification::class.java)
                            play.action = Constants.ACTION.CHANGE_TO_PLAY
                            myActivity?.startService(play)
                        }

                    }
                    (AudioManager.AUDIOFOCUS_LOSS) -> {
                        if (mediaPlayer!!.isPlaying) {

                            wasPlaying = true
                            if (MainScreenAdapter.Statified.stopPlayingCalled) {
                                // it means we started the song from within the app so don't pause it
                                MainScreenAdapter.Statified.stopPlayingCalled = false
                            } else {

//                                wasPlaying = true
                                mediaPlayer?.pause()
                                playpausebutton?.setBackgroundResource(R.drawable.play_icon)

                                var play = Intent(myActivity, EchoNotification::class.java)
                                play.action = Constants.ACTION.CHANGE_TO_PLAY
                                myActivity?.startService(play)
//                    val component = ComponentName(this, MediaControlReceiver::class.java)
//                    am.unregisterMediaButtonEventReceiver(component)
                            }
                        }
                    }
                    (AudioManager.AUDIOFOCUS_GAIN) -> {

                        if (wasPlaying) {
                            wasPlaying = false

                            // Return the volume to normal and resume if paused.
                            mediaPlayer?.setVolume(1f, 1f)
                            mediaPlayer?.start()
                            playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
                            var play = Intent(myActivity, EchoNotification::class.java)
                            play.action = Constants.ACTION.CHANGE_TO_PAUSE
                            myActivity?.startService(play)
                        }
                    }
                    else -> {
                    }
                }
            }
        }


        @SuppressLint("NewApi")
        fun reuestAudiofocus(): Int {

                val am = myActivity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        //Request audio focus for playback
            // old method
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
                val result = am.requestAudioFocus(Staticated.focusChangeListener,
                        // Use the music stream.
                        AudioManager.STREAM_MUSIC,
                        // Request permanent focus.
                        AudioManager.AUDIOFOCUS_GAIN)

                return result
            }
            // from O onwards
            else {
                var focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                    setAudioAttributes(AudioAttributes.Builder().run {
                        setUsage(AudioAttributes.USAGE_GAME)
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        build()
                    })
                    setAcceptsDelayedFocusGain(true)
                    setOnAudioFocusChangeListener(focusChangeListener)
                    build()
                }

                return am.requestAudioFocus(focusRequest)
            }

        }

        @SuppressLint("UseCompatLoadingForDrawables")
        fun updateTextViews(songtitle: String, songartist: String) {

            var songtitleupdted = songtitle
            var songartistupdted = songartist

            if (songtitle.equals("<unknown>", true)) {
                songtitleupdted = "Unknown"
            }
            if (songartist.equals("<unknown>", true)) {
                songartistupdted = "Unknown"
            }
            Statified.songTitle?.text = songtitleupdted
            Statified.songArtist?.text = songartistupdted

            var img = getAlbumart(currentSongHelper?.songAlbum!!.toLong())
            if(img==null)
            ALbumArt?.setImageDrawable(myActivity!!.resources.getDrawable(R.drawable.now_playing_bar_eq_image))
            else ALbumArt?.setImageBitmap(img)

        }


        /*function used to update the time*/
        fun processInformation(mediaPlayer: MediaPlayer) {

            /*Obtaining the final time*/
            val finalTime = mediaPlayer.duration

            /*Obtaining the current position*/
            val startingTime = mediaPlayer.currentPosition

            seekBar?.max = finalTime

            var seconds_start = TimeUnit.MILLISECONDS.toSeconds(startingTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startingTime.toLong()))

            var seconds_end = TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()))

            if (seconds_start >= 10) {

                /*Here we format the time and set it to the start time text*/
                Statified.startTime?.text = String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(startingTime.toLong()),
                        TimeUnit.MILLISECONDS.toSeconds(startingTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startingTime.toLong())))

                if (seconds_end >= 10) {

                    /*Similar to above is done for the end time text*/
                    Statified.endTime?.text = String.format("%d:%d",
                            TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                            TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))
                } else {
                    Statified.endTime?.text = String.format("%d:0%d",
                            TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                            TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))
                }


            } else if (seconds_start < 10) {

                /*Here we format the time and set it to the start time text*/
                Statified.startTime?.text = String.format("%d:0%d",
                        TimeUnit.MILLISECONDS.toMinutes(startingTime.toLong()),
                        TimeUnit.MILLISECONDS.toSeconds(startingTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startingTime.toLong())))

                if (seconds_end >= 10) {

                    /*Similar to above is done for the end time text*/
                    Statified.endTime?.text = String.format("%d:%d",
                            TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                            TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))
                } else {
                    Statified.endTime?.text = String.format("%d:0%d",
                            TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                            TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))
                }
            }


            /*Seekbar has been assigned this time so that it moves according to the time of song*/
            seekBar?.progress = startingTime

            /*Now this task is synced with the update song time object*/
            Handler().postDelayed(updateSongTime, 1000)
        }

        fun upddateButton(Mode: String) {

            if (Mode.equals("pause", true)) {
                playpausebutton?.setBackgroundResource(R.drawable.play_icon)
                currentSongHelper?.isPlaying = false

            } else if (Mode.equals("play", true)) {
                playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
                currentSongHelper?.isPlaying = true

            }
        }


        fun playNext(check: String) {

            /*Let this one sit for a while, We'll explain this after the next section where we will be teaching to add the next and previous functionality*/
            if (check.equals("PlayNextNormal", true)) {
                currentPosition = currentPosition + 1

            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var n  = fetchSongs?.size?.plus(1) as Int
                if(n==0) {
                    currentPosition = 0
                }
                else {
                    var randomObject = Random()                                                              // initialising a random object of the random class
                    var randomPosition = randomObject.nextInt(fetchSongs?.size?.plus(1) as Int)                // setting range of random to size+1
                    currentPosition = randomPosition
                }
            }
            if (currentPosition == fetchSongs?.size) {    // if the currentposition exceeds the size, start over
                currentPosition = 0
            }
            var nextSong = fetchSongs?.get(currentPosition)
            currentSongHelper?.songpath = nextSong?.songData
            currentSongHelper?.songTitle = nextSong?.songTitle
            currentSongHelper?.songArtist = nextSong?.artist
            currentSongHelper?.songAlbum = nextSong?.songAlbum
            currentSongHelper?.album = nextSong?.album
            if(null==currentSongHelper?.songId || null==nextSong?.songID) {
                return
            }
            else
                currentSongHelper?.songId = nextSong?.songID as Long

            updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

            Statified.mediaPlayer?.reset()   // resetting the media player once a song completes or next is clicked

            try {
                Statified.mediaPlayer?.setDataSource(myActivity as Activity, Uri.parse(currentSongHelper?.songpath))
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                processInformation(Statified.mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            MainScreenFragment.Staticated.setTitle()
            MainScreenFragment.Staticated.setArtist()

            FavoriteFragment.Staticated.setTitle()
            FavoriteFragment.Staticated.setArtist()

            AlbumTracksFragment.setTitle()
            AlbumTracksFragment.setArtist()
            AlbumTracksFragment.setAlbumArt()

            OfflineAlbumsFragment.setTitle()
            OfflineAlbumsFragment.setArtist()
            OfflineAlbumsFragment.setAlbumArt()

            if (favoriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                fab?.setBackgroundResource(R.drawable.favorite_on)
            } else {
                fab?.setBackgroundResource(R.drawable.favorite_off)
            }
//            setData()
        }

    }




    var mAcceleration: Float = 0f


    /**
     * creating a broadcast receiver to register earphones unplugging
     *
     */

    private val mNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Statified.mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer?.pause()
                inform=true
                playpausebutton?.setBackgroundResource(R.drawable.play_icon)

                var play = Intent(context, EchoNotification::class.java)
                play.action = Constants.ACTION.CHANGE_TO_PLAY
                activity?.startService(play)

                Toast.makeText(context, "Headphones Unplugged", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private val mCallingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Statified.mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer?.pause()
                playpausebutton?.setBackgroundResource(R.drawable.play_icon)

                var play = Intent(context, EchoNotification::class.java)
                play.action = Constants.ACTION.CHANGE_TO_PLAY
                activity?.startService(play)

//                Toast.makeText(context, "Headphones Unplugged", Toast.LENGTH_SHORT).show()
            }
        }
    }
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment


        //only recreating the view if it is not present beforehand

        var view = view

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        }

        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.GONE

//        activity?.title = "Now Playing"
        setHasOptionsMenu(true)

//        receiver=CaptureBroadcast()

        ALbumArt = view?.findViewById(R.id.art)

        Statified.songTitle = view?.findViewById(R.id.songTitle)
        Statified.songTitle?.isSelected = true
        Statified.songArtist = view?.findViewById(R.id.songArtist)
        Statified.songArtist?.isSelected = true
        Statified.startTime = view?.findViewById(R.id.startTime)
        Statified.endTime = view?.findViewById(R.id.endTime)

        seekBar = view?.findViewById(R.id.seekbar)

        playpausebutton = view?.findViewById(R.id.playpausebutton)
        previousbutton = view?.findViewById(R.id.previousbutton)
        nextbutton = view?.findViewById(R.id.nextbutton)
        loopbutton = view?.findViewById(R.id.loopButton)
        shufflebutton = view?.findViewById(R.id.shuffleButton)


        /*Linking it with the view*/
        fab = view?.findViewById(R.id.favouriteButton)
        art = view?.findViewById(R.id.showArtButton)

        /*Fading the favorite icon*/
        fab?.alpha = 0.8f
        art?.alpha = 0.8f

        glView = view?.findViewById(R.id.visualizer_view)

        Statified.seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromuser: Boolean) {
                if (fromuser && Statified.mediaPlayer != null) {
                    Statified.mediaPlayer?.seekTo(progress)
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) {
                        var play = Intent(myActivity, EchoNotification::class.java)
                        play.action = Constants.ACTION.STARTFOREGROUND_ACTION
                        play.putExtra("title", currentSongHelper?.songTitle)
                        play.putExtra("artist", currentSongHelper?.songArtist)
                        play.putExtra("album", currentSongHelper?.songAlbum)
                        myActivity?.startService(play)
                    }
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Statified.mediaSession = MediaSession(requireContext(), "EchoNotification")
        }

        return view


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*Sensor service is activate when the fragment is created*/
        Staticated.mSensorManager = myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        /*Default values*/
        mAcceleration = 0.0f
        /*We take earth's gravitational value to be default, this will give us good results*/
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.clear()   // clearing any previous menus
        inflater.inflate(R.menu.song_playing_menu, menu)

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val item: MenuItem? = menu.findItem(R.id.action_redirect)
        item?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_redirect -> {
                // redirecting the user to the activity from which they came
                myActivity?.onBackPressed()
                return false

            }
        }
        return false
    }


    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // you can extract AudioVisualization interface for simplifying things
        audioVisualization = glView as AudioVisualization
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity

    }


    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        myActivity = activity
    }


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onActivityCreated(savedInstanceState: Bundle?) {


        // recreating everything again only if it didn't happen before


        super.onActivityCreated(savedInstanceState)


        /*Initialising the params of the current song helper object*/
        favoriteContent = EchoDatabase(myActivity)
        currentSongHelper = CurrentSongHelper()
        currentSongHelper?.isPlaying = true
        currentSongHelper?.isLoop = false
        currentSongHelper?.isShuffle = false


        var path: String? = null   // to get the args of the bundle
        var _songTitle: String? = null
        var _songArtist: String? = null
        var _songId: Long? = null
        var _songAlbum:Long?=null
        var _album:String?=null

        try {
            path = arguments?.getString("path")
            _songArtist = arguments?.getString("songArtist")
            _songTitle = arguments?.getString("songTitle")
            _songAlbum = arguments?.getLong("songAlbum")
//            var id = arguments?.getLong("SongID")
            _songId = arguments?.getLong("SongID")
            _album = arguments?.getString("album")


            /*Here we fetch the received bundle data for current position and the list of all songs*/
            currentPosition = arguments!!.getInt("songPosition")
            fetchSongs = arguments?.getParcelableArrayList("songData")

            /*Now store the song details to the current song helper object so that they can be used later*/
            currentSongHelper?.songpath = path
            currentSongHelper?.songTitle = _songTitle
            currentSongHelper?.songArtist = _songArtist
            currentSongHelper?.songId = _songId
            currentSongHelper?.songAlbum = _songAlbum
            currentSongHelper?.album = _album
            currentSongHelper?.currentPosition = currentPosition

            ALbumArt?.setImageBitmap(getAlbumart(currentSongHelper?.songAlbum!!.toLong()))
            ALbumArt?.visibility=View.GONE

            // updating the textViews as soon as the song is changed and loaded

            updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)


        } catch (e: Exception) {
            e.printStackTrace()
        }

        var fromFavbotomBar = arguments?.get("FavBottomBar")  as? String
        var fromMainbottomBar = arguments?.get("MainBottomBar")  as? String
        var fromAlbumbottomBar = arguments?.get("AlbumsBottomBar")  as? String
        var fromAlbumSongsbottomBar = arguments?.get("AlbumSongsBottomBar")  as? String


        if (fromFavbotomBar != null) {
            myActivity?.title = "Now Playing"
            Statified.mediaPlayer = FavoriteFragment.Statified.mediaPlayer
        } else if (fromMainbottomBar != null) {
            myActivity?.title = "Now Playing"
            Statified.mediaPlayer = MainScreenFragment.Statified.mediaPlayer
        }
        else if (fromAlbumbottomBar != null) {
            myActivity?.title = "Now Playing"
            Statified.mediaPlayer = OfflineAlbumsFragment.mediaPlayer
        }
        else if (fromAlbumSongsbottomBar != null) {
            myActivity?.title = "Now Playing"
            Statified.mediaPlayer = AlbumTracksFragment.mediaPlayer
        }else {

            // set up media player for default
            myActivity?.title = "Now Playing"

            Statified.mediaPlayer = MediaPlayer()
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            //stopPlaying()

            try {

                //setting the data source for the media player with the help of uri
                Statified.mediaPlayer?.setDataSource(path)
                Statified.mediaPlayer?.prepare()

            } catch (e: Exception) {
                e.printStackTrace()
            }




//            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // other app had stopped playing song now , so u can do u stuff now .

            if(reuestAudiofocus()== AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            Statified.mediaPlayer?.start()
//            }


        }

        // precess all the information at the start of the song
        processInformation(Statified.mediaPlayer as MediaPlayer)

        if (currentSongHelper?.isPlaying as Boolean && mediaPlayer?.isPlaying as Boolean) {
            playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playpausebutton?.setBackgroundResource(R.drawable.play_icon)
        }



        Statified.mediaPlayer?.setOnCompletionListener {
            onSongComplete()
        }

        clickHandler()

        /**
         *  set visualiser helper
         *  */

        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(myActivity as Context, 0)
        audioVisualization?.linkTo(visualizationHandler)


        /**
         *  getting the shared preferences for shuffle set by the song
         */

        var prefsForShuffle = myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)

        /*Here we extract the value of preferences and check if shuffle was ON or not*/
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {

            /*if shuffle was found activated, then we change the icon color and tun loop OFF*/
            currentSongHelper?.isShuffle = true
            currentSongHelper?.isLoop = false
            shufflebutton?.setBackgroundResource(R.drawable.shuffle_icon)
            loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
        } else {


            /*Else default is set*/
            currentSongHelper?.isShuffle = false
            shufflebutton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }


        /**
         *  getting the shared preferences for loop set by the song
         */

        var prefsForLoop = myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)

        /*Here we extract the value of preferences and check if loop was ON or not*/
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {

            /*If loop was activated we change the icon color and shuffle is turned OFF */
            currentSongHelper?.isShuffle = false
            currentSongHelper?.isLoop = true
            shufflebutton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            loopbutton?.setBackgroundResource(R.drawable.loop_icon)
        } else {
            /*Else defaults are used*/
            loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
            currentSongHelper?.isLoop = false
        }


        /*Here we check that if the song playing is a favorite, then we show a red colored heart indicating favorite else only the heart boundary
       * This action is performed whenever a new song is played, hence this will done in the playNext(), playPrevious() and onSongComplete() methods*/
        if (favoriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            fab?.setBackgroundResource(R.drawable.favorite_on)
        } else {
            fab?.setBackgroundResource(R.drawable.favorite_off)
        }


        // register reciever for unplugging earphones

        var filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        myActivity?.registerReceiver(mNoisyReceiver, filter)

//        var filter2 = IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL)
//        myActivity?.registerReceiver(mCallingReceiver, filter2)


    }

    override fun onResume() {
        super.onResume()
        Staticated.mSensorManager?.registerListener(Staticated.mSensorListener,
                Staticated.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)
        if(null== mediaPlayer)
            mediaPlayer = MediaPlayer()
        audioVisualization?.onResume()
    }

    override fun onPause() {
        audioVisualization?.onPause()

        /*When fragment is paused, we remove the sensor to prevent the battery drain*/

        super.onPause()
    }


    // if user leaves the screen destroy it
    override fun onDestroyView() {

        super.onDestroyView()

        try {

            audioVisualization?.release()
            myActivity?.unregisterReceiver(mNoisyReceiver)
            myActivity?.unregisterReceiver(mCallingReceiver)
            mSensorManager?.unregisterListener(mSensorListener)

        }
        catch (e: Exception){}
    }

    /*A new click handler function is created to handle all the click functions in the song playing fragment*/
    fun clickHandler() {

        /*Here we handle the click of the favorite icon
       * When the icon was clicked, if it was red in color i.e. a favorite song then we remove the song from favorites*/
        fab?.setOnClickListener {
            if (favoriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                fab?.setBackgroundResource(R.drawable.favorite_off)
                favoriteContent?.deleteFavourite(currentSongHelper?.songId?.toInt() as Int)

                /*Toast is prompt message at the bottom of screen indicating that an action has been performed*/
                Toast.makeText(myActivity, "Removed from Favorites", Toast.LENGTH_SHORT).show()
            } else {

                /*If the song was not a favorite, we then add it to the favorites using the method we made in our database*/
                fab?.setBackgroundResource(R.drawable.favorite_on)
                var album = currentSongHelper?.album
                if(null==album){
                    album=""
                }
                var songAlbum = currentSongHelper?.songAlbum
                if(null==songAlbum){
                    songAlbum = 0
                }
                favoriteContent?.storeAsFavorite(currentSongHelper?.songId?.toInt(), currentSongHelper?.songArtist, currentSongHelper?.songTitle, currentSongHelper?.songpath, songAlbum,
                        album)
                Toast.makeText(myActivity, "Added to Favorites", Toast.LENGTH_SHORT).show()
            }
        }

        art?.setOnClickListener{
            if(glView?.visibility==View.VISIBLE){
                glView?.visibility = View.GONE
                ALbumArt?.visibility= View.VISIBLE
            }
            else {
                glView?.visibility=View.VISIBLE
                ALbumArt?.visibility=View.GONE
            }
        }


        shufflebutton?.setOnClickListener {

            var shuffle = myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
            var isShuffle = shuffle!!.getBoolean("feature", false)
            var repeat = myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)

            // turning off shuffle
            if(isShuffle){
                shuffle.edit().putBoolean("feature", false).apply()
                shufflebutton!!.setBackgroundResource(R.drawable.shuffle_white_icon)
                currentSongHelper?.isShuffle = false
            }
            // turning on shuffle, repeat must be disabled now
            else{
                shuffle.edit().putBoolean("feature", true).apply()
                shufflebutton!!.setBackgroundResource(R.drawable.shuffle_icon)
                repeat!!.edit().putBoolean("feature", false).apply()
                loopbutton!!.setBackgroundResource(R.drawable.loop_white_icon)
                currentSongHelper?.isShuffle = true
                currentSongHelper?.isLoop = false
            }

        }


        nextbutton?.setOnClickListener {
            currentSongHelper?.isPlaying = true
            playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
            play = true

            if (currentSongHelper?.isLoop as Boolean) {
                currentSongHelper?.isLoop = false

                /*If the loop was on we turn it off*/
                loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
            }

            if (currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
            } else {
                playNext("PlayNextNormal")
            }

            currentSongHelper?.isLoop = false

//            var play = Intent(context, EchoNotification::class.java)
//            play.setAction(Constants.ACTION.NEXT_UPDATE);
//            play.putExtra("title", currentSongHelper?.songTitle)
//            play.putExtra("artist", currentSongHelper?.songArtist)
//            activity?.startService(play)
        }



        previousbutton?.setOnClickListener {


            /*We set the player to be playing by setting isPlaying to be true*/
            currentSongHelper?.isPlaying = true
            playpausebutton?.setBackgroundResource(R.drawable.play_icon)

            play = true
            /*First we check if the loop is on or not*/
            if (currentSongHelper?.isLoop as Boolean) {
                currentSongHelper?.isLoop = false

                /*If the loop was on we turn it off*/
                loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
            }

            /*After all of the above is done we then play the previous song using the playPrevious() function*/

            if (currentSongHelper?.isShuffle as Boolean) {
                playPrevious("PlayNextLikeNormalShuffle")
            } else {
                playPrevious("PlayNextNormal")
            }

//            var play = Intent(context, EchoNotification::class.java)
//            play.setAction(Constants.ACTION.PREV_UPDATE);
//            play.putExtra("title", currentSongHelper?.songTitle)
//            play.putExtra("artist", currentSongHelper?.songArtist)
//            activity?.startService(play)


        }





        loopbutton?.setOnClickListener {

            var shuffle = myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
            var repeat = myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)
            var isRepeat = repeat!!.getBoolean("feature", false)

            if (isRepeat) {
                currentSongHelper?.isLoop = false
                loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
                repeat.edit().putBoolean("feature", false).apply()

            } else {
                currentSongHelper?.isLoop = true
                currentSongHelper?.isShuffle = false
                loopbutton?.setBackgroundResource(R.drawable.loop_icon)
                shufflebutton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                shuffle!!.edit().putBoolean("feature", false).apply()
                repeat.edit().putBoolean("feature", true).apply()
            }
        }

        /*Here we handle the click event on the play/pause button*/
        playpausebutton?.setOnClickListener {

            /*if the song is already playing and then play/pause button is tapped
            * then we pause the media player and also change the button to play button*/
            if (Statified.mediaPlayer?.isPlaying as Boolean) {
                Statified.mediaPlayer?.pause()
                play = false
                currentSongHelper?.isPlaying = false
                playpausebutton?.setBackgroundResource(R.drawable.play_icon)

                var play = Intent(context, EchoNotification::class.java)
                play.action = Constants.ACTION.CHANGE_TO_PLAY
                activity?.startService(play)

                /*If the song was not playing the, we start the music player and
                * change the image to pause icon*/
            } else {
                if (reuestAudiofocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    Statified.mediaPlayer?.start()
                    MainScreenAdapter.Statified.stopPlayingCalled=true
                    play = true
                    currentSongHelper?.isPlaying = true
                    playpausebutton?.setBackgroundResource(R.drawable.pause_icon)

                    var play = Intent(activity, EchoNotification::class.java)
                    play.action = Constants.ACTION.CHANGE_TO_PAUSE
                    activity?.startService(play)
                }

            }
        }
    }


    /*This function handles the shake events in order to change the songs when we shake the phone*/
    fun bindShakeListener() {

        /*The sensor listener has two methods used for its implementation i.e. OnAccuracyChanged() and onSensorChanged*/
        Staticated.mSensorListener = object : SensorEventListener {

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

                /*We do not need to check or work with the accuracy changes for the sensor*/
            }

            override fun onSensorChanged(event: SensorEvent) {

                /*We need this onSensorChanged function
                * This function is called when there is a new sensor event*/
                /*The sensor event has 3 dimensions i.e. the x, y and z in which the changes can occur*/
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val curTime = System.currentTimeMillis()
                /*Now lets see how we calculate the changes in the acceleration*/
                /*Now we shook the phone so the current acceleration will be the first to start with*/
                mAccelerationLast = mAccelerationCurrent

                /*Since we could have moved the phone in any direction, we calculate the Euclidean distance to get the normalized distance*/
                mAccelerationCurrent = Math.sqrt(((x * x + y * y + z * z).toDouble())).toFloat()

                /*Delta gives the change in acceleration*/
                val delta = mAccelerationCurrent - mAccelerationLast

                /*Here we calculate the lower filter
                * The written below is a formula to get it*/
                mAcceleration = mAcceleration * 0.9f + delta

                /*We obtain a real number for acceleration
                * and we check if the acceleration was noticeable, considering 12 here*/
                if ((curTime - Staticated.mLastShakeTime!!) >1000 && mAcceleration > 12) {

                    /*If the accel was greater than 12 we change the song, given the fact our shake to change was active*/
                    val prefs = myActivity?.getSharedPreferences(Staticated.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)

                    val shuffle = myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
                    val isshuffled = shuffle?.getBoolean("feature", false)

                    mLastShakeTime = curTime

                    if(!mediaPlayer!!.isPlaying)
                        return


                    if (isAllowed as Boolean && isshuffled as Boolean != true) {
                        playNext("PlayNextNormal")
                    } else if (isAllowed && isshuffled as Boolean)
                        playNext("PlayNextLikeNormalShuffle")
                }
            }

        }
    }

    fun playNext(check: String) {

        /*Let this one sit for a while, We'll explain this after the next section where we will be teaching to add the next and previous functionality*/
        if (check.equals("PlayNextNormal", true)) {
            currentPosition = currentPosition + 1

        } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
            var randomObject = Random()                                                              // initialising a random object of the random class
            var randomPosition = randomObject.nextInt(fetchSongs?.size?.plus(1) as Int)                // setting range of random to size+1
            currentPosition = randomPosition
        }
        if (currentPosition == fetchSongs?.size) {    // if the currentposition exceeds the size, start over
            currentPosition = 0
        }
        var nextSong = fetchSongs?.get(currentPosition)
        currentSongHelper?.songpath = nextSong?.songData
        currentSongHelper?.songTitle = nextSong?.songTitle
        currentSongHelper?.songArtist = nextSong?.artist
        currentSongHelper?.songAlbum = nextSong?.songAlbum
        currentSongHelper?.album = nextSong?.album
        currentSongHelper?.songId = nextSong?.songID as Long

        updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

        Statified.mediaPlayer?.reset()   // resetting the media player once a song completes or next is clicked

        try {
            Statified.mediaPlayer?.setDataSource(myActivity as Activity, Uri.parse(currentSongHelper?.songpath))
            Statified.mediaPlayer?.prepare()
            Statified.mediaPlayer?.start()
            processInformation(Statified.mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (favoriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            fab?.setBackgroundResource(R.drawable.favorite_on)
        } else {
            fab?.setBackgroundResource(R.drawable.favorite_off)
        }


        MainScreenFragment.Staticated.setTitle()
        MainScreenFragment.Staticated.setArtist()

        FavoriteFragment.Staticated.setTitle()
        FavoriteFragment.Staticated.setArtist()

        AlbumTracksFragment.setTitle()
        AlbumTracksFragment.setArtist()
        AlbumTracksFragment.setAlbumArt()

        OfflineAlbumsFragment.setTitle()
        OfflineAlbumsFragment.setArtist()
        OfflineAlbumsFragment.setAlbumArt()

        var play = Intent(myActivity, EchoNotification::class.java)
        play.action = Constants.ACTION.NEXT_UPDATE
        play.putExtra("title", currentSongHelper?.songTitle)
        play.putExtra("artist", currentSongHelper?.songArtist)
        play.putExtra("album", currentSongHelper?.songAlbum)

        myActivity?.startService(play)
    }


    fun previous() {
        if (currentSongHelper?.isShuffle as Boolean) {
            playPrevious("PlayNextLikeNormalShuffle")
        } else {
            playPrevious("PlayNextNormal")
        }
    }


    fun next() {
        if (currentSongHelper?.isShuffle as Boolean) {
            playNext("PlayNextLikeNormalShuffle")
        } else {
            playNext("PlayNextNormal")
        }
    }


    fun playorpause(): Boolean {
        var play = false

        if (Statified.mediaPlayer?.isPlaying as Boolean) {
            Statified.mediaPlayer?.pause()
            currentSongHelper?.isPlaying = false
            play = false
            playpausebutton?.setBackgroundResource(R.drawable.play_icon)

            /*If the song was not playing then, we start the music player and
            * change the image to pause icon*/
        } else {
            if(reuestAudiofocus()== AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            Statified.mediaPlayer?.start()
            currentSongHelper?.isPlaying = true
            play = true
            playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
        }

        return play

    }

    fun getMediaPlayer():MediaPlayer?{
        return Statified.mediaPlayer
    }

    fun unregister(){
        mSensorManager?.unregisterListener(mSensorListener)
    }




}