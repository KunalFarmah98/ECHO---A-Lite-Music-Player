package com.apps.kunalfarmah.echo.fragment

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.apps.kunalfarmah.echo.Constants
import com.apps.kunalfarmah.echo.EchoNotification
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.Songs
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.apps.kunalfarmah.echo.adapter.MainScreenAdapter
import com.apps.kunalfarmah.echo.databinding.FragmentAlbumTracksBinding
import com.apps.kunalfarmah.echo.viewModel.SongsViewModel
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class AlbumTracksFragment(id: Long?, name: String) : Fragment() {


    var albumId: Long = id!!
    var albumName = name
    val viewModel: SongsViewModel by viewModels()
    var binding: FragmentAlbumTracksBinding? = null
    var trackPosition: Int = 0
    var song: SongPlayingFragment? = null
    var args: Bundle? = null
    var main: MainActivity? = null

    companion object Statified{
        val TAG = "AlbumTracksFragment"
        var mediaPlayer: MediaPlayer? = null
        var noNext: Boolean = true
        var songAlbum: Long? = null
        var songImg: ImageView? = null
        var songArtist: TextView? = null
        var songTitle: TextView? = null
        fun setTitle() {
            if (null != songTitle && null!= SongPlayingFragment.Statified.currentSongHelper)
                songTitle?.text = SongPlayingFragment.Statified.currentSongHelper?.songTitle
        }

        fun setArtist() {
            if (null != songArtist && null!= SongPlayingFragment.Statified.currentSongHelper) {
                var artist = SongPlayingFragment.Statified.currentSongHelper?.songArtist
                if (artist.equals("<unknown>", ignoreCase = true))
                    songArtist?.visibility = View.GONE
                else {
                    songArtist?.visibility = View.VISIBLE
                    songArtist?.text = artist
                }
            }
        }

        fun setAlbumArt() {
            if(null!= songImg && null!= SongPlayingFragment.Statified.currentSongHelper) {
                val sArtworkUri: Uri = Uri
                        .parse("content://media/external/audio/albumart")
                val uri: Uri = ContentUris.withAppendedId(sArtworkUri, SongPlayingFragment.Statified.currentSongHelper?.songAlbum!!)
                if(null==uri || uri.toString().isEmpty())
                   songImg!!.setImageResource(R.drawable.echo_icon)
                else
                    songImg!!.setImageURI(uri)

                if(null== OfflineAlbumsFragment.songImg!!.drawable){
                    OfflineAlbumsFragment.songImg!!.setImageResource(R.drawable.echo_icon)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel.getAlbumSongs(albumId)
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentAlbumTracksBinding.inflate(layoutInflater)

        try {
            activity?.actionBar?.title = albumName
            activity?.title = albumName
        } catch (e: java.lang.Exception) {

        }
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.VISIBLE

        viewModel.albumSongsList.observe(viewLifecycleOwner, {
            var list = viewModel.albumSongsList.value
            if (!list.isNullOrEmpty())
                setView(list as ArrayList<Songs>)
        })
        binding!!.songArtist.isSelected = true
        binding!!.songTitle.isSelected = true
        songArtist = binding!!.songArtist
        songTitle = binding!!.songTitle
        songImg = binding!!.songImg
        return binding!!.root
    }

    /* It is used to do the final initialization once the other things are in place*/
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /*The variable getSongsList() is used to get store the arrayList returned by the function getSongsFromPhone()*/

        bottomBarSetup()

    }


    fun setView(list: ArrayList<Songs>) {
        binding!!.tracks.layoutManager = LinearLayoutManager(context)
        binding!!.tracks.itemAnimator = DefaultItemAnimator()
        binding!!.tracks.setHasFixedSize(true)
        binding!!.tracks.setItemViewCacheSize(100)
        binding!!.tracks.isDrawingCacheEnabled = true
        binding!!.tracks.isAlwaysDrawnWithCacheEnabled = true
        binding!!.tracks.adapter = MainScreenAdapter(list, activity as Context)
    }

    fun bottomBarSetup() {
        try {

            bottomBarClickHandler()
            songTitle?.text = SongPlayingFragment.Statified.currentSongHelper?.songTitle
            var artist = SongPlayingFragment.Statified.currentSongHelper?.songArtist
            if (artist.equals("<unknown>", ignoreCase = true))
                binding!!.songArtist.visibility = View.GONE
            else
                binding!!.songArtist.text = artist
            songAlbum = SongPlayingFragment.Statified.currentSongHelper?.songAlbum
            setAlbumArt(songAlbum)
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener {
                binding!!.songTitle.text = SongPlayingFragment.Statified.currentSongHelper?.songTitle
                if (artist.equals("<unknown>", ignoreCase = true))
                    binding!!.songArtist.visibility = View.GONE
                else
                    binding!!.songArtist.text = artist
                songAlbum = SongPlayingFragment.Statified.currentSongHelper?.songAlbum
                try {
                    setAlbumArt(songAlbum)
                } catch (e: java.lang.Exception) {

                }
                SongPlayingFragment.Staticated.onSongComplete()
            }

            if(!isMyServiceRunning(EchoNotification::class.java, requireContext())) {
                binding!!.nowPlayingBottomBarMain.visibility = View.GONE
                return
            }
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                binding!!.nowPlayingBottomBarMain.visibility = View.VISIBLE
                binding!!.playPause.setImageDrawable(requireContext().resources.getDrawable(R.drawable.pause_icon))
//                SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
            } else {
                binding!!.nowPlayingBottomBarMain.visibility = View.VISIBLE
                binding!!.playPause.setImageDrawable(requireContext().resources.getDrawable(R.drawable.play_icon))
//                SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.play_icon)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler() {

        binding!!.nowPlayingBottomBarMain.setOnClickListener {

            /*Using the same media player object*/
            mediaPlayer = SongPlayingFragment.Statified.mediaPlayer
            val songPlayingFragment = SongPlayingFragment()
            args = Bundle()

            /*Here when we click on the bottom bar, we navigate to the song playing fragment
            * Since we want the details of the same song which is playing to be displayed in the song playing fragment
            * we pass the details of the current song being played to the song playing fragment using Bundle*/
            args?.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args?.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args?.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songpath)
            args?.putLong("SongID", SongPlayingFragment.Statified.currentSongHelper?.songId!!)
            args?.putLong("songAlbum", SongPlayingFragment.Statified.currentSongHelper?.songAlbum!!)
            args?.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args?.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)

            /*Here we put the additional string in the bundle
            * this tells us that the bottom bar was successfully setup*/
            args?.putString("AlbumSongsBottomBar", "success")
            args?.putString("AlbumName",albumName)

            /*Here we pass the bundle object to the song playing fragment*/
            songPlayingFragment.arguments = args

            /*The below lines are now familiar
            * These are used to open a fragment*/
            fragmentManager?.beginTransaction()
                    ?.replace(R.id.details_fragment, songPlayingFragment)

                    /*The below piece of code is used to handle the back navigation
                    * This means that when you click the bottom bar and move on to the next screen
                    * on pressing back button you navigate to the screen you came from*/
                    ?.addToBackStack("SongPlayingFragment")
                    ?.commit()
        }

        /*Apart from the click on the bottom bar we have a play/pause button in our bottom bar
        * This button is used to play or pause the media player*/
        binding!!.playPause.setOnClickListener {

            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {

                /*If the song was already playing, we then pause it and save the it's position
                * and then change the button to play button*/
                SongPlayingFragment.Statified.mediaPlayer?.pause()
//                trackPosition = SongPlayingFragment.Statified.mediaPlayer?.currentPosition as Int
                binding!!.playPause.setImageDrawable(requireContext().resources.getDrawable(R.drawable.play_icon))

                var play = Intent(context, EchoNotification::class.java)
                play.action = Constants.ACTION.CHANGE_TO_PLAY
                activity?.startService(play)

                SongPlayingFragment.Staticated.upddateButton("pause")
            } else {

                // using the same variable as it does similar work

                MainScreenAdapter.Statified.stopPlayingCalled = true

                if (main?.getnotify_val() == false) {

                    noNext = false


                    song = SongPlayingFragment()

                    trackPosition = SongPlayingFragment.Statified.mediaPlayer?.currentPosition as Int  // current postiton where the player as stopped
                    SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)

//                    SongPlayingFragment.Statified.mediaPlayer?.seekTo(-0)
                    if (SongPlayingFragment.Staticated.reuestAudiofocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                        SongPlayingFragment.Statified.mediaPlayer?.start()
//                    SongPlayingFragment.Statified.mediaPlayer?.previous()


                    binding!!.playPause.setImageDrawable(requireContext().resources.getDrawable(R.drawable.pause_icon))
                    var serviceIntent = Intent(requireActivity(), EchoNotification::class.java)

                    serviceIntent.putExtra("title", binding!!.songTitle.text.toString())
                    serviceIntent.putExtra("artist", binding!!.songArtist.text.toString())
                    serviceIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION


                    activity?.startService(serviceIntent)

                    var play = Intent(requireActivity(), EchoNotification::class.java)
                    play.action = Constants.ACTION.CHANGE_TO_PAUSE
                    activity?.startService(play)

//                    song!!.previous()
                    SongPlayingFragment.Staticated.upddateButton("play")

                } else if (main?.getnotify_val() == true) {


                    /*If the music was already paused and we then click on the button
                * it plays the song from the same position where it was paused
                * and change the button to pause button*/
                    trackPosition = SongPlayingFragment.Statified.mediaPlayer?.currentPosition as Int  // current postiton where the player as stopped
                    SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                    if (SongPlayingFragment.Staticated.reuestAudiofocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                        SongPlayingFragment.Statified.mediaPlayer?.start()
                    binding!!.playPause.setImageDrawable(requireContext().resources.getDrawable(R.drawable.pause_icon))

//                if (main?.notify == true) {
//                    var play = Intent(context, EchoNotification::class.java)
//                    play.setAction(Constants.ACTION.CHANGE_TO_PAUSE)
//                    activity?.startService(play)

                    var play = Intent(requireActivity(), EchoNotification::class.java)
                    play.action = Constants.ACTION.CHANGE_TO_PAUSE
                    activity?.startService(play)



                    SongPlayingFragment.Staticated.upddateButton("play")

                }
            }
        }
        val shuffle = context?.getSharedPreferences(SongPlayingFragment.Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        val isshuffled = shuffle?.getBoolean("feature", false)

        binding!!.next.setOnClickListener {
            song = SongPlayingFragment()
            if (isshuffled!!)
                song!!.playNext("PlayNextLikeNormalShuffle")
            else
                song!!.playNext("PlayNextNormal")
            binding!!.playPause.setImageDrawable(requireContext().resources.getDrawable(R.drawable.pause_icon))
        }


    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setAlbumArt(songAlbum: Long?) {
        var albumId = songAlbum as Long
        if (albumId <= 0L) binding!!.songImg.setImageDrawable(context?.resources?.getDrawable(R.drawable.echo_icon))
        val sArtworkUri: Uri = Uri
                .parse("content://media/external/audio/albumart")
        val uri: Uri = ContentUris.withAppendedId(sArtworkUri, albumId)
        Glide.with(requireContext()).load(uri).placeholder(R.drawable.echo_icon).into(binding!!.songImg)
    }

    override fun onResume() {
        super.onResume()
        setTitle()
        setArtist()
//        setAlbumArt()
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
}