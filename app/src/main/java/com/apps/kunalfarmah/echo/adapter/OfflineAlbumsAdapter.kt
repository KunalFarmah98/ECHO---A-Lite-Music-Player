package com.apps.kunalfarmah.echo.adapter

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.SongAlbum
import com.apps.kunalfarmah.echo.Songs
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.apps.kunalfarmah.echo.databinding.GridItemBinding
import com.apps.kunalfarmah.echo.fragment.AlbumTracksFragment
import com.bumptech.glide.Glide

class OfflineAlbumsAdapter(context: Context, list: List<SongAlbum>) : RecyclerView.Adapter<OfflineAlbumsAdapter.AlbumsViewHolder>() {

    var mContext = context
    var albums = list

    class AlbumsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: GridItemBinding? = null

        init {
            binding = GridItemBinding.bind(itemView)
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(album: SongAlbum) {
            binding!!.name.text = album._name
            if (album._id <= 0L) binding!!.image!!.setImageDrawable(itemView.context!!.resources.getDrawable(R.drawable.now_playing_bar_eq_image))
            val sArtworkUri: Uri = Uri
                    .parse("content://media/external/audio/albumart")
            val uri: Uri = ContentUris.withAppendedId(sArtworkUri, album._id)
            itemView.context?.let { binding!!.image?.let { it1 -> Glide.with(it).load(uri).into(it1) } }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumsViewHolder {
        return AlbumsViewHolder(GridItemBinding.inflate(LayoutInflater.from(mContext)).root)
    }

    override fun onBindViewHolder(holder: AlbumsViewHolder, position: Int) {
        val album = albums[position]
        holder.bind(album)
        holder.binding!!.root.setOnClickListener {
            (mContext as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, AlbumTracksFragment(album._id, album._name), AlbumTracksFragment.TAG)
                    .addToBackStack("SongPlayingFragment")
                    .commit()
        }
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    fun filter_data(newList : ArrayList<SongAlbum>?){


        if(newList!=null) {
            albums = ArrayList<SongAlbum>()
            (albums as ArrayList<SongAlbum>).addAll(newList)
            notifyDataSetChanged()
        }

    }
}