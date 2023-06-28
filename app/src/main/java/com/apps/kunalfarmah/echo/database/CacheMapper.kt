package com.apps.kunalfarmah.echo.database


import com.apps.kunalfarmah.echo.database.entity.FavoriteEntity
import com.apps.kunalfarmah.echo.database.entity.SongAlbumEntity
import com.apps.kunalfarmah.echo.database.entity.SongsEntity
import com.apps.kunalfarmah.echo.model.SongAlbum
import com.apps.kunalfarmah.echo.model.Songs
import com.apps.kunalfarmah.echo.util.EntityMapper
import javax.inject.Inject

class CacheMapper
@Inject
constructor() :
        EntityMapper<SongsEntity, Songs> {

    override fun mapFromEntity(entity: SongsEntity): Songs {
        return Songs(
                songID = entity.songID,
                songTitle = entity.songTitle,
                songData = entity.songData,
                songAlbum = entity.songAlbum,
                artist = entity.artist,
                album = entity.album,
                dateAdded = entity.dateAdded
        )
    }

     fun mapFromFavEntity(entity: FavoriteEntity): Songs {
        return Songs(
                songID = entity.songID,
                songTitle = entity.songTitle,
                songData = entity.songData,
                songAlbum = entity.songAlbum,
                artist = entity.artist,
                album = entity.album,
                dateAdded = entity.dateAdded
        )
    }

     fun mapFromAlbumEntity(entity: SongAlbumEntity): SongAlbum {
        return SongAlbum(
                name = entity.albumName,
                id = entity.albumId
        )
    }

    override fun mapToEntity(domainModel: Songs): SongsEntity {
        return SongsEntity(
                songID = domainModel.songID,
                songTitle = domainModel.songTitle,
                songData = domainModel.songData,
                artist = domainModel.artist,
                album = domainModel.album,
                songAlbum = domainModel.songAlbum,
                dateAdded = domainModel.dateAdded
        )
    }

     fun mapToFavEntity(domainModel: Songs): FavoriteEntity {
        return FavoriteEntity(
                songID = domainModel.songID,
                songTitle = domainModel.songTitle,
                songData = domainModel.songData,
                artist = domainModel.artist,
                album = domainModel.album,
                songAlbum = domainModel.songAlbum,
                dateAdded = domainModel.dateAdded
        )
    }

    fun mapFromEntityList(entities: List<SongsEntity>): List<Songs> {
        return entities.map { mapFromEntity(it) }
    }

    fun mapFromFavEntityList(entities: List<FavoriteEntity>): List<Songs> {
        return entities.map { mapFromFavEntity(it) }
    }

    fun mapFromAlbumEntityList(entities: List<SongAlbumEntity>): List<SongAlbum> {
        return entities.map { mapFromAlbumEntity(it) }
    }

    fun mapToEntityList(songs: List<Songs>): List<SongsEntity> {
        return songs.map { mapToEntity(it) }
    }
}











