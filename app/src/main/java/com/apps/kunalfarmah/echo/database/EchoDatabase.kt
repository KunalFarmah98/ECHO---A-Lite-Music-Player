package com.apps.kunalfarmah.echo.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.apps.kunalfarmah.echo.database.EchoDatabase.Staticated.COLUMN_ID
import com.apps.kunalfarmah.echo.database.EchoDatabase.Staticated.COLUMN_SONG_ALBUM
import com.apps.kunalfarmah.echo.database.EchoDatabase.Staticated.COLUMN_SONG_ALBUM_NAME
import com.apps.kunalfarmah.echo.database.EchoDatabase.Staticated.COLUMN_SONG_ARTIST
import com.apps.kunalfarmah.echo.database.EchoDatabase.Staticated.COLUMN_SONG_PATH
import com.apps.kunalfarmah.echo.database.EchoDatabase.Staticated.COLUMN_SONG_TITLE
import com.apps.kunalfarmah.echo.database.EchoDatabase.Staticated.TABLE_NAME
import com.apps.kunalfarmah.echo.model.Songs

class EchoDatabase : SQLiteOpenHelper {

    var _songList = ArrayList<Songs>()
    var context:Context? = null

    object Staticated{
        val DB_NAME = "FavoriteDatabase"
        var DB_VERSION = 14


        val TABLE_NAME = "FavoriteTable"
        val COLUMN_ID = "SongID"
        val COLUMN_SONG_TITLE = "SongTitle"
        val COLUMN_SONG_ARTIST = "SongArtist"
        val COLUMN_SONG_PATH = "SongPath"
        val COLUMN_SONG_ALBUM = "SongAlbum"
        val COLUMN_SONG_ALBUM_NAME = "SongAlbumName"

    }

    @SuppressLint("SQLiteString")
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE " + TABLE_NAME + "( " + COLUMN_ID +
                " INTEGER," + COLUMN_SONG_ARTIST + " STRING," + COLUMN_SONG_TITLE + " STRING,"
                + COLUMN_SONG_PATH + " STRING," + COLUMN_SONG_ALBUM + " STRING," + COLUMN_SONG_ALBUM_NAME + " STRING);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if(oldVersion<newVersion){
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        }
        onCreate(db)
    }

    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(context, name, factory, version)

    constructor(context: Context?) : super(context,Staticated.DB_NAME, null, Staticated.DB_VERSION)

    fun storeAsFavorite(id: Int?, artist: String?, songTitle: String?, path: String?, album: Long?, albumName: String?) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_ID, id)
        contentValues.put(COLUMN_SONG_ARTIST, artist)
        contentValues.put(COLUMN_SONG_TITLE, songTitle)
        contentValues.put(COLUMN_SONG_PATH, path)
        contentValues.put(COLUMN_SONG_ALBUM, album.toString())
        contentValues.put(COLUMN_SONG_ALBUM_NAME, albumName)
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }


    fun queryDBList(): ArrayList<Songs>? {


        try {
            val db = this.readableDatabase
            val query_params = "SELECT * FROM " + TABLE_NAME
            var cSor = db.rawQuery(query_params, null)
            if (cSor.moveToFirst()) {
                do {
                    var _id = cSor.getInt(cSor.getColumnIndexOrThrow(COLUMN_ID))
                    var _artist = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_ARTIST))
                    var _title = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_TITLE))
                    var _songPath = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_PATH))
                    var _songAlbum = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_ALBUM))
                    var _songAlbumName = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_ALBUM_NAME))
                    if(_songAlbumName.isNullOrEmpty())
                        _songAlbumName = "<unknown>"
                    _songList.add(Songs(_id.toLong(), _title, _artist, _songAlbumName,_songPath, 0,_songAlbum.toLong()))
                }

                while (cSor.moveToNext())
            }
            else {
                return null
            }
        }

        catch (e: Exception) {
            e.printStackTrace()
        }

        return _songList
    }

    fun checkifIdExists(_id: Int): Boolean {

        var storeId = -1090
        val db = this.readableDatabase

        val query_params = "SELECT * FROM " + TABLE_NAME + " WHERE SongID = '$_id'"
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()) {
            do {
                storeId = cSor.getInt(cSor.getColumnIndexOrThrow(COLUMN_ID))
            } while (cSor.moveToNext())
        } else {
            return false
        }
        return storeId != -1090
    }

    fun deleteFavourite(_id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, COLUMN_ID + " = " + _id, null)
        db.close()
    }


    fun checkSize() : Int{
        var counter = 0

        val db = this.readableDatabase

        val query_params = "SELECT * FROM " + TABLE_NAME
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()) {
            do {
                ++counter

            } while (cSor.moveToNext())
        } else {
            return 0
        }

        return counter
    }
}