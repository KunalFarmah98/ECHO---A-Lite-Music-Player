package com.apps.kunalfarmah.echo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AppUtil {
    public static SharedPreferences getAppPreferences(Context context) {
        return context.getSharedPreferences(Constants.APP_PREFS, Context.MODE_PRIVATE);
    }

    public static byte[] convertVideoToBytes(Context context, Uri uri) {
        byte[] videoBytes = null;
        try {//  w  w w  . j ava 2s . c  o m
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            FileInputStream fis = new FileInputStream(new File(String.valueOf(uri)));
            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fis.read(buf)))
                baos.write(buf, 0, n);

            videoBytes = baos.toByteArray();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return videoBytes;
    }
}
