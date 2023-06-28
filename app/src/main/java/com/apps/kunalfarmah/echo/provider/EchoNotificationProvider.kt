package com.apps.kunalfarmah.echo.provider

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player.COMMAND_PLAY_PAUSE
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.service.PlaybackService
import com.apps.kunalfarmah.echo.util.MediaUtils
import com.google.common.collect.ImmutableList

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
    override fun addNotificationActions(mediaSession: MediaSession, mediaButtons: ImmutableList<CommandButton>, builder: NotificationCompat.Builder, actionFactory: MediaNotification.ActionFactory): IntArray {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return super.addNotificationActions(mediaSession, mediaButtons, builder, actionFactory)

        val shuffleOnCommandButton = CommandButton.Builder().setSessionCommand(
                SessionCommand(PlaybackService.ShuffleActions.CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON.name, Bundle()))
                .setIconResId(R.drawable.baseline_shuffle_24)
                .setEnabled(true)
                .setDisplayName("shuffle on")
                .build()

        val shuffleOffCommandButton = CommandButton.Builder().setSessionCommand(
                SessionCommand(PlaybackService.ShuffleActions.CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF.name, Bundle()))
                .setIconResId(R.drawable.baseline_shuffle_on_24)
                .setEnabled(true)
                .setDisplayName("shuffle off")
                .build()

        val skipPreviousCommandButton = CommandButton.Builder().setPlayerCommand(
                COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .setEnabled(mediaSession.player.hasPreviousMediaItem())
                .setIconResId(androidx.media3.ui.R.drawable.exo_ic_skip_previous)
                .setExtras(Bundle().apply {
                    if(mediaSession.player.hasPreviousMediaItem())
                        putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, 0)
                })
                .build()

        val playCommandButton = CommandButton.Builder().setPlayerCommand(
                COMMAND_PLAY_PAUSE)
                .setEnabled(true)
                .setIconResId(
                        if(mediaSession.player.isPlaying)
                            androidx.media3.ui.R.drawable.exo_icon_pause
                        else
                            androidx.media3.ui.R.drawable.exo_icon_play
                )
                .setExtras(Bundle().apply {
                    if(mediaSession.player.hasPreviousMediaItem())
                        putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, 1)
                    else
                        putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, 0)
                })
                .build()

        val skipNextCommandButton = CommandButton.Builder().setPlayerCommand(
                COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .setEnabled(mediaSession.player.hasPreviousMediaItem())
                .setIconResId(androidx.media3.ui.R.drawable.exo_ic_skip_next)
                .setExtras(Bundle().apply {
                    if(mediaSession.player.hasNextMediaItem())
                        if(mediaSession.player.hasPreviousMediaItem())
                            putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, 2)
                        else
                            putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, 1)
                })
                .build()

        val closeCommandButton = CommandButton.Builder().setSessionCommand(
                SessionCommand("action_close", Bundle()))
                .setIconResId(R.drawable.baseline_close_24)
                .setEnabled(true)
                .setDisplayName("close")
                .build()

        val mediaButtonsList = mutableListOf(
                if(MediaUtils.isShuffle)
                    shuffleOffCommandButton
                else
                    shuffleOnCommandButton ,
                skipPreviousCommandButton,
                playCommandButton,
                skipNextCommandButton,
                closeCommandButton
        )

        if(!mediaSession.player.hasNextMediaItem()){
            mediaButtonsList.removeAt(3)
        }
        if(!mediaSession.player.hasPreviousMediaItem()){
            mediaButtonsList.removeAt(1)
        }

        return super.addNotificationActions(mediaSession, ImmutableList.copyOf(mediaButtonsList), builder, actionFactory)
    }
}