package com.apps.kunalfarmah.echo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Constants {
    public static final String SETTINGS_APP_SETTINGS = "ECHO_A_LITE_MUSIC_PLAYER";

    public interface ACTION {
        String MAIN_ACTION = "com.apps.kunalfarmah.action.main";
        String INIT_ACTION = "com.apps.kunalfarmah.action.init";
        String PREV_ACTION = "com.apps.kunalfarmah.action.prev";
        String PLAY_ACTION = "com.apps.kunalfarmah.action.play";
        String PAUSE_ACTION = "com.apps.kunalfarmah.action.pause";
        String NEXT_ACTION = "com.apps.kunalfarmah.action.next";
        String STARTFOREGROUND_ACTION = "com.apps.kunalfarmah.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.apps.kunalfarmah.action.stopforeground";
        String CHANGE_TO_PAUSE = "com.apps.kunalfarmah.action.changetopause";
        String CHANGE_TO_PLAY = "com.apps.kunalfarmah.action.changetoplay";
        String NEXT_UPDATE = "com.apps.kunalfarmah.action.nextupdate";
        String NEXT_UPDATE_SHUFFLE = "com.apps.kunalfarmah.action.nextupdateshuffle";
        String PREV_UPDATE = "com.apps.kunalfarmah.action.prevupdate";
        String PREV_UPDATE_SHUFFLE = "com.apps.kunalfarmah.action.prevupdateshuffle";
        String CLOSE = "com.apps.kunalfarmah.action.close";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }

    public static Bitmap getDefaultAlbumArt(Context context) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            bm = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.now_playing_bar_eq_image, options);
        } catch (Error ee) {
        } catch (Exception e) {
        }
        return bm;
    }

    public static final String  API_KEY = "96575af11bc84f955ad52eb7c0a951ad";
    public static final String SECRET  = "985c2b381395c365681141fb3864c973";

}