package com.apps.kunalfarmah.echo.adapter

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.apps.kunalfarmah.echo.activity.SongPlayingActivity
import com.apps.kunalfarmah.echo.databinding.RowCustomMainscreenAdapterBinding
import com.apps.kunalfarmah.echo.model.Songs
import com.apps.kunalfarmah.echo.util.MediaUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlin.math.max

class SearchAdapter(
         var songs: List<Songs>, private val mContext: Context?)
    : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(RowCustomMainscreenAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bindData(songs[position])
    }
    
    fun filter(text: String) {
        songs = songs.filter { 
            it.songTitle.contains(text, true) || it.album.contains(text,true) || it.artist.contains(text,true)
        }
        notifyDataSetChanged()
    }

    fun setList(list: List<Songs>){
        songs = list
    }

    override fun getItemCount(): Int = songs.size

    inner class SearchViewHolder(var binding: RowCustomMainscreenAdapterBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bindData(songObject: Songs) {
            binding.trackTitle.text = songObject.songTitle
            binding.trackArtist.text = songObject.artist
            binding.trackAlbum.text = songObject.album

            if (binding.trackTitle.text.equals("<unknown>"))
                binding.trackTitle.text = "unknown"

            if (binding.trackArtist.text?.equals("<unknown>") == true)
                binding.trackArtist.visibility = View.GONE

            if (binding.trackAlbum.text?.equals("<unknown>") == true)
                binding.trackAlbum.text = "Unknown Album"

            var albumId = songObject.songAlbum as Long
            //var art: Bitmap? =null

            if (albumId <= 0L) binding.album.setImageDrawable(mContext?.resources?.getDrawable(R.drawable.now_playing_bar_eq_image))
            val sArtworkUri: Uri = Uri
                    .parse("content://media/external/audio/albumart")
            val uri: Uri = ContentUris.withAppendedId(sArtworkUri, albumId)
            mContext.let { if (it != null) binding.album.let { it1 -> Glide.with(it).load(uri).placeholder(R.drawable.now_playing_bar_eq_image).diskCacheStrategy(DiskCacheStrategy.ALL).into(it1) } }


            /*Handling the click event i.e. the action which happens when we click on any song*/
            binding.contentRow.setOnClickListener {
                var intent = Intent(mContext, SongPlayingActivity::class.java)
                notifyItemChanged(max(MediaUtils.getSongIndex(), 0))
                MediaUtils.currSong = songObject
                intent.putExtra("songArtist", songObject.artist)
                intent.putExtra("songTitle", songObject.songTitle)
                intent.putExtra("path", songObject.songData)
                intent.putExtra("SongID", songObject.songID)
                intent.putExtra("songAlbum", songObject.songAlbum ?: -1)
                intent.putExtra("album", songObject.album)
                intent.putExtra("fromSearch", true)

                MediaUtils.isAllSongsPLaying = true
                MediaUtils.isAlbumPlaying = false
                MediaUtils.currAlbum = -1
                MediaUtils.isFavouritesPlaying = false

                MediaUtils.songsList = MediaUtils.allSongsList
                MediaUtils.setMediaItems()

                (mContext as? MainActivity)?.startActivity(intent)
            }
        }
    }

}