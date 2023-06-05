package com.apps.kunalfarmah.echo.adapter

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager.BitmapCallback
import androidx.media3.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.apps.kunalfarmah.echo.App
import com.apps.kunalfarmah.echo.EchoNotification
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.apps.kunalfarmah.echo.util.Constants
import com.apps.kunalfarmah.echo.util.SongHelper.currentSongHelper


class PlayerDescriptionAdapter() : MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): String {
        return currentSongHelper.songTitle ?: ""
    }

    override fun getCurrentContentText(player: Player): String? {
        return currentSongHelper.songArtist ?: ""
    }

    override fun getCurrentLargeIcon(player: Player,
                                     callback: BitmapCallback): Bitmap? {
        return EchoNotification.getAlbumart(App.context, currentSongHelper.songAlbum)
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        val notificationIntent = Intent(App.context, MainActivity::class.java)
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION)
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return PendingIntent.getActivity(App.context, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE)
    }
}