package com.apps.kunalfarmah.echo.database


import com.apps.kunalfarmah.echo.Songs
import com.apps.kunalfarmah.echo.database.entity.SongsEntity
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

    fun mapFromEntityList(entities: List<SongsEntity>): List<Songs> {
        return entities.map { mapFromEntity(it) }
    }

    fun mapToEntityList(songs: List<Songs>): List<SongsEntity> {
        return songs.map { mapToEntity(it) }
    }
}











