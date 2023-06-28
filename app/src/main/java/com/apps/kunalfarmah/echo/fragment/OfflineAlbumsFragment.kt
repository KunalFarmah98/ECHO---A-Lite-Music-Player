package com.apps.kunalfarmah.echo.fragment

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.apps.kunalfarmah.echo.adapter.OfflineAlbumsAdapter
import com.apps.kunalfarmah.echo.databinding.FragmentAlbumsBinding
import com.apps.kunalfarmah.echo.model.SongAlbum
import com.apps.kunalfarmah.echo.viewModel.SongsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OfflineAlbumsFragment : Fragment() {

    companion object{
        val TAG = "OfflineAlbumsFragment"
        var postion: Int? = 0
    }
    val viewModel: SongsViewModel by viewModels()
    var binding: FragmentAlbumsBinding? = null
    var main: MainActivity? = null
    var list: List<SongAlbum>? = null
    var mAdapter: OfflineAlbumsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel.getAllAlbums()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentAlbumsBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        main = MainActivity()
        try {
            activity?.title = "Albums"
            activity?.actionBar?.title = "Albums"
        } catch (e: java.lang.Exception) {

        }
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.VISIBLE
        binding!!.help.text = (Html.fromHtml("<u>Need Help?</u>"))
        binding!!.help.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.details_fragment, HelpFragment(), HelpFragment.TAG)
                    .addToBackStack(HelpFragment.TAG)
                    .commit()
        }
        viewModel.albumsList.observe(viewLifecycleOwner, {
            list=it
            if(it.isNullOrEmpty()){
                binding!!.noSongs.visibility = View.VISIBLE
                binding!!.Albums.visibility = View.GONE
                return@observe
            }
            binding!!.noSongs.visibility = View.GONE
            binding!!.Albums.visibility = View.VISIBLE
            mAdapter = OfflineAlbumsAdapter(activity as Context, it)
            binding!!.Albums.layoutManager = (GridLayoutManager(requireContext(), 2))
            binding!!.Albums.setHasFixedSize(true)
            binding!!.Albums.setItemViewCacheSize(10)
            binding!!.Albums.adapter = mAdapter
            binding!!.Albums.scrollToPosition(postion!!)
        })
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}