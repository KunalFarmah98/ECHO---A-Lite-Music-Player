package com.apps.kunalfarmah.echo.fragment

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.apps.kunalfarmah.echo.adapter.FavoriteAdapter
import com.apps.kunalfarmah.echo.database.EchoDatabase
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.model.Songs
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.apps.kunalfarmah.echo.databinding.FragmentFavoriteBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import androidx.appcompat.widget.SearchView
import com.apps.kunalfarmah.echo.util.BottomBarUtils
import com.apps.kunalfarmah.echo.viewModel.SongsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

@AndroidEntryPoint
class FavoriteFragment : Fragment() {


    companion object{
        val TAG = "FavoriteFragment"
        var noNext: Boolean? = true
    }
    var _FavouriteAdapter: FavoriteAdapter? = null
    var main: MainActivity? = null
    var myActivity: Activity? = null
    var trackPosition: Int = 0

    /*This variable will be used for database instance*/
    var favoriteContent: EchoDatabase? = null

    /*Variable to store favorites*/
    var refreshList: ArrayList<Songs>? = null

    var getListfromDatabase: ArrayList<Songs>? = null

    lateinit var binding: FragmentFavoriteBinding

    val viewModel: SongsViewModel by viewModels()
    var songAlbum: Long? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)
        main = MainActivity()
        activity?.title = "Favorites"
        favoriteContent = EchoDatabase(myActivity)
        binding = FragmentFavoriteBinding.inflate(layoutInflater)
        MainActivity.Statified.settingsOn = true
        binding.nowPlayingBottomBar.songTitle.isSelected = true
        binding.nowPlayingBottomBar.songArtist.isSelected = true
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.VISIBLE
        viewModel.isSongPlaying.observe(viewLifecycleOwner,{
            if(it)
                binding.nowPlayingBottomBar.playPause.setImageDrawable(requireContext().resources.getDrawable(R.drawable.pause_icon))
            else
                binding.nowPlayingBottomBar.playPause.setImageDrawable(requireContext().resources.getDrawable(R.drawable.play_icon))
        })

//        MainActivity.Statified.MainorFavOn=true

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        if(main!=null){
            BottomBarUtils.bottomBarSetup(requireActivity(),main!!,requireFragmentManager(),
            binding.nowPlayingBottomBar)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        display_favorites_by_searching()
        BottomBarUtils.bottomBarSetup(myActivity!!,main!!,requireFragmentManager(),
        binding.nowPlayingBottomBar)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        var searchView = searchItem?.actionView as SearchView
        searchView.queryHint = "Enter Song or Artist to Search"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(query: String): Boolean {
                try {

                    var name_to_saerch = query.toLowerCase()

                    var newList: java.util.ArrayList<Songs>? = java.util.ArrayList<Songs>()

                    for (songs in refreshList!!) {
                        var name = songs.songTitle.toLowerCase()
                        var artist = songs.artist.toLowerCase()
                        if (name.contains(name_to_saerch, true))
                            newList?.add(songs)
                        else if (artist.contains(name_to_saerch, true))
                            newList?.add(songs)

                    }
                    //Task HERE

                    _FavouriteAdapter?.filter_data(newList)
                } catch (e: Exception) {
                    Toast.makeText(context, "Aw Snap! Something Wrong Happened", Toast.LENGTH_SHORT).show()
                }
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
            val editor = myActivity?.getSharedPreferences(getString(R.string.sorting), Context.MODE_PRIVATE)?.edit()
            editor?.putString(getString(R.string.sort_by_name), "true")
            editor?.putString(getString(R.string.sort_by_recent), "false")
            editor?.apply()
            if (refreshList != null) {
                Collections.sort(refreshList, Songs.Statified.nameComparator)
            }
            _FavouriteAdapter?.notifyDataSetChanged()
            return false
        } else if (switcher == R.id.action_sort_recent) {
            val editortwo = myActivity?.getSharedPreferences(getString(R.string.sorting), Context.MODE_PRIVATE)?.edit()
            editortwo?.putString(getString(R.string.sort_by_recent), "true")
            editortwo?.putString(getString(R.string.sort_by_name), "false")
            editortwo?.apply()
            if (refreshList != null) {
                Collections.sort(refreshList, Songs.Statified.dateComparator)
            }
            _FavouriteAdapter?.notifyDataSetChanged()
            return false
        }
        return super.onOptionsItemSelected(item)
    }


    /*As the name suggests, this function is used to fetch the songs present in your phones and returns the arraylist of the same*/
    fun getSongsFromPhone(): ArrayList<Songs>? {
        var arrayList = ArrayList<Songs>()

        /*A content resolver is used to access the data present in your phone
        * In this case it is used for obtaining the songs present your phone*/
        var contentResolver = myActivity?.contentResolver

        /*Here we are accessing the Media class of Audio class which in turn a class of Media Store, which contains information about all the media files present
        * on our mobile device*/
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        /*Here we make the request of songs to the content resolver to get the music files from our device*/
        var songCursor = contentResolver?.query(songUri, null, null, null, null)

        /*In the if condition we check whether the number of music files are null or not. The moveToFirst() function returns the first row of the results*/
        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val songAlbum = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val songAlbumName = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            /*moveToNext() returns the next row of the results. It returns null if there is no row after the current row*/
            while (songCursor.moveToNext()) {

                var currentId = songCursor.getLong(songId)
                var currentTitle = songCursor.getString(songTitle)
                var album = songCursor.getString(songAlbumName)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateIndex)
                var currAlbum = songCursor.getLong(songAlbum)
                if(null==currentArtist)
                    currentArtist = ""
                if(null==album)
                    album = ""
                /*Adding the fetched songs to the arraylist*/
                arrayList.add(Songs(currentId, currentTitle, currentArtist,album, currentData, currentDate, currAlbum))
            }
        } else {
            return null
        }

        /*Returning the arraylist of songs*/
        return arrayList
    }


    private fun getCoverArtPath(context: Context?, androidAlbumId: Long): String? {
        var path: String? = null
        val c: Cursor? = context?.contentResolver?.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Audio.Albums.ALBUM_ART),
                MediaStore.Audio.Albums._ID + "=?", arrayOf(java.lang.Long.toString(androidAlbumId)),
                null)
        if (c != null) {
            if (c.moveToFirst()) {
                path = c.getString(0)
            }
            c.close()
        }
        return path
    }
    /*The below function is used to search the favorites and display*/
    fun display_favorites_by_searching() {

        /*Checking if database has any entry or not*/
        if (favoriteContent?.checkSize() as Int > 0) {

            /*New list for storing the favorites*/
            refreshList = ArrayList<Songs>()

            /*Getting the list of songs from database*/
            getListfromDatabase = favoriteContent?.queryDBList()

            /*Getting list of songs from phone storage*/
            val fetchListfromDevice = getSongsFromPhone()

            /*If there are no songs in phone then there cannot be any favorites*/
            if (fetchListfromDevice != null) {

                /*Then we check all the songs in the phone*/
                for (i in 0..fetchListfromDevice.size - 1) {

                    /*We iterate through every song in database*/
                    for (j in 0..getListfromDatabase?.size as Int - 1) {

                        /*While iterating through all the songs we check for the songs which are in both the lists
                        * i.e. the favorites songs*/
                        if (getListfromDatabase?.get(j)?.songID === fetchListfromDevice.get(i).songID) {

                            /*on getting the favorite songs we add them to the refresh list*/
                            refreshList?.add((getListfromDatabase as ArrayList<Songs>)[j])
                        }
                    }
                }
            } else {
            }

            /*If refresh list is null we display that there are no favorites*/
            if (refreshList == null) {
                //  recyclerView?.visibility = View.INVISIBLE
                binding.noFavorites?.visibility = View.VISIBLE
            } else {

                binding.noFavorites?.visibility = View.INVISIBLE
                // recyclerView?.visibility = View.VISIBLE

                /*Else we setup our recycler view for displaying the favorite songs*/
                _FavouriteAdapter = FavoriteAdapter(refreshList as java.util.ArrayList<Songs>, myActivity as Context)
                val mLayoutManager = LinearLayoutManager(activity)
                binding.recyclerView?.layoutManager = mLayoutManager
                binding.recyclerView?.itemAnimator = DefaultItemAnimator()
                binding.recyclerView?.adapter = _FavouriteAdapter
                binding.recyclerView?.setHasFixedSize(true)
            }
        } else {

            /*If initially the checkSize() function returned 0 then also we display the no favorites present message*/
            binding.recyclerView?.visibility = View.INVISIBLE
            binding.noFavorites?.visibility = View.VISIBLE
        }
    }

}