package com.apps.kunalfarmah.echo.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apps.kunalfarmah.echo.Adapters.MainScreenAdapter
import com.apps.kunalfarmah.echo.Constants
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.Songs
import com.apps.kunalfarmah.echo.activities.MainActivity
import com.apps.kunalfarmah.echo.mNotification
import com.bumptech.glide.Glide
import java.io.FileDescriptor
import java.util.*



class MainScreenFragment : Fragment()  {


    var args:Bundle?=null
    var main:MainActivity?=null
    var getSongsList: ArrayList<Songs>? = null

    var nowPlayingBottomBarMain: RelativeLayout? = null
    var play_pause: ImageButton? = null

    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null
    var recyclerView: RecyclerView? = null

    var myActivity: Activity? = null
    var trackPosition: Int = 0
    var _MainScreenAdapter: MainScreenAdapter? = null
    var song:SongPlayingFragment?=null

    @SuppressLint("StaticFieldLeak")
    companion object Statified {
        var songImg: ImageView? = null
        var mediaPlayer: MediaPlayer? = null
        var noNext:Boolean=true
        var songTitle: TextView? = null
        var songArtist: TextView? =null
        var songAlbum: Long? = null

    }
    object Staticated{
        fun setTitle(){
            songTitle?.text = SongPlayingFragment.Statified.currentSongHelper?.songTitle
        }

    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_screen, container, false)

        setHasOptionsMenu(true)

        main= MainActivity()
        activity?.title = "All Songs"
        visibleLayout = view?.findViewById<RelativeLayout>(R.id.visible_layout)
        noSongs = view?.findViewById(R.id.noSongs)
        recyclerView = view?.findViewById(R.id.ContentMain)
        songTitle = view?.findViewById(R.id.song_title)
        songTitle?.isSelected = true
        songArtist=view?.findViewById(R.id.song_artist)
        songImg = view?.findViewById(R.id.defaultMusic)
        play_pause = view?.findViewById(R.id.play_pause)
        nowPlayingBottomBarMain = view?.findViewById(R.id.hiddenBarMainScreen1)

        MainActivity.Statified.MainorFavOn=true

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        myActivity = activity
    }


    /* It is used to do the final initialization once the other things are in place*/
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /*The variable getSongsList() is used to get store the arrayList returned by the function getSongsFromPhone()*/
        getSongsList = getSongsFromPhone()

        /*Declaring the preferences to save the sorting order which we select*/
        val prefs = activity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)
        val action_sort_ascending = prefs?.getString("action_sort_ascending", "true")
        val action_sort_recent = prefs?.getString("action_sort_recent", "false")

        /*If there are no songs we do not display the list instead we display no songs message*/
        if (getSongsList == null || getSongsList?.size==0) {
            //visibleLayout?.visibility = View.INVISIBLE
            noSongs?.visibility = View.VISIBLE
        }
        /*If there are songs in the device, we display the list*/
        else {
            visibleLayout?.visibility = View.VISIBLE
            noSongs?.visibility=View.GONE
            /*Here we initialize the main screen adapter and pass it the required parameters i.e. the list of songs and the context*/
            _MainScreenAdapter = MainScreenAdapter(getSongsList as ArrayList<Songs>, myActivity as Context)
            /*The layout manager defines the way a view will be set in the recycler view
            * There are different types of layout managers e.g. Linear, Grid, Staggered grid
            * Here we are using the Linear layout manager which aligns the objects in a linear way one under the other*/
            val mLayoutManager = LinearLayoutManager(myActivity)
            /*Here we put assign our layout manager to the recycler view's layout manager*/
            recyclerView?.layoutManager = mLayoutManager
            /*It is similar to the item animator we used in the navigation drawer*/
            recyclerView?.itemAnimator = DefaultItemAnimator()
            /*Finally we set the adapter to the recycler view*/
            recyclerView?.setHasFixedSize(true)
            recyclerView?.setItemViewCacheSize(100)
            recyclerView?.isDrawingCacheEnabled = true
            recyclerView?.isAlwaysDrawnWithCacheEnabled=true
            recyclerView?.adapter = _MainScreenAdapter
        }

        /*If the songs list is not empty, then we check whether applied any comparator
        * And we use that comparator and sort the list accordingly*/

        if (getSongsList != null) {
            if (action_sort_ascending!!.equals("true", ignoreCase = true)) {
                Collections.sort(getSongsList, Songs.Statified.nameComparator)
                _MainScreenAdapter?.notifyDataSetChanged()
            } else if (action_sort_recent!!.equals("true", ignoreCase = true)) {
                Collections.sort(getSongsList, Songs.Statified.dateComparator)
                _MainScreenAdapter?.notifyDataSetChanged()
            }
        }

        bottomBarSetup()

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        var searchView = searchItem?.actionView as SearchView
        searchView.queryHint = "Enter Song or Artist to Search"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(query: String): Boolean {

                var name_to_saerch = query.toLowerCase()

                var newList:ArrayList<Songs>?= ArrayList<Songs>()

                for(songs in getSongsList!!) {
                    var name = songs.songTitle.toLowerCase()
                    var artist = songs.artist.toLowerCase()
                    if (name.contains(name_to_saerch, true))
                        newList?.add(songs)
                    else if (artist.contains(name_to_saerch, true))
                        newList?.add(songs)

                }
                //Task HERE

                _MainScreenAdapter?.filter_data(newList)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {

                return false
            }


        })

        return
    }


    /*Here we perform the actions of sorting according to the menu item clicked*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val switcher = item.itemId
        if (switcher == R.id.acton_sort_ascending) {
            /*Whichever action item is selected, we save the preferences and perform the operation of comparison*/
            val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
            editor?.putString("action_sort_ascending", "true")
            editor?.putString("action_sort_recent", "false")
            editor?.apply()
            if (getSongsList != null) {
                Collections.sort(getSongsList, Songs.Statified.nameComparator)
            }
            _MainScreenAdapter?.notifyDataSetChanged()
            return false
        } else if (switcher == R.id.action_sort_recent) {
            val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
            editor?.putString("action_sort_recent", "true")
            editor?.putString("action_sort_ascending", "false")
            editor?.apply()
            if (getSongsList != null) {
                Collections.sort(getSongsList, Songs.Statified.dateComparator)
            }
            _MainScreenAdapter?.notifyDataSetChanged()
            return false
        }
        return super.onOptionsItemSelected(item)
    }


    fun getSongsFromPhone(): ArrayList<Songs> {

        var arralist = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songURI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songURI, null, null, null, null)

        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateAdded = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val songAlbum = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

            while (songCursor.moveToNext()) {
                var currentID = songCursor.getLong(songId)
                var currTitle = songCursor.getString(songTitle)
                var currArtist = songCursor.getString(songArtist)
                var currData = songCursor.getString(songData)
                var currdate = songCursor.getLong(dateAdded)
                var currAlbum = songCursor.getLong(songAlbum)


                try {
                    arralist.add(Songs(currentID, currTitle, currArtist, currData, currdate, currAlbum))
                }
                catch (e:Exception){
                    Toast.makeText(context,"Not Enough RAM to Allocate Memory and Collect Your Songs :(",Toast.LENGTH_SHORT).show()
                }
            }
        }

        try {
            removeduplicates(arralist)
        }catch (e:Exception){}

        try {
            songCursor!!.close()
        }catch (e:Exception){}

        return arralist


    }

    fun removeduplicates(list:ArrayList<Songs>) {

        // preventing index out of bounds
        try {
            for (i in 0 until list.size - 3) {
                for (j in i + 1 until list.size - 3) {
                    if (list.get(j).songTitle == list.get(i).songTitle && list.get(j).artist == list.get(i).artist)
                        list.removeAt(j)
                }
            }
        }catch (e:Exception){}
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun getAlbumart(album_id: Long): Bitmap? {
        var bm: Bitmap? = null
        try {
            val sArtworkUri: Uri = Uri
                    .parse("content://media/external/audio/albumart")
            val uri: Uri = ContentUris.withAppendedId(sArtworkUri, album_id)
            val pfd: ParcelFileDescriptor? = context!!.contentResolver
                    .openFileDescriptor(uri, "r")
            if (pfd != null) {
                val fd: FileDescriptor = pfd.fileDescriptor
                bm = BitmapFactory.decodeFileDescriptor(fd)
            }
        } catch (e: java.lang.Exception) {
        }
        return bm
    }


    fun bottomBarSetup() {
        try {

            /*Calling the click handler function will help us handle the click events of the bottom bar*/
            bottomBarClickHandler()

            /*We fetch the song title with the help of the current song helper and set it to the song title in our bottom bar*/
            songTitle?.text = SongPlayingFragment.Statified.currentSongHelper?.songTitle

            songAlbum = SongPlayingFragment.Statified.currentSongHelper?.songAlbum

            setAlbumArt(songAlbum)

            /*If we are the on the favorite screen and not on the song playing screen when the song finishes
            * we want the changes in the song to reflect on the favorite screen hence we call the onSongComplete() function which help us in maintaining consistency*/
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener({
                songTitle?.text = SongPlayingFragment.Statified.currentSongHelper?.songTitle
                songArtist?.text = SongPlayingFragment.Statified.currentSongHelper?.songArtist
                songAlbum = SongPlayingFragment.Statified.currentSongHelper?.songAlbum
                try {
                    setAlbumArt(songAlbum)
                }
                catch (e:java.lang.Exception){

                }
                SongPlayingFragment.Staticated.onSongComplete()
            })

            /*While coming back from the song playing screen
            * if the song was playing then only the bottom bar is placed, else not placed*/
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                nowPlayingBottomBarMain?.visibility = View.VISIBLE
                play_pause?.setBackgroundResource(R.drawable.pause_icon)
//                SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
            } else {
                nowPlayingBottomBarMain?.visibility = View.VISIBLE
                play_pause?.setBackgroundResource(R.drawable.play_icon)
//                SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.play_icon)
            }

            /*Since we are dealing with the media player object which can be null, hence we handle all such exceptions using the try-catch block*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*The bottomBarClickHandler() function is used to handle the click events on the bottom bar*/
    fun bottomBarClickHandler() {

        /*We place a click listener on the bottom bar*/
        nowPlayingBottomBarMain?.setOnClickListener({

            /*Using the same media player object*/
            Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer
            val songPlayingFragment = SongPlayingFragment()
            args = Bundle()

            /*Here when we click on the bottom bar, we navigate to the song playing fragment
            * Since we want the details of the same song which is playing to be displayed in the song playing fragment
            * we pass the details of the current song being played to the song playing fragment using Bundle*/
            args?.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args?.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args?.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songpath)
            args?.putLong("SongID", SongPlayingFragment.Statified.currentSongHelper?.songId!!)
            args?.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args?.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)

            /*Here we put the additional string in the bundle
            * this tells us that the bottom bar was successfully setup*/
            args?.putString("MainBottomBar", "success")

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
        })

        /*Apart from the click on the bottom bar we have a play/pause button in our bottom bar
        * This button is used to play or pause the media player*/
        play_pause?.setOnClickListener({

            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {

                /*If the song was already playing, we then pause it and save the it's position
                * and then change the button to play button*/
                SongPlayingFragment.Statified.mediaPlayer?.pause()
//                trackPosition = SongPlayingFragment.Statified.mediaPlayer?.currentPosition as Int
                play_pause?.setBackgroundResource(R.drawable.play_icon)

                var play = Intent(context, mNotification::class.java)
                play.action = Constants.ACTION.CHANGE_TO_PLAY
                activity?.startService(play)

                SongPlayingFragment.Staticated.upddateButton("pause")
            } else {

                // using the same variable as it does similar work

                MainScreenAdapter.Statified.stopPlayingCalled=true

                if (main?.getnotify_val() == false) {

                    Statified.noNext=false


                    song= SongPlayingFragment()

                    trackPosition = SongPlayingFragment.Statified.mediaPlayer?.currentPosition as Int  // current postiton where the player as stopped
                    SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)

//                    SongPlayingFragment.Statified.mediaPlayer?.seekTo(-0)
                    if(SongPlayingFragment.Staticated.reuestAudiofocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                        SongPlayingFragment.Statified.mediaPlayer?.start()
//                    SongPlayingFragment.Statified.mediaPlayer?.previous()


                    play_pause?.setBackgroundResource(R.drawable.pause_icon)

                    var serviceIntent = Intent(myActivity, mNotification::class.java)

                    serviceIntent.putExtra("title", songTitle?.text.toString())
                    serviceIntent.putExtra("artist", songArtist?.text.toString())
                    serviceIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION


                    activity?.startService(serviceIntent)

                    var play = Intent(myActivity, mNotification::class.java)
                    play.action = Constants.ACTION.CHANGE_TO_PAUSE
                    activity?.startService(play)

//                    song!!.previous()
                    SongPlayingFragment.Staticated.upddateButton("play")

                } else if(main?.getnotify_val()==true){


                    /*If the music was already paused and we then click on the button
                * it plays the song from the same position where it was paused
                * and change the button to pause button*/
                    trackPosition = SongPlayingFragment.Statified.mediaPlayer?.currentPosition as Int  // current postiton where the player as stopped
                    SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                    if(SongPlayingFragment.Staticated.reuestAudiofocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                        SongPlayingFragment.Statified.mediaPlayer?.start()
                    play_pause?.setBackgroundResource(R.drawable.pause_icon)

//                if (main?.notify == true) {
//                    var play = Intent(context, mNotification::class.java)
//                    play.setAction(Constants.ACTION.CHANGE_TO_PAUSE)
//                    activity?.startService(play)

                    var play = Intent(myActivity, mNotification::class.java)
                    play.action = Constants.ACTION.CHANGE_TO_PAUSE
                    activity?.startService(play)



                    SongPlayingFragment.Staticated.upddateButton("play")

                }
            }
        })

    }

    fun setAlbumArt(songAlbum:Long?){
        var albumId = songAlbum as Long

        if(albumId<=0L) songImg!!.setImageResource(R.drawable.now_playing_bar_eq_image)
        val sArtworkUri: Uri = Uri
                .parse("content://media/external/audio/albumart")
        val uri: Uri = ContentUris.withAppendedId(sArtworkUri, albumId)
        Glide.with(requireContext()).load(uri).into(songImg!!)
    }
}