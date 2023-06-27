package com.apps.kunalfarmah.echo.provider

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
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

    override fun getMediaButtons(session: MediaSession, playerCommands: Player.Commands, customLayout: ImmutableList<CommandButton>, showPauseButton: Boolean): ImmutableList<CommandButton> {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU) {
            return super.getMediaButtons(session, playerCommands, customLayout, showPauseButton)
        }
        var customCommands = listOf(
                CommandButton.Builder().setSessionCommand(
                        SessionCommand(
                                if (!MediaUtils.isShuffle)
                                    PlaybackService.ShuffleActions.CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON.name
                                else
                                    PlaybackService.ShuffleActions.CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF.name
                                , Bundle())
                        )
                        .setIconResId(if(!MediaUtils.isShuffle)
                            R.drawable.baseline_shuffle_24
                        else
                            R.drawable.baseline_shuffle_on_24)
                        .setEnabled(true)
                        .setDisplayName("shuffle on")
                        .build(),
                CommandButton.Builder().setPlayerCommand(Player.COMMAND_SEEK_TO_PREVIOUS)
                        .setIconResId(android.R.drawable.ic_media_previous)
                        .setEnabled(true)
                        .setDisplayName("previous")
                        .build(),
                CommandButton.Builder().setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
                        .setIconResId(
                                if(MediaUtils.mediaPlayer.isPlaying)
                                    android.R.drawable.ic_media_pause
                                else
                                    android.R.drawable.ic_media_play
                        )
                        .setEnabled(true)
                        .setDisplayName("play/pause")
                        .build(),
                CommandButton.Builder().setPlayerCommand(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                        .setIconResId(android.R.drawable.ic_media_next)
                        .setEnabled(true)
                        .setDisplayName("next")
                        .build(),

                CommandButton.Builder().setSessionCommand(
                        SessionCommand("action_close", Bundle()))
                        .setIconResId(R.drawable.baseline_close_24)
                        .setEnabled(true)
                        .setDisplayName("close")
                        .build()

        )

        return ImmutableList.copyOf(customCommands)

    }
}