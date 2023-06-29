package com.apps.kunalfarmah.echo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.apps.kunalfarmah.echo.R;
import com.apps.kunalfarmah.echo.activity.MainActivity;
import com.apps.kunalfarmah.echo.activity.SongPlayingActivity;
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment;
import com.apps.kunalfarmah.echo.util.Constants;
import com.apps.kunalfarmah.echo.util.MediaUtils;
import com.apps.kunalfarmah.echo.viewModel.SongsViewModel;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;


/**
 * http://www.tutorialsface.com/2015/08/android-custom-notification-tutorial/
 * https://stackoverflow.com/questions/22789588/how-to-update-notification-with-remoteviews
 */

@AndroidEntryPoint
public class EchoNotification extends Service {


    @Inject
    SongsViewModel songsViewModel;
    ArrayList<String> thoughts;


    MainActivity main;
    String title = "";
    String artist = "";
    Long albumID;
    SongPlayingFragment msong;
    RemoteViews views;
    RemoteViews smallviews;
    ImageView imageView;
    String CHANNEL_ID = "Echo_Music";// The id of the channel.
    CharSequence name = "Echo-Notification";
    Notification status;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        msong = new SongPlayingFragment();
        main = new MainActivity();
        thoughts = new ArrayList<String>();
        thoughts.add("ECHO TIME!");
        thoughts.add("FUN TIME!");
        thoughts.add("LOST DREAMS!");
        thoughts.add("DANCE BABY!");
        thoughts.add("GO ECHO");
        thoughts.add("NIRVANA!");
        thoughts.add("LIVE MUSIC!");
        thoughts.add("CAN'T SLEEP?");
        thoughts.add("PAIN GONE");
        thoughts.add("LOST WORLD");
        thoughts.add("BE HAPPY");
        thoughts.add("TUNES ON");
        thoughts.add("FULL ON");
        thoughts.add("SING ALONG");
        thoughts.add("PLAY ALONG");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            if (null == intent || intent.getAction() == null) {
                stopForeground(true);
                stopSelf();
                super.onStartCommand(intent,flags,startId);
            }

            if (null != intent && intent.getAction() != null
                    && intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {

                title = intent.getStringExtra("title");
                artist = intent.getStringExtra("artist");
                albumID = intent.getLongExtra("album", -1);
                main.setNotify_val(true);
                showNotification();

            } else if (null != intent && intent.getAction() != null
                    && intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {

                msong.previous();
                views.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);
                smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);

                Bitmap img = getAlbumart(getBaseContext(), albumID);
                if (img != null) {
                    views.setImageViewBitmap(R.id.song_image, img);
                    smallviews.setImageViewBitmap(R.id.song_image, img);
                } else {
                    views.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                    smallviews.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                }

                updateNotiUI();


            } else if (null != intent && intent.getAction() != null
                    && intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {

                msong.setPlay(msong.playorpause());

                if (msong.getPlay() == false) {
                    songsViewModel.setPlayStatus(false);
                    views.setImageViewResource(R.id.playpausebutton_not, R.drawable.play_icon);
                    smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.play_icon);

                } else {
                    songsViewModel.setPlayStatus(true);
                    views.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);
                    smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);
                }


                updateNotiUI();

            } else if (null != intent && intent.getAction() != null
                    && intent.getAction().equals(Constants.ACTION.CHANGE_TO_PAUSE)) {
                songsViewModel.setPlayStatus(true);
                views.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);
                smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);
                updateNotiUI();
            } else if (null != intent && intent.getAction() != null
                    && intent.getAction().equals(Constants.ACTION.CHANGE_TO_PLAY)) {
                songsViewModel.setPlayStatus(false);
                views.setImageViewResource(R.id.playpausebutton_not, R.drawable.play_icon);
                smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.play_icon);

                updateNotiUI();

            } else if (null != intent && intent.getAction() != null
                    && intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
                msong.next();
                views.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);
                smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);

                Bitmap img = getAlbumart(getBaseContext(), albumID);
                if (img != null) {
                    views.setImageViewBitmap(R.id.song_image, img);
                    smallviews.setImageViewBitmap(R.id.song_image, img);
                } else {
                    views.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                    smallviews.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                }

                updateNotiUI();


            } else if (null != intent && intent.getAction() != null
                    && intent.getAction().equals(Constants.ACTION.NEXT_UPDATE)) {

                title = intent.getStringExtra("title");
                artist = intent.getStringExtra("artist");
                albumID = intent.getLongExtra("album", -1);


                if (title == null || title.equals("<unknown>"))
                    title = "Unknown";

                if (artist == null || artist.equals("<unknown>"))
                    artist = "unknown";

                views.setTextViewText(R.id.song_title_nav, title);
                views.setTextViewText(R.id.song_artist_nav, artist);
                views.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);
                smallviews.setTextViewText(R.id.song_title_nav, title);
                smallviews.setTextViewText(R.id.song_artist_nav, artist);
                smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);

                Bitmap img = getAlbumart(getBaseContext(), albumID);
                if (img != null) {
                    views.setImageViewBitmap(R.id.song_image, img);
                    smallviews.setImageViewBitmap(R.id.song_image, img);
                } else {
                    views.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                    smallviews.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                }


                updateNotiUI();
            } else if (null != intent && intent.getAction() != null
                    && intent.getAction().equals(Constants.ACTION.PREV_UPDATE)) {
                title = intent.getStringExtra("title");
                artist = intent.getStringExtra("artist");
                albumID = intent.getLongExtra("album", -1);

                if (title == null || title.equals("<unknown>"))
                    title = "Unknown";

                if (artist == null || artist.equals("<unknown>"))
                    artist = "unknown";
                views.setTextViewText(R.id.song_title_nav, title);
                views.setTextViewText(R.id.song_artist_nav, artist);
                views.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);

                smallviews.setTextViewText(R.id.song_title_nav, title);
                smallviews.setTextViewText(R.id.song_artist_nav, artist);
                smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);

                Bitmap img = getAlbumart(getBaseContext(), albumID);
                if (img != null) {
                    views.setImageViewBitmap(R.id.song_image, img);
                    smallviews.setImageViewBitmap(R.id.song_image, img);
                } else {
                    views.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                    smallviews.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                }


                updateNotiUI();

            } else if (null != intent && intent.getAction() != null
                    && intent.getAction().equals(Constants.ACTION.SHUFFLE_ACTION)) {
                SongPlayingFragment.Statified.shufflebutton.callOnClick();
            } else if (null != intent && intent.getAction() != null
                    && intent.getAction().equals(
                    Constants.ACTION.STOPFOREGROUND_ACTION)) {

                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                        .getInstance(this);
                localBroadcastManager.sendBroadcast(new Intent(
                        Constants.ACTION.CLOSE));

                msong.unregister();
                SongPlayingActivity act = SongPlayingActivity.Companion.getInstance();

                try {
                    MediaUtils.INSTANCE.getMediaPlayer().stop();
                    MediaUtils.INSTANCE.getMediaPlayer().setPlayWhenReady(false);
                    //MediaUtils.INSTANCE.getMediaPlayer().release();
                    MediaUtils.INSTANCE.setCurrSong(null);
                    main.setNotify_val(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if(act!=null)
                            act.onBackPressed();
                        main.finishAndRemoveTask();
                    }
                    else{
                        if(act!=null)
                            act.onBackPressed();
                        main.finishAffinity();
                    }
                } catch (Exception e) {
                }
                stopForeground(true);
                stopSelf();

            }
            return START_STICKY;
        } catch (Exception e) {
            if(e.getMessage()!=null)
                FirebaseCrashlytics.getInstance().log("EchoNotification: "+e.getMessage());
            return START_STICKY;
        }
    }

    private void showNotification() {
        int pendingIntentFlag = Build.VERSION.SDK_INT<Build.VERSION_CODES.S ? 0 : PendingIntent.FLAG_IMMUTABLE;
// Using RemoteViews to bind custom layouts into Notification
        views = new RemoteViews(getPackageName(),
                R.layout.notification_bar);

        smallviews = new RemoteViews(getPackageName(),
                R.layout.notificaiton_smalll);


        Intent openIntent = new Intent(this, EchoNotification.class);
        PendingIntent pOpenIntent = PendingIntent.getActivity(this, 0, openIntent, pendingIntentFlag);


        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, pendingIntentFlag);

        Intent previousIntent = new Intent(this, EchoNotification.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, pendingIntentFlag);

        Intent playIntent = new Intent(this, EchoNotification.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, pendingIntentFlag);

        Intent nextIntent = new Intent(this, EchoNotification.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, pendingIntentFlag);

        Intent closeIntent = new Intent(this, EchoNotification.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, pendingIntentFlag);

        views.setOnClickPendingIntent(R.id.playpausebutton_not, pplayIntent);
        smallviews.setOnClickPendingIntent(R.id.playpausebutton_not, pplayIntent);


        views.setOnClickPendingIntent(R.id.nextbutton_not, pnextIntent);
        smallviews.setOnClickPendingIntent(R.id.nextbutton_not, pnextIntent);


        views.setOnClickPendingIntent(R.id.previousbutton_not, ppreviousIntent);
        smallviews.setOnClickPendingIntent(R.id.previousbutton_not, ppreviousIntent);


        views.setOnClickPendingIntent(R.id.close, pcloseIntent);
        smallviews.setOnClickPendingIntent(R.id.close, pcloseIntent);


        // setting thoughts of the day
        Random randomObject = new Random();                                                            // initialising a random object of the random class
        int randomPosition = randomObject.nextInt(thoughts.size() + 1);                // setting range of random to size+1
        int currentPosition = randomPosition;

        if (currentPosition == thoughts.size()) {    // if the currentposition exceeds the size, start over
            currentPosition = 0;
        }

        views.setTextViewText(R.id.logo, thoughts.get(currentPosition));


        if (title == null || title.equals("<unknown>"))
            title = "Unknown";

        if (artist == null || artist.equals("<unknown>"))
            artist = "unknown";

        views.setTextViewText(R.id.song_title_nav, title);
        smallviews.setTextViewText(R.id.song_title_nav, title);


        views.setTextViewText(R.id.song_artist_nav, artist);
        smallviews.setTextViewText(R.id.song_artist_nav, artist);


        views.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);
        smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);


        Bitmap img = getAlbumart(getBaseContext(), albumID);
        if (img != null) {
            views.setImageViewBitmap(R.id.song_image, img);
            smallviews.setImageViewBitmap(R.id.song_image, img);
        } else {
            views.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
            smallviews.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            buildMediaNotification();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // Sets an ID for the notification, so it can be updated.
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setSound(null, null);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mChannel.enableVibration(false);
            mNotificationManager.createNotificationChannel(mChannel);

            status = new Notification.Builder(this, CHANNEL_ID).setOnlyAlertOnce(true)
                    .setVisibility(Notification.VISIBILITY_PUBLIC).setContentIntent(pOpenIntent).build();
            status.contentView = smallviews;
            status.bigContentView = views;
            status.priority = Notification.PRIORITY_MAX;
            status.when = 0;
            status.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
            status.icon = R.mipmap.ic_launcher;
            status.contentIntent = pendingIntent;

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            status = new Notification.Builder(this).setWhen(0).setContentIntent(pOpenIntent).build();
            status.contentView = smallviews;
            status.bigContentView = views;
            status.visibility = Notification.VISIBILITY_PUBLIC;
            status.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
            status.icon = R.mipmap.ic_launcher;
            status.contentIntent = pendingIntent;
            status.priority = Notification.PRIORITY_MAX;

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);

        } else {
            status = new Notification.Builder(this).setWhen(0).setContentIntent(pOpenIntent).build();
            status.contentView = smallviews;
            status.bigContentView = views;

            status.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
            status.icon = R.mipmap.ic_launcher;
            status.contentIntent = pendingIntent;
            status.priority = Notification.PRIORITY_MAX;

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void buildMediaNotification() {
        int pendingIntentFlag = Build.VERSION.SDK_INT<Build.VERSION_CODES.S ? 0 : PendingIntent.FLAG_IMMUTABLE;
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Sets an ID for the notification, so it can be updated.
        int importance = NotificationManager.IMPORTANCE_MIN;


        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

        mChannel.setSound(null, null);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mChannel.enableVibration(false);

        mNotificationManager.createNotificationChannel(mChannel);

        MediaSession mediaSession = new MediaSession(getBaseContext(), "EchoNotification");
        mediaSession.setActive(true);
        addMetaData(mediaSession);


        // Create a MediaStyle object and supply your media session token to it.
        Notification.MediaStyle mediaStyle = new Notification.MediaStyle().setMediaSession(mediaSession.getSessionToken());
        mediaStyle.setShowActionsInCompactView(1, 2, 3);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, pendingIntentFlag);

        Intent closeIntent = new Intent(this, EchoNotification.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, pendingIntentFlag);

        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                .setStyle(mediaStyle)
                .setSmallIcon(R.drawable.ic_echo_icon)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(pcloseIntent);

        addActions(builder);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            addInfo(builder);
        }

        status = builder.build();
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addInfo(Notification.Builder builder) {
        Bitmap icon = getAlbumart(getBaseContext(), albumID);
        builder.setContentTitle(title);
        builder.setContentText(artist);
        if (icon == null)
            builder.setLargeIcon(Icon.createWithContentUri(getUriToDrawable(getBaseContext(), R.drawable.echo_icon)));
        else
            builder.setLargeIcon(icon);
    }

    public static Bitmap getAlbumart(Context context, Long album_id) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
                pfd = null;
                fd = null;
            }
        } catch (Error ee) {
        } catch (Exception e) {
        }
        return bm;
    }

    public String getAlbumArtUri(Long album_id) {
        Bitmap img = getAlbumart(getBaseContext(), albumID);
        if (img == null)
            return getUriToDrawable(getBaseContext(), R.drawable.echo_icon).toString();
        final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(sArtworkUri, album_id).toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void addActions(Notification.Builder builder) {
        int pendingIntentFlag = Build.VERSION.SDK_INT<Build.VERSION_CODES.S ? 0 : PendingIntent.FLAG_IMMUTABLE;

        Intent previousIntent = new Intent(this, EchoNotification.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, pendingIntentFlag);

        Intent playIntent = new Intent(this, EchoNotification.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, pendingIntentFlag);

        Intent nextIntent = new Intent(this, EchoNotification.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, pendingIntentFlag);

        Intent closeIntent = new Intent(this, EchoNotification.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, pendingIntentFlag);

        Intent shuffleIntent = new Intent(this, EchoNotification.class);
        shuffleIntent.setAction(Constants.ACTION.SHUFFLE_ACTION);
        PendingIntent pShuffleIntent = PendingIntent.getService(this, 0,
                shuffleIntent, pendingIntentFlag);

        Notification.Action mShuffleAction =
                new Notification.Action(
                        R.drawable.baseline_shuffle_24,
                        "shuffle",
                        pShuffleIntent);

        Notification.Action mPrevAction =
                new Notification.Action(
                        R.drawable.play_previous_icon,
                        "prev",
                        ppreviousIntent);

        Notification.Action mPlayAction =
                new Notification.Action(
                        R.drawable.play_circle_white,
                        "play",
                        pplayIntent);
        Notification.Action mPauseAction =
                new Notification.Action(
                        R.drawable.pause_circle_white,
                        "pause",
                        pplayIntent);

        Notification.Action mNextAction =
                new Notification.Action(
                        R.drawable.play_next_icon,
                        "next",
                        pnextIntent);

        Notification.Action mCloseAction =
                new Notification.Action(
                        R.drawable.baseline_close_24,
                        "close",
                        pcloseIntent);

        builder.addAction(mShuffleAction);
        builder.addAction(mPrevAction);

        if (MediaUtils.INSTANCE.isMediaPlayerPlaying())
            builder.addAction(mPauseAction);
        else
            builder.addAction(mPlayAction);
        builder.addAction(mNextAction);
        builder.addAction(mCloseAction);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addMetaData(MediaSession mediaSession) {
        mediaSession.setMetadata(
                new MediaMetadata.Builder()
                        .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                        .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, getAlbumArtUri(albumID))
                        .putLong(MediaMetadata.METADATA_KEY_DURATION, MediaUtils.INSTANCE.getDuration())
                        .build()
        );

        mediaSession.setPlaybackState(
                new PlaybackState.Builder()
                        .setState(
                                MediaUtils.INSTANCE.isMediaPlayerPlaying() ?
                                        PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED,
                                (long) MediaUtils.INSTANCE.getCurrentPosition(),
                                1f
                        )
                        // isSeekable.
                        .setActions(PlaybackState.ACTION_SEEK_TO)
                        .build()
        );

        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                MediaUtils.INSTANCE.getMediaPlayer().seekTo((int) pos);
                buildMediaNotification();
            }
        });

    }

    private Uri getUriToDrawable(@NonNull Context context,
                                 @AnyRes int drawableId) {
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + context.getResources().getResourcePackageName(drawableId)
                + '/' + context.getResources().getResourceTypeName(drawableId)
                + '/' + context.getResources().getResourceEntryName(drawableId));
        return imageUri;
    }


    public void updateNotiUI() {
        getApplicationContext().getSharedPreferences(Constants.APP_PREFS, Context.MODE_PRIVATE).edit().putLong("albumId", albumID).apply();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S)
            buildMediaNotification();
        else
            this.startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, this.status);
    }
}