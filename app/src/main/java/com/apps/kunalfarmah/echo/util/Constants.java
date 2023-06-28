package com.apps.kunalfarmah.echo.util;

public class Constants {
    public static final String WAS_MEDIA_PLAYING = "WAS_MEDIA_PLAYING";

    public interface ACTION {
        String MAIN_ACTION = "com.apps.kunalfarmah.action.main";
        String PREV_ACTION = "com.apps.kunalfarmah.action.prev";
        String PLAY_ACTION = "com.apps.kunalfarmah.action.play";
        String NEXT_ACTION = "com.apps.kunalfarmah.action.next";
        String SHUFFLE_ACTION = "com.apps.kunalfarmah.action.shuffle";
        String STARTFOREGROUND_ACTION = "com.apps.kunalfarmah.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.apps.kunalfarmah.action.stopforeground";
        String CHANGE_TO_PAUSE = "com.apps.kunalfarmah.action.changetopause";
        String CHANGE_TO_PLAY = "com.apps.kunalfarmah.action.changetoplay";
        String NEXT_UPDATE = "com.apps.kunalfarmah.action.nextupdate";
        String PREV_UPDATE = "com.apps.kunalfarmah.action.prevupdate";
        String CLOSE = "com.apps.kunalfarmah.action.close";
    }

    public static final String APP_PREFS = "APP_PREFS";
    public static final String SHUFFLE = "SHUFFLE";
    public static final String LOOP = "LOOP";
    public static final String SHAKE_TO_CHANGE = "SHAKE_TO_CHANGE";
    public static final String SORTING = "SORTING";
    public static final String NAME_ASC = "NAME_ASC";
    public static final String RECENTLY_ADDED = "RECENTLY_ADDED";

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }

}