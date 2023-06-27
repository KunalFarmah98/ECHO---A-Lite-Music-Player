package com.apps.kunalfarmah.echo.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.Player
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.apps.kunalfarmah.echo.App
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.apps.kunalfarmah.echo.activity.SongPlayingActivity.Companion.instance
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment
import com.apps.kunalfarmah.echo.provider.EchoNotificationProvider
import com.apps.kunalfarmah.echo.util.AppUtil
import com.apps.kunalfarmah.echo.util.Constants
import com.apps.kunalfarmah.echo.util.EchoBitmapLoader
import com.apps.kunalfarmah.echo.util.MediaUtils
import com.apps.kunalfarmah.echo.util.MediaUtils.currSong
import com.apps.kunalfarmah.echo.util.MediaUtils.mediaPlayer
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class PlaybackService : MediaSessionService(), MediaSession.Callback {

    companion object {
        var mInstance: PlaybackService ?= null
    }
    private var mediaSession: MediaSession? = null
    private var customLayout = listOf<CommandButton>()

    private var customCommands = listOf(
            CommandButton.Builder().setSessionCommand(
                    SessionCommand(ShuffleActions.CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON.name, Bundle()))
                    .setIconResId(R.drawable.baseline_shuffle_24)
                    .setEnabled(true)
                    .setDisplayName("shuffle on")
                    .build(),
            CommandButton.Builder().setSessionCommand(
                    SessionCommand(ShuffleActions.CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF.name, Bundle()))
                    .setIconResId(R.drawable.baseline_shuffle_on_24)
                    .setEnabled(true)
                    .setDisplayName("shuffle off")
                    .build(),
            CommandButton.Builder().setSessionCommand(
                    SessionCommand("action_close", Bundle()))
                    .setIconResId(R.drawable.baseline_close_24)
                    .setEnabled(true)
                    .setDisplayName("close")
                    .build()
    )

    var customCommandsLegacy = listOf(
            CommandButton.Builder().setSessionCommand(
                    SessionCommand(ShuffleActions.CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF.name, Bundle()))
                    .setIconResId(
                            R.drawable.baseline_shuffle_24
                    )
                    .setEnabled(true)
                    .setDisplayName("shuffle on")
                    .build(),
            CommandButton.Builder().setSessionCommand(
                    SessionCommand(ShuffleActions.CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF.name, Bundle()))
                    .setIconResId(R.drawable.baseline_shuffle_on_24)
                    .setEnabled(true)
                    .setDisplayName("shuffle off")
                    .build(),
            CommandButton.Builder().setPlayerCommand(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .setIconResId(android.R.drawable.ic_media_previous)
                    .setEnabled(true)
                    .setDisplayName("previous")
                    .build(),
            CommandButton.Builder().setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
                    .setIconResId(
                            if(mediaPlayer.isPlaying)
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
    enum class ShuffleActions(name: String) {
        CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON("action_shuffle_on"),
        CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF("action_shuffle_on")
    }

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        if(session.player.playbackState == Player.STATE_BUFFERING)
            return
        super.onUpdateNotification(session, startInForegroundRequired)
    }

    // Create your Player and MediaSession in the onCreate lifecycle event
    override fun onCreate() {
        Toast.makeText(this, "PlayBackService Running", Toast.LENGTH_SHORT).show()
        mInstance = this
        this.setMediaNotificationProvider(EchoNotificationProvider(this))
        val openIntent = Intent(this, MainActivity::class.java)
        val pOpenIntent = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

        mediaSession = MediaSession.Builder(this, mediaPlayer)
                .setSessionActivity(pOpenIntent)
                .setBitmapLoader(EchoBitmapLoader())
                .setCallback(this)
                .build()

        mediaSession?.setCustomLayout(getCustomLayout())
        super.onCreate()
    }

    private fun getCustomLayout(): List<CommandButton> {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (MediaUtils.isShuffle) {
                 listOf(customCommands[1], customCommands[2])
            } else {
                listOf(customCommands[0], customCommands[2])
            }
        }
        else{
            if (MediaUtils.isShuffle) {
                listOf(customCommandsLegacy[1], customCommandsLegacy[2], customCommandsLegacy[3], customCommandsLegacy[4], customCommandsLegacy[5])
            } else {
                listOf(customCommandsLegacy[0], customCommandsLegacy[2], customCommandsLegacy[3], customCommandsLegacy[4], customCommandsLegacy[5])
            }
        }
    }

    // Return a MediaSession to link with the MediaController that is making
    // this request.
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    fun setCustomLayoutForShuffle(isShuffle: Boolean?){
        mediaPlayer.shuffleModeEnabled = (isShuffle == true)
        mediaSession?.setCustomLayout(getCustomLayout())
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        Toast.makeText(this, "PlayBackService destroyed", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }

    override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)
        val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
        customCommands.forEach { commandButton ->
            // Add custom command to available session commands.
            commandButton.sessionCommand?.let { availableSessionCommands.add(it) }
        }
        return MediaSession.ConnectionResult.accept(
                availableSessionCommands.build(),
                connectionResult.availablePlayerCommands
        )
    }

    override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
    ): ListenableFuture<SessionResult> {
        if (ShuffleActions.CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON.name == customCommand.customAction) {
            // Enable shuffling.
//            session.player.shuffleModeEnabled = true
            SongPlayingFragment.Statified.shufflebutton.callOnClick()
            // Change the custom layout to contain the `Disable shuffling` command and send the updated custom layout to controllers.
            //customLayout = listOf(customCommands[1], customCommands[2])
            session.setCustomLayout(getCustomLayout())
        } else if (ShuffleActions.CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF.name == customCommand.customAction) {
            // Disable shuffling.
//            session.player.shuffleModeEnabled = false
            SongPlayingFragment.Statified.shufflebutton.callOnClick()
            // Change the custom layout to contain the `Enable shuffling` command and send the updated custom layout to controllers.
           // customLayout = listOf(customCommands[0], customCommands[2])
            session.setCustomLayout(getCustomLayout())
        }
        else if(customCommand.customAction == "action_close" ){
            killApp()
        }
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }

    override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
        if (customLayout.isNotEmpty()) {
            // Let the controller now about the custom layout right after it connected.
            mediaSession?.setCustomLayout(controller, getCustomLayout())
        }
    }

    fun killApp(){
        val localBroadcastManager = LocalBroadcastManager
                .getInstance(this)
        localBroadcastManager.sendBroadcast(Intent(
                Constants.ACTION.CLOSE))

        val act = instance
        val main=  MainActivity()
        try {
            mediaPlayer.stop()
//            mediaPlayer.release()
            currSong = null
            main.setNotify_val(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                act?.onBackPressed()
                main.finishAndRemoveTask()
            } else {
                act?.onBackPressed()
                main.finishAffinity()
            }
        } catch (e: Exception) {
        }
    }

}