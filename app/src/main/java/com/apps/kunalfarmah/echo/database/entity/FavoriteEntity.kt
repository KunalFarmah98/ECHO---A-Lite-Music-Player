package com.apps.kunalfarmah.echo.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
        @PrimaryKey
        @ColumnInfo(name = "songID")
        var songID: Long,
        @ColumnInfo(name = "songTitle")
        var songTitle: String,
        @ColumnInfo(name = "artist")
        var artist: String,
        @ColumnInfo(name = "album")
        var album: String,
        @ColumnInfo(name = "songData")
        var songData: String,
        @ColumnInfo(name = "dateAdded")
        var dateAdded: Long,
        @ColumnInfo(name = "songAlbum")
        var songAlbum: Long?,
)
