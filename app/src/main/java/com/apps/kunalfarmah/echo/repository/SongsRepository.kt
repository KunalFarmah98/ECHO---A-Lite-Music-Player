package com.apps.kunalfarmah.echo.repository

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.provider.MediaStore
import android.widget.Toast
import com.apps.kunalfarmah.echo.Songs
import com.apps.kunalfarmah.echo.database.CacheMapper
import com.apps.kunalfarmah.echo.database.dao.SongsDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class SongsRepository
constructor(
        @ApplicationContext private val context: Context,
        private val songsDao: SongsDao,
        private val cacheMapper: CacheMapper
){

    suspend fun fetchSongs(){
        var songs = getSongsFromPhone()
        for(song in songs)
            songsDao.insert(cacheMapper.mapToEntity(song))
    }

    suspend fun getAllSongs(): List<Songs> {
        return cacheMapper.mapFromEntityList(songsDao.getSongs())
    }

    @SuppressLint("Recycle")
    fun getSongsFromPhone(): ArrayList<Songs> {

        var arralist = ArrayList<Songs>()
        var contentResolver = context.contentResolver
        var songURI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songURI, null, null, null, null)

        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateAdded = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val songAlbum = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

            while (songCursor.moveToNext()) {
                var currentID = songCursor.getLong(songId)
                var currTitle = songCursor.getString(songTitle)
                var currArtist = songCursor.getString(songArtist)
                var currData = songCursor.getString(songData)
                var currdate = songCursor.getLong(dateAdded)
                var currAlbum = songCursor.getLong(songAlbum)


                try {
                    arralist.add(Songs(currentID, currTitle, currArtist, currData, currdate, currAlbum))
                }
                catch (e:Exception){
                    Toast.makeText(context,"Not Enough RAM to Allocate Memory and Collect Your Songs :(", Toast.LENGTH_SHORT).show()
                }
            }
        }

        try {
            removeduplicates(arralist)
        }catch (e:Exception){}

        try {
            songCursor!!.close()
        }catch (e:Exception){}

        return arralist


    }

    fun removeduplicates(list:ArrayList<Songs>) {

        // preventing index out of bounds
        try {
            for (i in 0 until list.size - 3) {
                for (j in i + 1 until list.size - 3) {
                    if (list.get(j).songTitle == list.get(i).songTitle && list.get(j).artist == list.get(i).artist)
                        list.removeAt(j)
                }
            }
        }catch (e:Exception){}
    }
}