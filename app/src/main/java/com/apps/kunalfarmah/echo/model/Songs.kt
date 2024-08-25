package com.apps.kunalfarmah.echo.model


import android.os.Parcel
import android.os.Parcelable

data class Songs(var songID :Long, var songTitle:String, var artist:String, var album:String, var songData: String, var dateAdded:Long, var songAlbum:Long?  ) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()?:"Unknown",
        parcel.readString()?:"Unknown",
        parcel.readString()?:"Unknown",
        parcel.readString()?:"",
        parcel.readLong(),
        parcel.readValue(Long::class.java.classLoader) as? Long
    ) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(songID)
        dest.writeString(songTitle)
        dest.writeString(artist)
        dest.writeString(album)
        dest.writeString(songData)
        dest.writeLong(dateAdded)
        dest.writeValue(songAlbum)
    }

    override fun describeContents(): Int {
       return 0
    }

    companion object CREATOR : Parcelable.Creator<Songs> {
        override fun createFromParcel(parcel: Parcel): Songs {
            return Songs(parcel)
        }

        override fun newArray(size: Int): Array<Songs?> {
            return arrayOfNulls(size)
        }
    }

}