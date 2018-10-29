package com.apps.kunalfarmah.echo.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.apps.kunalfarmah.echo.Constants
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.activities.MainActivity
import com.apps.kunalfarmah.echo.fragments.SongPlayingFragment
import com.apps.kunalfarmah.echo.fragments.SongPlayingFragment.Statified.myActivity
import com.apps.kunalfarmah.echo.fragments.SongPlayingFragment.Statified.wasPlaying
import com.apps.kunalfarmah.echo.mNotification

class CaptureBroadcast : BroadcastReceiver() {


    /*The broadcast receiver has a mandatory method called the onReceive() method
    * The onReceive() method receives the intent outside the app and performs the functions accordingly
    * Here the intent will be the calling state*/
    override fun onReceive(context: Context?, intent: Intent?) {
        /*Here we check whether the user has an outgoing call or not*/
        if (intent?.action == Intent.ACTION_NEW_OUTGOING_CALL) {

            // to remove the notification when calling
            try {
                MainActivity.Statified.notificationManager?.cancel(1998)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                /*If the media player was playing we pause it and change the play/pause button*/
                if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                    wasPlaying=true
                    SongPlayingFragment.Statified.mediaPlayer?.pause()
                    SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.play_icon)

                    var play = Intent(myActivity, mNotification::class.java)
                    play.action = Constants.ACTION.CHANGE_TO_PLAY
                    myActivity?.startService(play)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // stopping music in case the headphonoes are plugged out

        else if(intent?.action == android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            try {
                if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                    wasPlaying=true
                    SongPlayingFragment.Statified.mediaPlayer?.pause()
                    SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.play_icon)

                    var play = Intent(myActivity, mNotification::class.java)
                    play.action = Constants.ACTION.CHANGE_TO_PLAY
                    myActivity?.startService(play)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        else {
            /*Here we use the telephony manager to get the access for the service*/
            val tm: TelephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            when (tm.callState) {
            /*We check the call state and if the call is ringing i.e. the user has an incoming call
            * then also we pause the media player*/
                TelephonyManager.CALL_STATE_RINGING -> {

                    try{
                        MainActivity.Statified.notificationManager?.cancel(1998)
                    }catch (e:Exception){
                        e.printStackTrace()
                    }

                    try {
                        if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                            wasPlaying=true
                            SongPlayingFragment.Statified.mediaPlayer?.pause()
                            SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.play_icon)

                            var play = Intent(myActivity, mNotification::class.java)
                            play.action = Constants.ACTION.CHANGE_TO_PLAY
                            myActivity?.startService(play)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                TelephonyManager.CALL_STATE_IDLE-> {
                    // not in call
                    try {
                        if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean == false && SongPlayingFragment.Statified.inform==false && wasPlaying) {

                            SongPlayingFragment.Statified.mediaPlayer?.start()
                            wasPlaying=false
                            SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.pause_icon)

                            var play = Intent(myActivity, mNotification::class.java)
                            play.action = Constants.ACTION.CHANGE_TO_PAUSE
                            myActivity?.startService(play)
                        }
                        else if(SongPlayingFragment.Statified.inform){
                            SongPlayingFragment.Statified.inform=false
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                TelephonyManager.CALL_STATE_OFFHOOK-> {
                    //A call is dialing, active or on hold

                    try {
                        if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                            wasPlaying=true
                            SongPlayingFragment.Statified.mediaPlayer?.pause()
                            SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.play_icon)

                            var play = Intent(myActivity, mNotification::class.java)
                            play.action = Constants.ACTION.CHANGE_TO_PLAY
                            myActivity?.startService(play)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                else -> {
                    /*Else we do nothing*/
                }
            }
        }
    }
}