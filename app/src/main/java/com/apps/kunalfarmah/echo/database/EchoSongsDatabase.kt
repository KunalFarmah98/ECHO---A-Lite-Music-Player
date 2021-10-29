package com.apps.kunalfarmah.echo.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.apps.kunalfarmah.echo.database.dao.EchoDao
import com.apps.kunalfarmah.echo.database.entity.FavoriteEntity
import com.apps.kunalfarmah.echo.database.entity.SongAlbumEntity
import com.apps.kunalfarmah.echo.database.entity.SongArtistEntity
import com.apps.kunalfarmah.echo.database.entity.SongsEntity

@Database(entities = arrayOf(SongsEntity::class, SongAlbumEntity::class, SongArtistEntity::class, FavoriteEntity::class), version = 2, exportSchema = false)
abstract class EchoSongsDatabase : RoomDatabase() {
    abstract fun SongsDao(): EchoDao

    companion object {
        val DATABASE_NAME: String = "echo_songs_db"
    }
}