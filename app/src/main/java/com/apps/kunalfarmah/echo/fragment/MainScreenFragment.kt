package com.apps.kunalfarmah.echo.fragment

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.apps.kunalfarmah.echo.App
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.apps.kunalfarmah.echo.adapter.MainScreenAdapter
import com.apps.kunalfarmah.echo.databinding.FragmentMainScreenBinding
import com.apps.kunalfarmah.echo.model.Songs
import com.apps.kunalfarmah.echo.util.Constants
import com.apps.kunalfarmah.echo.util.MediaUtils
import com.apps.kunalfarmah.echo.viewModel.SongsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.util.Collections
import kotlin.math.max


@AndroidEntryPoint
class MainScreenFragment : Fragment() {

    companion object {
        val TAG = "MainScreenFragment"
        var mInstance: MainScreenFragment? = null
    }

    private val viewModel: SongsViewModel by viewModels()
    var main: MainActivity? = null
    var songsList: List<Songs>? = null

    var myActivity: Activity? = null
    var mainScreenAdapter: MainScreenAdapter? = null
    lateinit var prefs: SharedPreferences
    var sortOrder : String = ""

    private lateinit var binding: FragmentMainScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mInstance = this
        prefs = (activity?: App.context).getSharedPreferences(Constants.APP_PREFS, Context.MODE_PRIVATE)
        sortOrder = prefs.getString(Constants.SORTING, "").toString()
        viewModel.getAllSongs()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentMainScreenBinding.inflate(layoutInflater)
        main = MainActivity()
        activity?.title = "All Songs"
        
        MainActivity.Statified.MainorFavOn = true
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.VISIBLE

        if(!viewModel.songsList.value.isNullOrEmpty()){
            binding.loading.visibility = View.GONE
            songsList = viewModel.songsList.value
            setView()
        }
        viewModel.songsList.observe(viewLifecycleOwner, {
            binding.loading.visibility = View.GONE
            songsList = viewModel.songsList.value
            setView()
        })

        binding.help.text = (Html.fromHtml("<u>Need Help?</u>"))
        binding.help.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.details_fragment, HelpFragment(), HelpFragment.TAG)
                    .addToBackStack(HelpFragment.TAG)
                    .commit()
        }
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

    fun setView() {

        if (songsList == null || songsList?.size == 0) {
            binding.noSongs.visibility = View.VISIBLE
        } else {
//            if (sortOrder.equals(Constants.NAME_ASC, ignoreCase = true)) {
//                Collections.sort(songsList, Songs.Statified.nameComparator)
//            } else if (sortOrder.equals(Constants.RECENTLY_ADDED, ignoreCase = true)) {
//                Collections.sort(songsList, Songs.Statified.dateComparator)
//            }
            Collections.sort(songsList, Songs.Statified.dateComparator)
            binding.visibleLayout.visibility = View.VISIBLE
            binding.noSongs.visibility = View.GONE
            mainScreenAdapter = MainScreenAdapter(songsList as ArrayList<Songs>, myActivity as Context)
            val mLayoutManager = LinearLayoutManager(myActivity)
            binding.recyclerView.layoutManager = mLayoutManager
            binding.recyclerView.itemAnimator = DefaultItemAnimator()
            binding.recyclerView.setHasFixedSize(true)
            binding.recyclerView.setItemViewCacheSize(100)
            binding.recyclerView.isDrawingCacheEnabled = true
            binding.recyclerView.isAlwaysDrawnWithCacheEnabled = true
            binding.recyclerView.adapter = mainScreenAdapter
            try {
                binding.recyclerView.scrollToPosition(max(0, MediaUtils.getSongIndex() - 2))
            }
            catch (e:java.lang.Exception){}
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.main, menu)
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val switcher = item.itemId
        if (switcher == R.id.acton_sort_ascending) {
            val editor = myActivity?.getSharedPreferences(Constants.APP_PREFS, Context.MODE_PRIVATE)?.edit()
            editor?.putString(Constants.SORTING,Constants.NAME_ASC)
            editor?.apply()
            if (songsList != null) {
                Collections.sort(songsList, Songs.Statified.nameComparator)
            }
            mainScreenAdapter?.notifyDataSetChanged()
            return false
        } else if (switcher == R.id.action_sort_recent) {
            val editor = myActivity?.getSharedPreferences(Constants.APP_PREFS, Context.MODE_PRIVATE)?.edit()
            editor?.putString(Constants.SORTING,Constants.RECENTLY_ADDED)
            editor?.apply()
            if (songsList != null) {
                Collections.sort(songsList, Songs.Statified.dateComparator)
            }
            mainScreenAdapter?.notifyDataSetChanged()
            return false
        }
        return super.onOptionsItemSelected(item)
    }


}