package com.apps.kunalfarmah.echo.repository

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.apps.kunalfarmah.echo.database.CacheMapper
import com.apps.kunalfarmah.echo.database.dao.EchoDao
import com.apps.kunalfarmah.echo.model.SongAlbum
import com.apps.kunalfarmah.echo.model.Songs
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.LinkedHashSet

class SongsRepository(
        @ApplicationContext private val context: Context,
        private val echoDao: EchoDao,
        private val cacheMapper: CacheMapper
){

    suspend fun fetchSongs(){
        var songs = getSongsFromPhone()
        echoDao.deleteAllSongs()
        echoDao.insertAll(cacheMapper.mapToEntityList(songs))
    }

    suspend fun getAllSongs(): List<Songs> {
        return cacheMapper.mapFromEntityList(echoDao.getSongs())
    }

    suspend fun fetchAlbums(){
        var albums = echoDao.getAllAlbums()
        if(albums.isNotEmpty())
            echoDao.deleteAllAlbums()
        echoDao.insertAllAlbums(albums)
    }

    suspend fun getAlbums(): List<SongAlbum>{
       return cacheMapper.mapFromAlbumEntityList(echoDao.getAlbums())
    }

    suspend fun getSongsByAlbum(id:Long?) :List<Songs>{
        return cacheMapper.mapFromEntityList(echoDao.getSongsByAlbum(id))
    }


    fun getSongsFromPhone(): ArrayList<Songs> {

        val songs = LinkedHashSet<Songs>()
        var contentResolver = context.contentResolver

        val songURI =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(
                            MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
        // all music files larger than 30 seconds and are not recordings
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} <> 0 AND ${MediaStore.Audio.Media.DURATION} > 30000 AND ${MediaStore.Audio.Media.TITLE.uppercase()} NOT LIKE 'AUD%' AND ${MediaStore.Audio.Media.TITLE.uppercase()} NOT LIKE '%RECORD%' AND ${MediaStore.Audio.Media.TITLE.uppercase()} NOT LIKE 'PTT%'"
        val order = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
        val songCursor = contentResolver?.query(songURI, null, selection, null, order)

        if (songCursor != null && songCursor.moveToFirst()) {
            // getting column indices to query
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateModified = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
            val songAlbum = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val songAlbumName = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)

            while (songCursor.moveToNext()) {
                // getting the data from the indices
                var currentID = songCursor.getLong(songId)
                var currTitle = songCursor.getString(songTitle)
                var currArtist = songCursor.getString(songArtist)
                var album = songCursor.getString(songAlbumName)
                var currData = songCursor.getString(songData)
                var currdate = songCursor.getLong(dateModified)*1000
                var currAlbum = songCursor.getLong(songAlbum)

                try {
                    songs.add(Songs(currentID, currTitle,  currArtist, album, currData, currdate, currAlbum))
                }
                catch (e:Exception){
                }
            }
        }

        try {
            songCursor!!.close()
        }catch (_:Exception){}

        return ArrayList(songs)
    }
}