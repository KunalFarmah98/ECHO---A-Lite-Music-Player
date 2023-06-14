package com.apps.kunalfarmah.echo.provider

import android.content.Context
import androidx.media3.common.MediaMetadata
import androidx.media3.session.DefaultMediaNotificationProvider
import com.apps.kunalfarmah.echo.util.MediaUtils

class EchoNotificationProvider(context: Context): DefaultMediaNotificationProvider(context) {

    override fun getNotificationContentTitle(metadata: MediaMetadata): CharSequence? {
        val title =  super.getNotificationContentTitle(metadata)
        return if(!title.isNullOrEmpty())
            title
        else{
            val index = MediaUtils.mediaPlayer.currentMediaItemIndex
            val song = MediaUtils.songsList[index]
            var titleToDisplay = song.songTitle
            if (titleToDisplay == "null" || titleToDisplay.equals("<unknown>", true)) {
                titleToDisplay = "Unknown"
            }
            titleToDisplay
        }
    }

    override fun getNotificationContentText(metadata: MediaMetadata): CharSequence? {
        val subTitle =  super.getNotificationContentText(metadata)
        return if(!subTitle.isNullOrEmpty())
            subTitle
        else{
            val index = MediaUtils.mediaPlayer.currentMediaItemIndex
            val song = MediaUtils.songsList[index]
            var subTitleToDisplay = song.artist
            if (subTitleToDisplay == "null" || subTitleToDisplay.equals("<unknown>", true)) {
                subTitleToDisplay = "Unknown"
            }
            subTitleToDisplay
        }
    }
}