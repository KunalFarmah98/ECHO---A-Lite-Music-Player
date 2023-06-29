package com.apps.kunalfarmah.echo.database.dao

import androidx.room.*
import com.apps.kunalfarmah.echo.database.entity.FavoriteEntity
import com.apps.kunalfarmah.echo.database.entity.SongAlbumEntity
import com.apps.kunalfarmah.echo.database.entity.SongArtistEntity
import com.apps.kunalfarmah.echo.database.entity.SongsEntity

@Dao
@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
interface EchoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: SongsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFav(fav: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAlbums(songs: List<SongAlbumEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllArtists(songs: List<SongArtistEntity>)

    @Query("SELECT * FROM songs")
    suspend fun getSongs():List<SongsEntity>

    @Query("SELECT * FROM favorites")
    suspend fun getFavorites():List<FavoriteEntity>

    @Query("SELECT * FROM favorites WHERE songID = (:id)")
    suspend fun getFavoriteById(id:Long):FavoriteEntity

    @Query("SELECT * FROM songs WHERE artist = (:artist)")
    suspend fun getSongsByArtist(artist:String):List<SongsEntity>

    @Query("SELECT * FROM songs WHERE songAlbum = (:album)")
    suspend fun getSongsByAlbum(album:Long?):List<SongsEntity>

    @Query("SELECT DISTINCT songAlbum as albumId, album as albumName from songs ORDER BY albumName")
    suspend fun getAllAlbums():List<SongAlbumEntity>

    @Query("SELECT * FROM albums ORDER BY albumName")
    suspend fun getAlbums() : List<SongAlbumEntity>

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()

    @Query("DELETE FROM albums")
    suspend fun deleteAllAlbums()


}