package com.apps.kunalfarmah.echo.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AppUtil {
    public static SharedPreferences getAppPreferences(Context context) {
        return context.getSharedPreferences(Constants.SETTINGS_APP_SETTINGS, Context.MODE_PRIVATE);
    }
}
