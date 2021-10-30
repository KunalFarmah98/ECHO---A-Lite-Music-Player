package com.apps.kunalfarmah.echo.adapter

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.apps.kunalfarmah.echo.util.Constants
import com.apps.kunalfarmah.echo.EchoNotification
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.model.Songs
import com.apps.kunalfarmah.echo.fragment.FavoriteFragment
import com.apps.kunalfarmah.echo.fragment.MainScreenFragment
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment
import com.apps.kunalfarmah.echo.util.MediaUtils.mediaPlayer
import com.bumptech.glide.Glide
import java.io.FileDescriptor

class FavoriteAdapter(_songDetails: ArrayList<Songs>, _context: Context) : RecyclerView.Adapter<FavoriteAdapter.MyViewHolder>() {

    /*Local variables used for storing the data sent from the fragment to be used in the adapter
    * These variables are initially null*/
    var songDetails: ArrayList<Songs>? = null
    var mContext: Context? = null

    /*In the init block we assign the data received from the params to our local variables*/
    init {
        this.songDetails = _songDetails
        this.mContext = _context
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val songObject = songDetails?.get(position)

        /*The holder object of our MyViewHolder class has two properties i.e
        * trackTitle for holding the name of the song and
        * trackArtist for holding the name of the artist*/
        holder.trackTitle?.text = songObject?.songTitle
        holder.trackArtist?.text = songObject?.artist

        if(holder.trackTitle?.text!!.equals("<unknown>"))
            holder.trackTitle?.text="unknown"

        if( holder.trackArtist?.text !!.equals("<unknown>"))
            holder.trackArtist?.text ="unknown"

        var albumId = songObject?.songAlbum as Long

        if(albumId<=0L) holder.trackArt!!.setImageDrawable(mContext!!.resources.getDrawable(R.drawable.now_playing_bar_eq_image))
        val sArtworkUri: Uri = Uri
                .parse("content://media/external/audio/albumart")
        val uri: Uri = ContentUris.withAppendedId(sArtworkUri, albumId)
        mContext?.let { holder.trackArt?.let { it1 -> Glide.with(it).load(uri).into(it1) } }
//        var art = getAlbumart(albumId)
//
//        if(art!=null) holder.trackArt?.setImageBitmap(art)
//        else holder.trackArt?.setImageDrawable(mContext?.resources?.getDrawable(R.drawable.now_playing_bar_eq_image))

        /*Handling the click event i.e. the action which happens when we click on any song*/
        holder.contentHolder?.setOnClickListener {

            /*Let's discuss this peice of code*/
            /*Firstly we define an object of the SongPlayingFragment*/
            val songPlayingFragment = SongPlayingFragment()

            /*A bundle is used to transfer data from one point in your activity to another
            * Here we create an object of Bundle to send the sond details to the fragment so that we can display the song details there and also play the song*/
            var args = Bundle()

            /*putString() function is used for adding a string to the bundle object
            * the string written in green is the name of the string which is placed in the bundle object with the value of that string written alongside
            * Note: Remember the name of the strings/entities you place inside the bundle object as you will retrieve them later using the same name. And these names are case-sensitive*/
            args.putString("songArtist", songObject.artist)
            args.putString("songTitle", songObject.songTitle)
            args.putString("path", songObject.songData)
            args.putLong("SongID", songObject.songID)
            args.putInt("songPosition", position)
            args.putLong("songAlbum", songObject.songAlbum as Long)

            /*Here the complete array list is sent*/
            args.putParcelableArrayList("songData", songDetails)

            /*Using this we pass the arguments to the song playing fragment*/
            songPlayingFragment.arguments = args

            stopPlaying()

            var serviceIntent = Intent(mContext, EchoNotification::class.java)

            serviceIntent.putExtra("title", songObject.songTitle)
            serviceIntent.putExtra("artist", songObject.artist)
            serviceIntent.putExtra("album", songObject.songAlbum!!)

            serviceIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION

            mContext?.startService(serviceIntent)

            /*Now after placing the song details inside the bundle, we inflate the song playing fragment*/
            (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, songPlayingFragment, SongPlayingFragment.Statified.TAG)
                    .addToBackStack(SongPlayingFragment.Statified.TAG)
                    .commit()
        }
    }

    /*This has the same implementation which we did for the navigation drawer adapter*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_custom_favorite_adapter, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {

        /*If the array list for the songs is null i.e. there are no songs in your device
        * then we return 0 and no songs are displayed*/
        if (songDetails == null) {
            return 0
        }

        /*Else we return the total size of the song details which will be the total number of song details*/
        else {
            return (songDetails as ArrayList<Songs>).size
        }
    }

    /*Every view holder class we create will serve the same purpose as it did when we created it for the navigation drawer*/
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        /*Declaring the widgets and the layout used*/
        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var trackArt: ImageView? = null
        var contentHolder: RelativeLayout? = null

        /*Constructor initialisation for the variables*/
        init {
            trackTitle = view.findViewById(R.id.tracktitle) as TextView
            trackArtist = view.findViewById(R.id.trackartist) as TextView
            trackArt = view.findViewById(R.id.album) as ImageView
            contentHolder = view.findViewById(R.id.content_row_fav) as RelativeLayout
        }
    }

    private fun stopPlaying() {
        if (mediaPlayer != null) {
            MainScreenAdapter.Statified.stopPlayingCalled=true
            mediaPlayer.stop()
        }


    }
    fun getAlbumart(album_id: Long): Bitmap? {
        var bm: Bitmap? = null
        if(album_id <= 0L) return bm
        try {
            val sArtworkUri: Uri = Uri
                    .parse("content://media/external/audio/albumart")
            val uri: Uri = ContentUris.withAppendedId(sArtworkUri, album_id)
            val pfd: ParcelFileDescriptor? = mContext!!.contentResolver
                    .openFileDescriptor(uri, "r")
            if (pfd != null) {
                val fd: FileDescriptor = pfd.fileDescriptor
                bm = BitmapFactory.decodeFileDescriptor(fd)
            }
        } catch (e: java.lang.Exception) {
        }
        return bm
    }


    fun filter_data(newList : ArrayList<Songs>?){


        if(newList!=null) {
//            songDetails?.removeAll(ArrayList<Songs>())

            songDetails = ArrayList<Songs>()
            songDetails?.addAll(newList)

            notifyDataSetChanged()
        }



    }


}