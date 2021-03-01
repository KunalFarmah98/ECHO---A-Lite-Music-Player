package com.apps.kunalfarmah.echo.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class SongAlbumEntity(
        @PrimaryKey
        @ColumnInfo(name = "albumId")
        var albumId: Long,
        @ColumnInfo(name = "albumName")
        var albumName: String,
)
