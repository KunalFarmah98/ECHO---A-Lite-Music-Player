package com.apps.kunalfarmah.echo.database.dao

import androidx.room.*
import com.apps.kunalfarmah.echo.database.entity.SongsEntity
import de.umass.lastfm.Album
import de.umass.lastfm.Artist

@Dao
@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
interface SongsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: SongsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongsEntity>)

    @Query("SELECT * FROM songs")
    suspend fun getSongs():List<SongsEntity>

    @Query("SELECT * FROM songs WHERE artist = (:artist)")
    suspend fun getSongsByArtist(artist:String):List<SongsEntity>

    @Query("SELECT * FROM songs WHERE songAlbum = (:album)")
    suspend fun getSongsByAlbum(album:String):List<SongsEntity>

}