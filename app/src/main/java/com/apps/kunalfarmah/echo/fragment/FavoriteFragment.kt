package com.apps.kunalfarmah.echo.fragment

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.apps.kunalfarmah.echo.adapter.FavoriteAdapter
import com.apps.kunalfarmah.echo.database.EchoDatabase
import com.apps.kunalfarmah.echo.databinding.FragmentFavoriteBinding
import com.apps.kunalfarmah.echo.model.Songs
import com.apps.kunalfarmah.echo.util.Constants
import com.apps.kunalfarmah.echo.util.MediaUtils
import com.apps.kunalfarmah.echo.viewModel.SongsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.util.Collections
import kotlin.math.max

@AndroidEntryPoint
class FavoriteFragment : Fragment() {


    companion object{
        val TAG = "FavoriteFragment"
        var noNext: Boolean? = true
        var mInstance: FavoriteFragment ?= null
    }
    var favouriteAdapter: FavoriteAdapter? = null
    var main: MainActivity? = null
    var myActivity: Activity? = null

    /*This variable will be used for database instance*/
    var favoriteContent: EchoDatabase? = null

    /*Variable to store favorites*/
    var refreshList: ArrayList<Songs>? = null

    var getListfromDatabase: ArrayList<Songs>? = null

    lateinit var binding: FragmentFavoriteBinding

    val viewModel: SongsViewModel by viewModels()
    var songAlbum: Long? = null

    var prefs: SharedPreferences ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = activity?.getSharedPreferences(Constants.APP_PREFS, Context.MODE_PRIVATE)
        mInstance = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)
        main = MainActivity()
        activity?.title = "Favorites"
        favoriteContent = EchoDatabase(myActivity)
        binding = FragmentFavoriteBinding.inflate(layoutInflater)
        MainActivity.Statified.settingsOn = true
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.VISIBLE
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
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.main, menu)
//    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_sort_recent)?.isVisible=false
    }

    /*Here we perform the actions of sorting according to the menu item clicked*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val switcher = item.itemId
        if (switcher == R.id.acton_sort_ascending) {
            val editor = prefs?.edit()
            editor?.putString(Constants.SORTING, Constants.NAME_ASC)
            editor?.apply()
            if (refreshList != null) {
                Collections.sort(refreshList, Songs.Statified.nameComparator)
            }
            favouriteAdapter?.notifyDataSetChanged()
            return false
        } else if (switcher == R.id.action_sort_recent) {
            val editor = prefs?.edit()
            editor?.putString(Constants.SORTING, Constants.RECENTLY_ADDED)
            editor?.apply()
            if (refreshList != null) {
                Collections.sort(refreshList, Songs.Statified.dateComparator)
            }
            favouriteAdapter?.notifyDataSetChanged()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if(favouriteAdapter?.songDetails.isNullOrEmpty()){
            binding.recyclerView?.visibility = View.GONE
            binding.noFavorites?.visibility = View.VISIBLE
        }
        else{
            binding.recyclerView?.visibility = View.VISIBLE
            binding.noFavorites?.visibility = View.GONE
        }
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
//                val sortOrder = prefs?.getString(Constants.SORTING, "")
//                if (sortOrder.equals(Constants.NAME_ASC, ignoreCase = true)) {
//                    Collections.sort(refreshList, Songs.Statified.nameComparator)
//                } else if (sortOrder.equals(Constants.RECENTLY_ADDED, ignoreCase = true)) {
//                    Collections.sort(refreshList, Songs.Statified.dateComparator)
//                }
                Collections.sort(refreshList, Songs.Statified.dateComparator)
                binding.noFavorites?.visibility = View.GONE
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
            binding.recyclerView?.visibility = View.GONE
            binding.noFavorites?.visibility = View.VISIBLE
        }
    }

}