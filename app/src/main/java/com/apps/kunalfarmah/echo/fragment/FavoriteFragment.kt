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
import com.apps.kunalfarmah.echo.util.MediaUtils
import com.apps.kunalfarmah.echo.viewModel.SongsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.math.max

@AndroidEntryPoint
class FavoriteFragment : Fragment() {


    companion object{
        val TAG = "FavoriteFragment"
        var noNext: Boolean? = true
    }
    var favouriteAdapter: FavoriteAdapter? = null
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
        viewModel.isSongPlaying.observe(viewLifecycleOwner) {
            if (it)
                binding.nowPlayingBottomBar.playPause.setImageDrawable(
                    requireContext().resources.getDrawable(
                        R.drawable.pause_icon
                    )
                )
            else
                binding.nowPlayingBottomBar.playPause.setImageDrawable(
                    requireContext().resources.getDrawable(
                        R.drawable.play_icon
                    )
                )
        }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getFavorites()
        BottomBarUtils.bottomBarSetup(myActivity!!,main!!,requireFragmentManager(),
        binding.nowPlayingBottomBar,FavoriteFragment@this)
    }

    override fun onResume() {
        super.onResume()
        updateCurrentSong()
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

                    favouriteAdapter?.filter_data(newList)
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
            favouriteAdapter?.notifyDataSetChanged()
            return false
        } else if (switcher == R.id.action_sort_recent) {
            val editortwo = myActivity?.getSharedPreferences(getString(R.string.sorting), Context.MODE_PRIVATE)?.edit()
            editortwo?.putString(getString(R.string.sort_by_recent), "true")
            editortwo?.putString(getString(R.string.sort_by_name), "false")
            editortwo?.apply()
            if (refreshList != null) {
                Collections.sort(refreshList, Songs.Statified.dateComparator)
            }
            favouriteAdapter?.notifyDataSetChanged()
            return false
        }
        return super.onOptionsItemSelected(item)
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
    fun getFavorites() {

        /*Checking if database has any entry or not*/
        if (favoriteContent?.checkSize() as Int > 0) {

            /*New list for storing the favorites*/
            refreshList = ArrayList<Songs>()

            /*Getting the list of songs from database*/
            getListfromDatabase = favoriteContent?.queryDBList()

            /*We iterate through every song in database*/
            for (i in 0..getListfromDatabase?.size as Int - 1) {
                /*on getting the favorite songs we add them to the refresh list*/
                refreshList?.add((getListfromDatabase as ArrayList<Songs>)[i])
            }

            /*If refresh list is null we display that there are no favorites*/
            if (refreshList == null) {
                //  recyclerView?.visibility = View.INVISIBLE
                binding.noFavorites?.visibility = View.VISIBLE
            } else {

                binding.noFavorites?.visibility = View.INVISIBLE
                // recyclerView?.visibility = View.VISIBLE

                /*Else we setup our recycler view for displaying the favorite songs*/
                favouriteAdapter = FavoriteAdapter(
                    refreshList as java.util.ArrayList<Songs>,
                    myActivity as Context
                )
                val mLayoutManager = LinearLayoutManager(activity)
                binding.recyclerView?.layoutManager = mLayoutManager
                binding.recyclerView?.itemAnimator = DefaultItemAnimator()
                binding.recyclerView?.adapter = favouriteAdapter
                binding.recyclerView?.setHasFixedSize(true)
                try {
                    binding.recyclerView.scrollToPosition(max(0, MediaUtils.getSongIndex() - 2))
                }
                catch (e:java.lang.Exception){}
            }
        } else {

            /*If initially the checkSize() function returned 0 then also we display the no favorites present message*/
            binding.recyclerView?.visibility = View.INVISIBLE
            binding.noFavorites?.visibility = View.VISIBLE
        }
    }

    fun updateCurrentSong(){
        favouriteAdapter?.notifyDataSetChanged()
    }

}