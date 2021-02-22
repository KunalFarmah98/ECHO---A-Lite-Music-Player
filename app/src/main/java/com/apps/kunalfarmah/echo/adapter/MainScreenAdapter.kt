package com.apps.kunalfarmah.echo.adapter

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.recyclerview.widget.RecyclerView
import com.apps.kunalfarmah.echo.Constants
import com.apps.kunalfarmah.echo.EchoNotification
import com.apps.kunalfarmah.echo.adapter.MainScreenAdapter.Statified.stopPlayingCalled
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.Songs
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.apps.kunalfarmah.echo.fragment.FavoriteFragment
import com.apps.kunalfarmah.echo.fragment.MainScreenFragment
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment
import com.apps.kunalfarmah.echo.util.AppUtil
import com.bumptech.glide.Glide
import com.google.gson.Gson

import java.io.FileDescriptor


/*This adapter class also serves the same function to act as a bridge between the single row view and its data. The implementation is quite similar to the one we did
* for the navigation drawer adapter*/
class MainScreenAdapter(_songDetails: ArrayList<Songs>, _context: Context) : RecyclerView.Adapter<MainScreenAdapter.MyViewHolder>() {

    /*Local variables used for storing the data sent from the fragment to be used in the adapter
    * These variables are initially null*/
    var songDetails: ArrayList<Songs>? = null
    var mContext: Context? = null
    lateinit var sharedPreferences: SharedPreferences

    object Statified{
        var stopPlayingCalled = false
    }

    /*In the init block we assign the data received from the params to our local variables*/
    init {
        this.songDetails = _songDetails
        this.mContext = _context
        sharedPreferences = AppUtil.getAppPreferences(mContext)
    }
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
        //var art: Bitmap? =null

        if(albumId<=0L) holder.trackArt!!.setImageResource(R.drawable.now_playing_bar_eq_image)
        val sArtworkUri: Uri = Uri
                .parse("content://media/external/audio/albumart")
        val uri: Uri = ContentUris.withAppendedId(sArtworkUri, albumId)
        mContext?.let { holder.trackArt?.let { it1 -> Glide.with(it).load(uri).into(it1) } }




        /*Handling the click event i.e. the action which happens when we click on any song*/
        holder.contentHolder?.setOnClickListener {
            val songPlayingFragment = SongPlayingFragment()

            var args = Bundle()
            args.putString("songArtist", songObject.songTitle)
            args.putString("songTitle", songObject.songTitle)
            args.putString("path", songObject.songData)
            args.putLong("SongID", songObject.songID)
            args.putLong("songAlbum", songObject.songAlbum!!)
            args.putInt("songPosition", position)

            args.putParcelableArrayList("songData", songDetails)  // sending the details as a parcel to the bundle

            songPlayingFragment.arguments = args


            stopPlaying()


            var serviceIntent = Intent(mContext, EchoNotification::class.java)

            serviceIntent.putExtra("title", songObject.songTitle)
            serviceIntent.putExtra("artist", songObject.songTitle)
            serviceIntent.putExtra("album", songObject.songAlbum!!)

            serviceIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION

            mContext?.startService(serviceIntent)

            (mContext as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, songPlayingFragment)
                    .addToBackStack("SongPlayingFragment")
                    .commit()
        }
    }
    /*This has the same implementation which we did for the navigation drawer adapter*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_custom_mainscreen_adapter, parent, false)
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
            contentHolder = view.findViewById(R.id.content_row) as RelativeLayout
        }
    }


    private fun stopPlaying() {
        if (SongPlayingFragment.Statified.mediaPlayer != null) {

            stopPlayingCalled=true

            SongPlayingFragment.Statified.mediaPlayer?.stop()
            SongPlayingFragment.Statified.mediaPlayer?.reset()

            FavoriteFragment.Statified.mediaPlayer?.stop()
            FavoriteFragment.Statified.mediaPlayer?.reset()

            MainScreenFragment.Statified.mediaPlayer?.stop()
            MainScreenFragment.Statified.mediaPlayer?.reset()

        }


    }

     fun getAlbumart(album_id: Long): Bitmap? {

        var bm: Bitmap? = null
        if(album_id <= 0L) return  BitmapFactory.decodeResource(mContext!!.resources,
                R.drawable.now_playing_bar_eq_image)
         //GlobalScope.launch {
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
             if(bm==null) bm = BitmapFactory.decodeResource(mContext!!.resources,
                     R.drawable.now_playing_bar_eq_image)
         // }
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