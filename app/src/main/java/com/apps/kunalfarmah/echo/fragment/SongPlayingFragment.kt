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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.view.*
import android.widget.*
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.media3.common.Player
import com.apps.kunalfarmah.echo.*
import com.apps.kunalfarmah.echo.activity.SongPlayingActivity
import com.apps.kunalfarmah.echo.adapter.MainScreenAdapter
import com.apps.kunalfarmah.echo.adapter.MainScreenAdapter.Statified.stopPlayingCalled
import com.apps.kunalfarmah.echo.database.EchoDatabase
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.mLastShakeTime
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.mSensorListener
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.mSensorManager
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.processInformation
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.updateViews
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.albumArt
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.art
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.audioVisualization
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.controlsView
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.currentPosition
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.fab
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.favoriteContent
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.fetchSongs
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.glView
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.loopbutton
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.myActivity
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.nextbutton
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.playpausebutton
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.previousbutton
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.seekBar
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.shufflebutton
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Statified.updateSongTime
import com.apps.kunalfarmah.echo.model.Songs
import com.apps.kunalfarmah.echo.service.EchoNotification
import com.apps.kunalfarmah.echo.util.BottomBarUtils
import com.apps.kunalfarmah.echo.util.Constants
import com.apps.kunalfarmah.echo.util.CurrentSongHelper
import com.apps.kunalfarmah.echo.util.MediaUtils
import com.apps.kunalfarmah.echo.util.MediaUtils.mediaPlayer
import com.apps.kunalfarmah.echo.util.SongHelper.currentSongHelper
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import java.io.FileDescriptor
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class SongPlayingFragment : Fragment() {

    var play: Boolean = false
    var showVisualizer = true

    companion object {
        var sharedPreferences: SharedPreferences? = null

        fun playNext(shuffle: Boolean) {

            sharedPreferences!!.edit().putBoolean(Constants.LOOP, false).apply()
            loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
            //BottomBarUtils.updatePlayPause()

            try {
                mediaPlayer.prepare()
            } catch (ignored: java.lang.Exception) {
            }
            MediaUtils.mediaPlayer.repeatMode = Player.REPEAT_MODE_OFF
            MediaUtils.mediaPlayer.seekToNextMediaItem()

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                var play = Intent(myActivity, EchoNotification::class.java)
                play.action = Constants.ACTION.NEXT_UPDATE
                play.putExtra("title", currentSongHelper.songTitle)
                play.putExtra("artist", currentSongHelper.songArtist)
                play.putExtra("album", currentSongHelper.songAlbum)
                myActivity?.startService(play)
            }
        }

        /*The function playPrevious() is used to play the previous song again*/
        fun playPrevious(shuffle: Boolean) {

            sharedPreferences!!.edit().putBoolean(Constants.LOOP, false).apply()
            loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
            //BottomBarUtils.updatePlayPause()
            try {
                mediaPlayer.prepare()
            } catch (ignored: java.lang.Exception) {
            }
            mediaPlayer.repeatMode = Player.REPEAT_MODE_OFF
            mediaPlayer.seekToPreviousMediaItem()

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                var play = Intent(myActivity, EchoNotification::class.java)
                play.action = Constants.ACTION.PREV_UPDATE
                play.putExtra("title", currentSongHelper.songTitle)
                play.putExtra("artist", currentSongHelper.songArtist)
                play.putExtra("album", currentSongHelper.songAlbum)
                myActivity?.startService(play)
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
/*Here you may wonder that why did we create two objects namely Statified and Staticated respectively
    * These objects are created as the variables and functions will be used from another class
    * Now, the question is why did we make two different objects and not one single object
    * This is because we created the Statified object which contains all the variables and
    * the Staticated object which contain all the functions*/



    object Statified {

        val TAG = "SongPLayingFragment"
        var myActivity: Activity? = null
        var inform: Boolean = false
        var wasPlaying = false

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
        lateinit var shufflebutton: ImageButton

        var albumArt: ImageView? = null
        var fab: ImageView? = null
        var art: ImageView? = null

        var currentPosition: Int = 0

        var fetchSongs: ArrayList<Songs>? = null

        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null
        var controlsView: LinearLayout? = null


        /**
         * creating an object to allow multi threading
         */


        var updateSongTime = object : Runnable {

            override fun run() {

                /*Retrieving the current time position of the media player*/
                try {
                    val getCurrent = mediaPlayer.currentPosition

                    /*The start time is set to the current position of the song
                    * The TimeUnit class changes the units to minutes and milliseconds and applied to the string
                    * The %d:%d is used for formatting the time strings as 03:45 so that it appears like time*/

                    var seconds = TimeUnit.MILLISECONDS.toSeconds(getCurrent.toLong() as Long) -
                            TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    getCurrent.toLong()
                                )
                            )

                    if (seconds > 1) {
                        stopPlayingCalled = false
                    }
                    if (seconds >= 10) {

                        startTime?.text = String.format(
                            "%d:%d",

                            TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong()),

                            TimeUnit.MILLISECONDS.toSeconds(getCurrent.toLong()) -
                                    TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(
                                            getCurrent.toLong()
                                        )
                                    )
                        )

                    } else if (seconds < 10) {
                        startTime?.text = String.format(
                            "%d:0%d",

                            TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong()),

                            TimeUnit.MILLISECONDS.toSeconds(getCurrent.toLong()) -
                                    TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(
                                            getCurrent.toLong()
                                        )
                                    )
                        )


                    }
                    seekBar?.progress = getCurrent.toInt()
                } catch (e: Exception) {
                }

                /*Since updating the time at each second will take a lot of processing, so we perform this task on the different thread using Handler*/
                Handler().postDelayed(this, 1000)
            }
        }


    }


    object Staticated {

        /*Sensor Variables*/
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null
        var MY_PREFS_NAME = "ShakeFeature"
        var mLastShakeTime: Long? = 0

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

        @SuppressLint("UseCompatLoadingForDrawables")
        fun updateViews(songtitle: String?, songartist: String?, artwork: Bitmap? = null) {

            var songtitleupdted = songtitle
            var songartistupdted = songartist

            if (songtitle == null || songtitle == "null" || songtitle.equals("<unknown>", true)) {
                songtitleupdted = "Unknown"
            }
            if (songartist == null || songartist == "null" || songartist.equals("<unknown>", true)) {
                songartistupdted = "Unknown"
            }
            Statified.songTitle?.text = songtitleupdted
            Statified.songArtist?.text = songartistupdted

            setSeekButtonsControl()

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                currentSongHelper.songAlbum.let {
                    if (it != null) {
                        var img = getAlbumart(currentSongHelper.songAlbum!!.toLong())
                        if (img == null) {
                            albumArt?.setImageDrawable(myActivity!!.resources.getDrawable(R.drawable.now_playing_bar_eq_image))
                            glView?.visibility = View.VISIBLE
                            albumArt?.visibility = View.GONE
                            controlsView?.setBackgroundColor(myActivity!!.resources.getColor(R.color.four))
                        } else {
                            albumArt?.setImageBitmap(img)
                            if (myActivity != null) {
                                glView?.visibility = View.GONE
                                albumArt?.visibility = View.VISIBLE
                                controlsView?.setBackgroundColor(myActivity!!.resources.getColor(R.color.colorPrimary))
                            }
                            else{}
                        }
                    } else {
                        albumArt?.setImageDrawable(myActivity!!.resources.getDrawable(R.drawable.now_playing_bar_eq_image))
                        glView?.visibility = View.VISIBLE
                        albumArt?.visibility = View.GONE
                        controlsView?.setBackgroundColor(myActivity!!.resources.getColor(R.color.four))
                    }
                }
            }
            else{
                var img: Bitmap? = null
                if(artwork != null){
                    albumArt?.setImageBitmap(artwork)
                }
                else {
                    if (currentSongHelper.songAlbum != null) {
                        img = getAlbumart(currentSongHelper.songAlbum!!)
                    }
                    if (img == null) {
                        albumArt?.setImageDrawable(myActivity!!.resources.getDrawable(R.drawable.now_playing_bar_eq_image))
                        glView?.visibility = View.VISIBLE
                        albumArt?.visibility = View.GONE
                        controlsView?.setBackgroundColor(myActivity!!.resources.getColor(R.color.four))
                    } else {
                        albumArt?.setImageBitmap(img)
                        if (myActivity != null) {
                            glView?.visibility = View.GONE
                            albumArt?.visibility = View.VISIBLE
                            controlsView?.setBackgroundColor(myActivity!!.resources.getColor(R.color.colorPrimary))
                        }
                    }
                }
            }

            if (favoriteContent?.checkifIdExists(currentSongHelper.songId?.toInt() as Int) as Boolean) {
                fab?.setImageDrawable(myActivity?.resources?.getDrawable(R.drawable.favorite_on))
            } else {
                fab?.setImageDrawable(myActivity?.resources?.getDrawable(R.drawable.favorite_off))
            }

            BottomBarUtils.setTitle(songtitle)
            BottomBarUtils.setArtist(songartist)
            BottomBarUtils.setAlbumArt(artwork)

        }

        fun setSeekButtonsControl(){
            if(!mediaPlayer.hasNextMediaItem()){
                nextbutton?.isEnabled = false
                nextbutton?.alpha = 0.5f
                BottomBarUtils.bottomBarBinding?.next?.visibility = View.GONE
            }
            else{
                nextbutton?.isEnabled = true
                nextbutton?.alpha = 1f
                BottomBarUtils.bottomBarBinding?.next?.visibility = View.VISIBLE
            }

            if(!mediaPlayer.hasPreviousMediaItem()){
                previousbutton?.isEnabled = false
                previousbutton?.alpha = 0.5f
            }
            else{
                previousbutton?.isEnabled = true
                previousbutton?.alpha = 1f
            }
        }


        /*function used to update the time*/
        fun processInformation() {

            /*Obtaining the final time*/
            val finalTime = MediaUtils.getDuration()

            /*Obtaining the current position*/
            val startingTime = MediaUtils.getCurrentPosition()

            seekBar?.max = finalTime.toInt()

            var seconds_start =
                TimeUnit.MILLISECONDS.toSeconds(startingTime) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(startingTime)
                )

            var seconds_end =
                TimeUnit.MILLISECONDS.toSeconds(finalTime) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(finalTime)
                )

            if (seconds_start >= 10) {

                /*Here we format the time and set it to the start time text*/
                Statified.startTime?.text = String.format(
                    "%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(startingTime),
                    TimeUnit.MILLISECONDS.toSeconds(startingTime) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(startingTime)
                    )
                )

                if (seconds_end >= 10) {

                    /*Similar to above is done for the end time text*/
                    Statified.endTime?.text = String.format(
                        "%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(finalTime),
                        TimeUnit.MILLISECONDS.toSeconds(finalTime) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(finalTime)
                        )
                    )
                } else {
                    Statified.endTime?.text = String.format(
                        "%d:0%d",
                        TimeUnit.MILLISECONDS.toMinutes(finalTime),
                        TimeUnit.MILLISECONDS.toSeconds(finalTime) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(finalTime)
                        )
                    )
                }


            } else if (seconds_start < 10) {

                /*Here we format the time and set it to the start time text*/
                Statified.startTime?.text = String.format(
                    "%d:0%d",
                    TimeUnit.MILLISECONDS.toMinutes(startingTime),
                    TimeUnit.MILLISECONDS.toSeconds(startingTime) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(startingTime)
                    )
                )

                if (seconds_end >= 10) {

                    /*Similar to above is done for the end time text*/
                    Statified.endTime?.text = String.format(
                        "%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(finalTime),
                        TimeUnit.MILLISECONDS.toSeconds(finalTime) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(finalTime)
                        )
                    )
                } else {
                    Statified.endTime?.text = String.format(
                        "%d:0%d",
                        TimeUnit.MILLISECONDS.toMinutes(finalTime),
                        TimeUnit.MILLISECONDS.toSeconds(finalTime) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(finalTime)
                        )
                    )
                }
            }

            if (finalTime <= 0) {
                Statified.endTime?.text = "-:--"
            }


            /*Seekbar has been assigned this time so that it moves according to the time of song*/
            seekBar?.progress = startingTime.toInt()

            /*Now this task is synced with the update song time object*/
            Handler().postDelayed(updateSongTime, 1000)
        }

        fun updateButton(Mode: String) {

            if (Mode.equals("pause", true)) {
                playpausebutton?.setBackgroundResource(R.drawable.play_icon)

            } else if (Mode.equals("play", true)) {
                playpausebutton?.setBackgroundResource(R.drawable.pause_icon)

            }
        }
    }

    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = view

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        }

        setHasOptionsMenu(true)

        albumArt = view?.findViewById(R.id.art)

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
        shufflebutton = view?.findViewById(R.id.shuffleButton)!!


        /*Linking it with the view*/
        fab = view?.findViewById(R.id.favouriteButton)
        art = view?.findViewById(R.id.showArtButton)
        art?.visibility = View.VISIBLE

        glView = view?.findViewById(R.id.visualizer_view)

        controlsView = view?.findViewById(R.id.controls_layout);

        Statified.seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromuser: Boolean) {
                if (fromuser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress.toLong())
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        return view


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*Sensor service is activate when the fragment is created*/
        Staticated.mSensorManager =
            myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        /*Default values*/
        mAcceleration = 0.0f
        /*We take earth's gravitational value to be default, this will give us good results*/
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        sharedPreferences = context?.getSharedPreferences(Constants.APP_PREFS, MODE_PRIVATE)
        bindShakeListener()
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
    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // you can extract AudioVisualization interface for simplifying things
        audioVisualization = glView as AudioVisualization

        /*Initialising the params of the current song helper object*/
        favoriteContent = EchoDatabase(myActivity)
        currentSongHelper = CurrentSongHelper()


        var path: String? = null   // to get the args of the bundle
        var _songTitle: String? = null
        var _songArtist: String? = null
        var _songId: Long? = null
        var _songAlbum: Long? = null
        var _album: String? = null
        var fromBottomBar = false
        var fromSearch = false

        //try {
        path = arguments?.getString("path")
        _songArtist = arguments?.getString("songArtist")
        _songTitle = arguments?.getString("songTitle")
        _songAlbum = arguments?.getLong("songAlbum")
        _songId = arguments?.getLong("SongID")
        _album = arguments?.getString("album")

        if (arguments?.getBoolean("fromBottomBar") != null)
            fromBottomBar = arguments?.getBoolean("fromBottomBar")!!

        fromSearch = arguments?.getBoolean("fromSearch", false) == true
        fetchSongs = MediaUtils.songsList


        /*Here we fetch the received bundle data for current position and the list of all songs*/
        currentPosition = if(fromSearch){
            // if playing song from search, we will get index form the existing list and play from the all songs list
            fetchSongs!!.indexOfFirst {
                it.songID == _songId
            }
        }
        else {
            arguments!!.getInt("songPosition")
        }

        //  Now store the song details to the current song helper object so that they can be used later
        currentSongHelper.songpath = path
        currentSongHelper.songTitle = _songTitle
        currentSongHelper.songArtist = _songArtist
        currentSongHelper.songId = _songId
        currentSongHelper.songAlbum = _songAlbum
        currentSongHelper.album = _album
        currentSongHelper.currentPosition = currentPosition

        albumArt?.visibility = View.GONE

        // updating the textViews as soon as the song is changed and loaded

        updateViews(currentSongHelper.songTitle, currentSongHelper.songArtist, currentSongHelper.albumArt)


        if (fromBottomBar) {
            myActivity?.title = "Now Playing"
            if(mediaPlayer.playWhenReady || mediaPlayer.isPlaying){
                playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
            }
            else{
                playpausebutton?.setBackgroundResource(R.drawable.play_icon)
            }
            processInformation()
        } else {
            // set up media player for default
            myActivity?.title = "Now Playing"

        try {
            //setting the data source for the media player with the help of uri
            mediaPlayer.setMediaItems(MediaUtils.mediaItemsList, currentPosition, 0L)
            mediaPlayer.prepare()
        } catch (e: Exception) {
            Toast.makeText(App.context,App.context.resources.getString(R.string.media_playback_failure), Toast.LENGTH_SHORT).show()
            if(activity!=null)
                (activity as SongPlayingActivity).finish()
            return
        }

        }

        clickHandler()

        /**
         *  set visualiser helper
         *  */

        try {
            var visualizationHandler =
                DbmHandler.Factory.newVisualizerHandler(myActivity as Context, 0)
            audioVisualization?.linkTo(visualizationHandler)
        }
        catch (e:java.lang.Exception){
            art?.visibility =  View.GONE
            glView?.visibility = View.GONE
            albumArt?.visibility = View.VISIBLE
            controlsView?.setBackgroundColor(requireContext().resources.getColor(R.color.colorPrimary))
        }


        /**
         *  getting the shared preferences for shuffle set by the song
         */

        if (MediaUtils.isShuffle) {
            /*if shuffle was found activated, then we change the icon color and tun loop OFF*/
            shufflebutton.setBackgroundResource(R.drawable.shuffle_icon)
            sharedPreferences!!.edit().putBoolean(Constants.LOOP, false).apply()
            loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            /*Else default is set*/
            shufflebutton.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        if (sharedPreferences!!.getBoolean(Constants.LOOP, false)) {
            /*If loop was activated we change the icon color and shuffle is turned OFF */
            loopbutton?.setBackgroundResource(R.drawable.loop_icon)
            sharedPreferences!!.edit().putBoolean(Constants.SHUFFLE, false).apply()
            MediaUtils.isShuffle = false
            shufflebutton.setBackgroundResource(R.drawable.shuffle_white_icon)
        } else {
            loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
        }


        /*Here we check that if the song playing is a favorite, then we show a red colored heart indicating favorite else only the heart boundary
       * This action is performed whenever a new song is played, hence this will done in the playNext(), playPrevious() and onSongComplete() methods*/
        if (favoriteContent?.checkifIdExists(currentSongHelper.songId?.toInt() as Int) as Boolean) {
            fab?.setImageDrawable(myActivity?.resources?.getDrawable(R.drawable.favorite_on))
        } else {
            fab?.setImageDrawable(myActivity?.resources?.getDrawable(R.drawable.favorite_off))
        }

        if (arguments?.getBoolean(Constants.WAS_MEDIA_PLAYING, false) == true) {
            activity?.onBackPressed()
        }

    }

    override fun onResume() {
        super.onResume()
        Staticated.mSensorManager?.registerListener(
            Staticated.mSensorListener,
            Staticated.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        if (audioVisualization != null)
            audioVisualization!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (audioVisualization != null)
            audioVisualization?.onPause()
    }


    // if user leaves the screen destroy it
    override fun onDestroyView() {

        super.onDestroyView()

        try {
            if (audioVisualization != null)
                audioVisualization?.release()
            mSensorManager?.unregisterListener(mSensorListener)
        } catch (e: Exception) {
        }
    }

    /*A new click handler function is created to handle all the click functions in the song playing fragment*/
    fun clickHandler() {

        /*Here we handle the click of the favorite icon
       * When the icon was clicked, if it was red in color i.e. a favorite song then we remove the song from favorites*/
        fab?.setOnClickListener {
            if (favoriteContent?.checkifIdExists(currentSongHelper.songId?.toInt() as Int) as Boolean) {
                fab?.setImageDrawable(myActivity?.resources?.getDrawable(R.drawable.favorite_off))
                favoriteContent?.deleteFavourite(currentSongHelper.songId?.toInt() as Int)
                // if we currently playing the favorites, remove this song from the list and the player queue and play the next song
                if(MediaUtils.isFavouritesPlaying && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    if(mediaPlayer.mediaItemCount==1){
                        MediaUtils.currInd = -1
                        FavoriteFragment.mInstance?.favouriteAdapter?.songDetails = ArrayList()
                        FavoriteFragment.mInstance?.favouriteAdapter?.notifyDataSetChanged()
                        return@setOnClickListener
                    }
                    try {
                        // remove from player queue
                        MediaUtils.songsList.removeIf {
                            it.songID == currentSongHelper.songId
                        }.let{
                            if(it) {
                                MediaUtils.setMediaItems()
                                val hadNext = mediaPlayer.hasNextMediaItem()
                                mediaPlayer.setMediaItems(MediaUtils.mediaItemsList, !hadNext)
                                MediaUtils.currInd = mediaPlayer.currentMediaItemIndex
                                FavoriteFragment.mInstance?.favouriteAdapter?.songDetails = MediaUtils.songsList
                                FavoriteFragment.mInstance?.favouriteAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                    catch (e: java.lang.Exception){
                        MediaUtils.currInd = -1
                    }
                }

                /*Toast is prompt message at the bottom of screen indicating that an action has been performed*/
                Toast.makeText(myActivity, "Removed from Favorites", Toast.LENGTH_SHORT).show()
            } else {

                /*If the song was not a favorite, we then add it to the favorites using the method we made in our database*/
                fab?.setImageDrawable(myActivity?.resources?.getDrawable(R.drawable.favorite_on))
                var album = currentSongHelper.album
                if (null == album) {
                    album = ""
                }
                var songAlbum = currentSongHelper.songAlbum
                if (null == songAlbum) {
                    songAlbum = 0
                }
                favoriteContent?.storeAsFavorite(
                    currentSongHelper.songId?.toInt(),
                    currentSongHelper.songArtist,
                    currentSongHelper.songTitle,
                    currentSongHelper.songpath,
                    songAlbum,
                    album
                )
                Toast.makeText(myActivity, "Added to Favorites", Toast.LENGTH_SHORT).show()
            }
        }

        art?.setOnClickListener {
            if (glView?.visibility == View.VISIBLE) {
                glView?.visibility = View.GONE
                albumArt?.visibility = View.VISIBLE
                controlsView?.setBackgroundColor(requireContext().resources.getColor(R.color.colorPrimary))
            } else {
                glView?.visibility = View.VISIBLE
                albumArt?.visibility = View.GONE
                controlsView?.setBackgroundColor(requireContext().resources.getColor(R.color.four))
            }
        }


        shufflebutton.setOnClickListener {

            // turning off shuffle
            if (MediaUtils.isShuffle) {
                MediaUtils.isShuffle = false
                sharedPreferences!!.edit().putBoolean(Constants.SHUFFLE, false).apply()
                shufflebutton!!.setBackgroundResource(R.drawable.shuffle_white_icon)
                sharedPreferences!!.edit().putBoolean(Constants.LOOP, false).apply()
                loopbutton!!.setBackgroundResource(R.drawable.loop_white_icon)
            }
            // turning on shuffle, repeat must be disabled now
            else {
                MediaUtils.isShuffle = true
                sharedPreferences!!.edit().putBoolean(Constants.SHUFFLE, true).apply()
                shufflebutton!!.setBackgroundResource(R.drawable.shuffle_icon)
                sharedPreferences!!.edit().putBoolean(Constants.LOOP, false).apply()
                loopbutton!!.setBackgroundResource(R.drawable.loop_white_icon)
            }

            mediaPlayer.shuffleModeEnabled = MediaUtils.isShuffle

        }


        nextbutton?.setOnClickListener {
            playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
            play = true
            stopPlayingCalled = true
            sharedPreferences!!.edit().putBoolean(Constants.LOOP, false).apply()
            loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
            playNext(MediaUtils.isShuffle)
        }



        previousbutton?.setOnClickListener {
            /*We set the player to be playing by setting isPlaying to be true*/
            playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
            play = true
            stopPlayingCalled = true
            sharedPreferences!!.edit().putBoolean(Constants.LOOP, false).apply()
            loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
            /*After all of the above is done we then play the previous song using the playPrevious() function*/
            playPrevious(MediaUtils.isShuffle)
        }


        loopbutton?.setOnClickListener {

            var isRepeat = sharedPreferences!!.getBoolean(Constants.LOOP, false)
            if (isRepeat) {
                loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
                sharedPreferences!!.edit().putBoolean(Constants.LOOP, false).apply()
                shufflebutton.setBackgroundResource(R.drawable.shuffle_white_icon)
                sharedPreferences!!.edit().putBoolean(Constants.SHUFFLE, false).apply()
                MediaUtils.isShuffle = false
            } else {
                loopbutton?.setBackgroundResource(R.drawable.loop_icon)
                sharedPreferences!!.edit().putBoolean(Constants.LOOP, true).apply()
                shufflebutton.setBackgroundResource(R.drawable.shuffle_white_icon)
                sharedPreferences!!.edit().putBoolean(Constants.SHUFFLE, false).apply()
                MediaUtils.isShuffle = false
            }
            MediaUtils.mediaPlayer.repeatMode = if(isRepeat) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ONE

        }

        /*Here we handle the click event on the play/pause button*/
        playpausebutton?.setOnClickListener {
            BottomBarUtils.updatePlayPause()
            /*if the song is already playing and then play/pause button is tapped
            * then we pause the media player and also change the button to play button*/
            if (MediaUtils.isMediaPlayerPlaying()) {
                mediaPlayer.pause()
                play = false
                playpausebutton?.setBackgroundResource(R.drawable.play_icon)
//                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//                    var play = Intent(context, EchoNotification::class.java)
//                    play.action = Constants.ACTION.CHANGE_TO_PLAY
//                    activity?.startService(play)
//                }

                /*If the song was not playing the, we start the music player and
                * change the image to pause icon*/
            } else {
                try {
                    mediaPlayer.prepare()
                }
                catch (ignored: java.lang.Exception){}
                    mediaPlayer.play()
                    MainScreenAdapter.Statified.stopPlayingCalled = true
                    play = true
                    playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//                    var play = Intent(activity, EchoNotification::class.java)
//                    play.action = Constants.ACTION.CHANGE_TO_PAUSE
//                    activity?.startService(play)
//                }
            }
        }
    }


    /*This function handles the shake events in order to change the songs when we shake the phone*/
    private fun bindShakeListener() {

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
                if ((curTime - Staticated.mLastShakeTime!!) > 1000 && mAcceleration > 12) {

                    /*If the accel was greater than 12 we change the song, given the fact our shake to change was active*/
                    val isAllowed = sharedPreferences!!.getBoolean(Constants.SHAKE_TO_CHANGE, false)

                    mLastShakeTime = curTime

                    if (!MediaUtils.isMediaPlayerPlaying())
                        return

                    if (isAllowed)
                        playNext(MediaUtils.isShuffle)
                }
            }

        }
    }


    fun previous() {
        playPrevious(MediaUtils.isShuffle)
    }


    fun next() {
        playNext(MediaUtils.isShuffle)
    }


    fun playorpause(): Boolean {
        var play = false
        if (MediaUtils.isMediaPlayerPlaying() as Boolean) {
            mediaPlayer.pause()
            play = false
            playpausebutton?.setBackgroundResource(R.drawable.play_icon)

            /*If the song was not playing then, we start the music player and
            * change the image to pause icon*/
        } else {
            mediaPlayer.play()
            MainScreenAdapter.Statified.stopPlayingCalled = true
            play = true
            playpausebutton?.setBackgroundResource(R.drawable.pause_icon)

        }
        BottomBarUtils.updatePlayPause()
        return play
    }

    fun unregister() {
        mSensorManager?.unregisterListener(mSensorListener)
    }


}