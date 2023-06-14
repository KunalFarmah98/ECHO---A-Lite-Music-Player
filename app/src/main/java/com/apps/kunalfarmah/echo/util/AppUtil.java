package com.apps.kunalfarmah.echo.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AppUtil {
    public static SharedPreferences getAppPreferences(Context context) {
        return context.getSharedPreferences(Constants.APP_PREFS, Context.MODE_PRIVATE);
    }
}
