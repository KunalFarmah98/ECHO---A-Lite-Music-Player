package com.apps.kunalfarmah.echo.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.apps.kunalfarmah.echo.Database.EchoDatabase.Staticated.COLUMN_ID
import com.apps.kunalfarmah.echo.Database.EchoDatabase.Staticated.COLUMN_SONG_ARTIST
import com.apps.kunalfarmah.echo.Database.EchoDatabase.Staticated.COLUMN_SONG_PATH
import com.apps.kunalfarmah.echo.Database.EchoDatabase.Staticated.COLUMN_SONG_TITLE
import com.apps.kunalfarmah.echo.Database.EchoDatabase.Staticated.DB_NAME
import com.apps.kunalfarmah.echo.Database.EchoDatabase.Staticated.DB_VERSION
import com.apps.kunalfarmah.echo.Database.EchoDatabase.Staticated.TABLE_NAME
import com.apps.kunalfarmah.echo.R.id.songTitle
import com.apps.kunalfarmah.echo.Songs

/*This class is created for managing the database for our application
* In Android we use SQLite database for storing the data
* We create this table for keeping the data which is used even after the app is closed
* Now you may think how it is different from SharedPreferences?
* Shared preferences can store very small amount of data only whereas SQLite has the ability to store huge amounts of data
* Therefore, in our application we will be using the SQLite database to store the favorite tracks*/
class EchoDatabase : SQLiteOpenHelper {

    /*List for storing the favorite songs*/
    var _songList = ArrayList<Songs>()






    object Staticated{
        val DB_NAME = "FavoriteDatabase"
        var DB_VERSION = 1


        val TABLE_NAME = "FavoriteTable"
        val COLUMN_ID = "SongID"
        val COLUMN_SONG_TITLE = "SongTitle"
        val COLUMN_SONG_ARTIST = "SongArtist"
        val COLUMN_SONG_PATH = "SongPath"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE " + TABLE_NAME + "( " + COLUMN_ID +
                " INTEGER," + COLUMN_SONG_ARTIST + " STRING," + COLUMN_SONG_TITLE + " STRING,"
                + COLUMN_SONG_PATH + " STRING);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(context, name, factory, version)

    constructor(context: Context?) : super(context,Staticated.DB_NAME, null, Staticated.DB_VERSION)

    fun storeAsFavorite(id: Int?, artist: String?, songTitle: String?, path: String?) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_ID, id)
        contentValues.put(COLUMN_SONG_ARTIST, artist)
        contentValues.put(COLUMN_SONG_TITLE, songTitle)
        contentValues.put(COLUMN_SONG_PATH, path)
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }

    /*This method asks the database for the list of Songs stored as favorite*/
    fun queryDBList(): ArrayList<Songs>? {

        /*Here a try-catch block is used to handle the exception as no songs in the database can result in null-pointer exception*/

        try {
            val db = this.readableDatabase

            /*The SQL query used for obtaining the songs is :
            * SELECT * FROM FavoriteTable
            * The query returns all the items present in the table*/
            val query_params = "SELECT * FROM " + TABLE_NAME

            //setting a cursor to fetch the row

            var cSor = db.rawQuery(query_params, null)

            /*The cSor stores the result obtained from the database
            * The function moveToFirst() checks if there are any entries or not*/
            if (cSor.moveToFirst()) {

                /*If 1 or more rows are returned then we store all the entries into the array list _songList*/
                do {

                    var _id = cSor.getInt(cSor.getColumnIndexOrThrow(COLUMN_ID))
                    var _artist = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_ARTIST))
                    var _title = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_TITLE))
                    var _songPath = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_PATH))
                    _songList.add(Songs(_id.toLong(), _title, _artist, _songPath, 0))
                }

                /*This task is performed till there are items present*/
                while (cSor.moveToNext())
            }

            /*Otherwise null is returned*/
            else {
                return null
            }
        }

        /*If there was any exception then it is handled by this*/
        catch (e: Exception) {
            e.printStackTrace()
        }

        /*Finally we return the songList which contains the songs present inside the database*/
        return _songList
    }

    /*This function is created for checking whether a particular song is a favorite or not*/
    fun checkifIdExists(_id: Int): Boolean {

        /*Random id which does not exist
        * We know that this id can never exist as the song id cannot be less than 0*/
        var storeId = -1090
        val db = this.readableDatabase

        /*The query for checking the if id is present or not is
        * SELECT * FROM FavoriteTable WHERE SongID = <id_of_our_song>*/
        val query_params = "SELECT * FROM " + TABLE_NAME + " WHERE SongID = '$_id'"
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()) {
            do {

                /*Storing the song id into the variable storeId*/
                storeId = cSor.getInt(cSor.getColumnIndexOrThrow(COLUMN_ID))
            } while (cSor.moveToNext())
        } else {
            return false
        }

        /*Here we need to return a boolean value i.e. true or false
        * Hence we check if the store id is not equal to -1090 then we return true, else we return false*/
        return storeId != -1090
    }

    /*This function is used to delete the songs from the favorite if the user the user removes any song from the favorite list*/
    fun deleteFavourite(_id: Int) {
        val db = this.writableDatabase

        /*The delete query is used to perform the delete function*/
        db.delete(TABLE_NAME, COLUMN_ID + " = " + _id, null)

        /*Here is also we close the database connection
        * Note that we only close the database whenever we open in writable mode*/
        db.close()
    }


    // returning the no of rows in the db

    fun checkSize() : Int{
        var counter = 0

        val db = this.readableDatabase

        /*The query for checking the if id is present or not is
        * SELECT * FROM FavoriteTable WHERE SongID = <id_of_our_song>*/
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