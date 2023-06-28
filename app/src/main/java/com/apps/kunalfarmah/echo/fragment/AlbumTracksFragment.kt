package com.apps.kunalfarmah.echo.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.adapter.AlbumTracksAdapter
import com.apps.kunalfarmah.echo.databinding.FragmentAlbumTracksBinding
import com.apps.kunalfarmah.echo.model.Songs
import com.apps.kunalfarmah.echo.viewModel.SongsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumTracksFragment(val id: Long?, val name: String) : Fragment() {


    companion object {
        val TAG = "AlbumTracksFragment"
        var mInstance: AlbumTracksFragment ?= null
    }

    var albumId: Long = id!!
    val viewModel: SongsViewModel by viewModels()
    var binding: FragmentAlbumTracksBinding? = null
    var songAlbum: Long? = null
    var tracksAdapter: AlbumTracksAdapter ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel.getAlbumSongs(albumId)
        mInstance = this
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentAlbumTracksBinding.inflate(layoutInflater)
        try {
            activity?.actionBar?.title = name
            activity?.title = name
        } catch (e: java.lang.Exception) {

        }
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.VISIBLE

        viewModel.albumSongsList.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty())
                setView(it as ArrayList<Songs>)
        })
        return binding!!.root
    }


    private fun setView(list: ArrayList<Songs>) {
        tracksAdapter = AlbumTracksAdapter(list, activity as Context, albumId)
        binding!!.tracks.layoutManager = LinearLayoutManager(context)
        binding!!.tracks.itemAnimator = DefaultItemAnimator()
        binding!!.tracks.setHasFixedSize(true)
        binding!!.tracks.setItemViewCacheSize(100)
        binding!!.tracks.isDrawingCacheEnabled = true
        binding!!.tracks.isAlwaysDrawnWithCacheEnabled = true
        binding!!.tracks.adapter = tracksAdapter
    }

}