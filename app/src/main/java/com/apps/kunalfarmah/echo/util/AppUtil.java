package com.apps.kunalfarmah.echo.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;


import com.apps.kunalfarmah.echo.Constants;
import com.apps.kunalfarmah.echo.R;
import com.apps.kunalfarmah.echo.fragment.AlbumTracksFragment;
import com.apps.kunalfarmah.echo.fragment.FavoriteFragment;
import com.apps.kunalfarmah.echo.fragment.MainScreenFragment;
import com.apps.kunalfarmah.echo.fragment.OfflineAlbumsFragment;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class AppUtil {

    private static final String TAG = "AppUtil";

    public static SharedPreferences getAppPreferences(Context context) {
        return context.getSharedPreferences(Constants.SETTINGS_APP_SETTINGS, Context.MODE_PRIVATE);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public static void CreateNotification(Context context, String title, String message) {

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notifyID = 1;
        String CHANNEL_ID = "exampur";// The id of the channel.
        CharSequence name = "exampur";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        Notification notification;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mNotificationManager.createNotificationChannel(mChannel);
            notificationBuilder.setChannelId(CHANNEL_ID);
        }

        notification = notificationBuilder.build();
        // Issue the notification.
        mNotificationManager.notify(notifyID, notification);
    }

    public static String getVersion(Context context) {

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                version = String.valueOf(pInfo.getLongVersionCode());
            } else {
                version = String.valueOf(pInfo.versionCode);
            }
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void OpenPlayStore(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

    public static String getDeviceId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return androidId;
    }


    public static boolean appInForeground(@NonNull Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
            if (runningAppProcess.processName.equals(context.getPackageName()) &&
                    runningAppProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getTimePassed(String dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("IST"));
        Date date = null;
        try {
            date = sdf.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long different = System.currentTimeMillis() - date.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        if (elapsedDays > 0) {
            if (elapsedDays == 1)
                return elapsedDays + " day ago";
            else
                return elapsedDays + " days ago";
        }

        if (elapsedHours > 0) {
            if (elapsedHours == 1)
                return elapsedHours + " hrs ago";
            else
                return elapsedHours + " hr ago";
        }

        if (elapsedMinutes > 0) {
            if (elapsedMinutes == 1)
                return elapsedMinutes + " min ago";
            else
                return elapsedMinutes + " mins ago";
        }

        if (elapsedSeconds > 0) {
            if (elapsedSeconds == 1)
                return elapsedSeconds + " sec ago";
            else
                return elapsedSeconds + " secs ago";
        }

        return "";
    }

    public static double truncate(double unroundedNumber) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.FLOOR);
        double result = new Double(df.format(unroundedNumber));
        return result;
    }



    public static int countChar(String str, char c) {
        int count = 0;

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c)
                count++;
        }

        return count;
    }

    public static String getFilePath(@NonNull final String url) {
        String fileName = "";
        final String dir = getSaveDir();
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        fileName = "/".concat(ts).concat(".mp4");
        return (dir + fileName);
    }


    public static String getSaveDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/e";
    }
}
