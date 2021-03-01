package com.apps.kunalfarmah.echo.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class SongArtistEntity(
        @PrimaryKey
        @ColumnInfo(name = "artistName")
        var artistName: String,
)
