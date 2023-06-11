package com.apps.kunalfarmah.echo.service

import android.app.PendingIntent
import android.content.Intent
import android.widget.Toast
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.apps.kunalfarmah.echo.EchoNotification
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.apps.kunalfarmah.echo.util.MediaUtils

// Extend MediaSessionService
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    // Create your Player and MediaSession in the onCreate lifecycle event
    override fun onCreate() {
        Toast.makeText(this, "PlayBackService Running", Toast.LENGTH_SHORT).show()
        val openIntent = Intent(this, MainActivity::class.java)
        val pOpenIntent = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)
        mediaSession = MediaSession.Builder(this, MediaUtils.mediaPlayer).setSessionActivity(pOpenIntent).build()
        super.onCreate()
    }

    // Return a MediaSession to link with the MediaController that is making
    // this request.
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession?
            = mediaSession


    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        Toast.makeText(this, "PlayBackService destroyed", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }
}