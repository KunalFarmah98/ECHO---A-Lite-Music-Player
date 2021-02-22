package com.apps.kunalfarmah.echo.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.apps.kunalfarmah.echo.database.dao.SongsDao
import com.apps.kunalfarmah.echo.database.entity.SongsEntity

@Database(entities = arrayOf(SongsEntity::class), version = 1, exportSchema = false)
abstract class EchoSongsDatabase : RoomDatabase() {
    abstract fun SongsDao(): SongsDao

    companion object {
        val DATABASE_NAME: String = "echo_songs_db"
    }
}