package com.apps.kunalfarmah.echo

import android.graphics.Bitmap

class CurrentSongHelper{
    var songTitle: String? = null
    var songArtist: String? = null
    var songpath:String?=null
    var songAlbum:Long? = null

    var songId: Long? = 0

    var currentPosition:Int?=0

    var isPlaying:Boolean =false
    var isLoop:Boolean=false
    var isShuffle:Boolean=false
    var trackPosition:Int = 0


}