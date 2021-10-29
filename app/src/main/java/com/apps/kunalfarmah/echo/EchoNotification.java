package com.apps.kunalfarmah.echo;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.apps.kunalfarmah.echo.activity.MainActivity;
import com.apps.kunalfarmah.echo.fragment.AlbumTracksFragment;
import com.apps.kunalfarmah.echo.fragment.FavoriteFragment;
import com.apps.kunalfarmah.echo.fragment.MainScreenFragment;
import com.apps.kunalfarmah.echo.fragment.OfflineAlbumsFragment;
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment;
import com.apps.kunalfarmah.echo.util.Constants;
import com.apps.kunalfarmah.echo.viewModel.SongsViewModel;
import com.bumptech.glide.Glide;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Objects;
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
    static MediaPlayer mMediaPlayer;


    String title = "";
    String artist = "";
    Long albumID;
    SongPlayingFragment msong;
    RemoteViews views;
    RemoteViews smallviews;
    ImageView imageView;


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

            if(null == intent){
                stopForeground(true);
                stopSelf();
            }

            mMediaPlayer = msong.getMediaPlayer();

            if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {

                title = intent.getStringExtra("title");
                artist = intent.getStringExtra("artist");
                albumID = intent.getLongExtra("album",-1);
                main.setNotify_val(true);

                showNotification();

            } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {

                msong.previous();
                views.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);
                smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);

                Bitmap img = getAlbumart(getBaseContext(), albumID);
                if(img!=null){
                    views.setImageViewBitmap(R.id.song_image, img);
                    smallviews.setImageViewBitmap(R.id.song_image, img);
                }
                else{
                    views.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                    smallviews.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                }

                updateNotiUI();
                setAlbumArt();


            } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {

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
                setAlbumArt();

            } else if (intent.getAction().equals(Constants.ACTION.CHANGE_TO_PAUSE)) {
                songsViewModel.setPlayStatus(true);
                views.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);
                smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);
                updateNotiUI();
            } else if (intent.getAction().equals(Constants.ACTION.CHANGE_TO_PLAY)) {
                songsViewModel.setPlayStatus(false);
                views.setImageViewResource(R.id.playpausebutton_not, R.drawable.play_icon);
                smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.play_icon);

                updateNotiUI();
                setAlbumArt();

            } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
                msong.next();
                views.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);
                smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);

                Bitmap img = getAlbumart(getBaseContext(), albumID);
                if(img!=null){
                    views.setImageViewBitmap(R.id.song_image, img);
                    smallviews.setImageViewBitmap(R.id.song_image, img);
                }
                else{
                    views.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                    smallviews.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                }

                updateNotiUI();
                setAlbumArt();


            } else if (intent.getAction().equals(Constants.ACTION.NEXT_UPDATE)) {

                title = intent.getStringExtra("title");
                artist = intent.getStringExtra("artist");
                albumID = intent.getLongExtra("album",-1);


                if (title.equals("<unknown>"))
                    title = "Unknown";

                if (artist.equals("<unknown>"))
                    artist = "unknown";

                views.setTextViewText(R.id.song_title_nav, title);
                views.setTextViewText(R.id.song_artist_nav, artist);
                views.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);
                smallviews.setTextViewText(R.id.song_title_nav, title);
                smallviews.setTextViewText(R.id.song_artist_nav, artist);
                smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);

                Bitmap img = getAlbumart(getBaseContext(), albumID);
                if(img!=null){
                    views.setImageViewBitmap(R.id.song_image, img);
                    smallviews.setImageViewBitmap(R.id.song_image, img);
                }
                else{
                    views.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                    smallviews.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                }


                updateNotiUI();
                setAlbumArt();
            }


            else if (intent.getAction().equals(Constants.ACTION.PREV_UPDATE)) {
                title = intent.getStringExtra("title");
                artist = intent.getStringExtra("artist");
                albumID = intent.getLongExtra("album",-1);

                if (title.equals("<unknown>"))
                    title = "Unknown";

                if (artist.equals("<unknown>"))
                    artist = "unknown";
                views.setTextViewText(R.id.song_title_nav, title);
                views.setTextViewText(R.id.song_artist_nav, artist);
                views.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);

                smallviews.setTextViewText(R.id.song_title_nav, title);
                smallviews.setTextViewText(R.id.song_artist_nav, artist);
                smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);

                Bitmap img = getAlbumart(getBaseContext(), albumID);
                if(img!=null){
                    views.setImageViewBitmap(R.id.song_image, img);
                    smallviews.setImageViewBitmap(R.id.song_image, img);
                }
                else{
                    views.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                    smallviews.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
                }


                updateNotiUI();
                setAlbumArt();

            } else if (intent.getAction().equals(
                    Constants.ACTION.STOPFOREGROUND_ACTION)) {

                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                        .getInstance(this);
                localBroadcastManager.sendBroadcast(new Intent(
                        Constants.ACTION.CLOSE));

                msong.unregister();

                mMediaPlayer = msong.getMediaPlayer();
                mMediaPlayer.stop();

                main.setNotify_val(false);
                stopForeground(true);
                stopSelf();

            }

        }

    catch(Exception e) {
        main.finishAffinity();
    }
    finally {
            if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q)
                return START_STICKY;
            else{
                return super.onStartCommand(intent, flags, startId);
            }
    }

}

    Notification status;

    private void showNotification() {
// Using RemoteViews to bind custom layouts into Notification
        views = new RemoteViews(getPackageName(),
                R.layout.notification_bar);

        smallviews = new RemoteViews(getPackageName(),
                R.layout.notificaiton_smalll);



        Intent openIntent = new Intent(this, EchoNotification.class);
        PendingIntent pOpenIntent = PendingIntent.getActivity(this, 0, openIntent, 0);



        Intent notificationIntent = new Intent(this,MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, EchoNotification.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, EchoNotification.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, EchoNotification.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, EchoNotification.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        views.setOnClickPendingIntent(R.id.playpausebutton_not, pplayIntent);
        smallviews.setOnClickPendingIntent(R.id.playpausebutton_not, pplayIntent);


        views.setOnClickPendingIntent(R.id.nextbutton_not, pnextIntent);
        smallviews.setOnClickPendingIntent(R.id.nextbutton_not, pnextIntent);


        views.setOnClickPendingIntent(R.id.previousbutton_not, ppreviousIntent);
        smallviews.setOnClickPendingIntent(R.id.previousbutton_not, ppreviousIntent);


        views.setOnClickPendingIntent(R.id.close, pcloseIntent);
        smallviews.setOnClickPendingIntent(R.id.close,pcloseIntent);


        // setting thoughts of the day
        Random randomObject =new  Random() ;                                                            // initialising a random object of the random class
        int randomPosition = randomObject.nextInt(thoughts.size()+1);                // setting range of random to size+1
        int currentPosition = randomPosition;

        if (currentPosition == thoughts.size()) {    // if the currentposition exceeds the size, start over
            currentPosition = 0;
        }

        views.setTextViewText(R.id.logo, thoughts.get(currentPosition));



        if(title.equals("<unknown>"))
            title="Unknown";

        if(artist.equals("<unknown>"))
            artist="unknown";

        views.setTextViewText(R.id.song_title_nav, title);
        smallviews.setTextViewText(R.id.song_title_nav, title);


        views.setTextViewText(R.id.song_artist_nav, artist);
        smallviews.setTextViewText(R.id.song_artist_nav, artist);



        views.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);
        smallviews.setImageViewResource(R.id.playpausebutton_not, R.drawable.pause_icon);


        Bitmap img = getAlbumart(getBaseContext(), albumID);
        if(img!=null){
        views.setImageViewBitmap(R.id.song_image, img);
        smallviews.setImageViewBitmap(R.id.song_image, img);
        }
        else {
            views.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
            smallviews.setImageViewResource(R.id.song_image, R.drawable.now_playing_bar_eq_image);
        }

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            buildMediaNotification();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // Sets an ID for the notification, so it can be updated.
            int notifyID = 1;
            String CHANNEL_ID = "my_channel_011";// The id of the channel.
            CharSequence name = "Notify";
            int importance = NotificationManager.IMPORTANCE_HIGH;


            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name,  importance);

            mChannel.setSound(null, null);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mChannel.enableVibration(false);

            mNotificationManager.createNotificationChannel(mChannel);

            status = new Notification.Builder(this, CHANNEL_ID).setOnlyAlertOnce(true)
                    .setVisibility(Notification.VISIBILITY_PUBLIC).setContentIntent(pOpenIntent).build();
            status.contentView=smallviews;
            status.bigContentView = views;
            status.priority=Notification.PRIORITY_MAX;
            status.when =0;


            status.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
            status.icon = R.mipmap.ic_launcher;
            status.contentIntent = pendingIntent;


            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);

        }

        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.O ){

            status = new Notification.Builder(this).setWhen(0).setContentIntent(pOpenIntent).build();
            status.contentView=smallviews;
            status.bigContentView = views;
            status.visibility=Notification.VISIBILITY_PUBLIC;
            status.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
            status.icon = R.mipmap.ic_launcher;
            status.contentIntent = pendingIntent;
            status.priority = Notification.PRIORITY_MAX;

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);

        }


        else {
            status = new Notification.Builder(this).setWhen(0).setContentIntent(pOpenIntent).build();


            status.contentView=smallviews;
            status.bigContentView = views;

            status.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
            status.icon = R.mipmap.ic_launcher;
            status.contentIntent = pendingIntent;
            status.priority = Notification.PRIORITY_MAX;

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void buildMediaNotification(){
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Sets an ID for the notification, so it can be updated.
        int notifyID = 1;
        String CHANNEL_ID = "my_channel_011";// The id of the channel.
        CharSequence name = "Notify";
        int importance = NotificationManager.IMPORTANCE_NONE;


        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name,  importance);

        mChannel.setSound(null, null);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mChannel.enableVibration(false);

        mNotificationManager.createNotificationChannel(mChannel);

        MediaSession mediaSession = SongPlayingFragment.Statified.INSTANCE.getMediaSession();
        addMetaData(mediaSession);


        // Create a MediaStyle object and supply your media session token to it.
        Notification.MediaStyle mediaStyle = new Notification.MediaStyle().setMediaSession(mediaSession.getSessionToken());
        ArrayList<Notification.Action> actions = new ArrayList<>();
        mediaStyle.setShowActionsInCompactView(0,1,2);

        Intent notificationIntent = new Intent(this,MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent closeIntent = new Intent(this, EchoNotification.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        Notification.Builder builder =  new Notification.Builder(this, CHANNEL_ID)
                .setStyle(mediaStyle)
                .setSmallIcon(R.drawable.echo_icon)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(pcloseIntent);

        addActions(builder);

        status =builder.build();
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }

    public static Bitmap getAlbumart(Context context,Long album_id){
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try{
            final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd != null){
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
                pfd = null;
                fd = null;
            }
        } catch(Error ee){}
        catch (Exception e) {}
        return bm;
    }

    public String getAlbumArtUri(Long album_id){
        final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(sArtworkUri, album_id).toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void addActions(Notification.Builder builder){

        Intent previousIntent = new Intent(this, EchoNotification.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, EchoNotification.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, EchoNotification.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, EchoNotification.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);
        Notification.Action mPrevAction =
                new Notification.Action(
                        R.drawable.skip_previous_white,
                        "pause",
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
                        R.drawable.skip_next_white,
                        "pause",
                        pnextIntent);

        Notification.Action mCloseAction =
                new Notification.Action(
                        R.drawable.close_white,
                        "close",
                        pcloseIntent);

        builder.addAction(mPrevAction);

        if(SongPlayingFragment.Statified.INSTANCE.getMediaPlayer().isPlaying())
            builder.addAction(mPauseAction);
        else
            builder.addAction(mPlayAction);
        builder.addAction(mNextAction);
        builder.addAction(mCloseAction);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addMetaData(MediaSession mediaSession){
        mediaSession.setMetadata(
                new MediaMetadata.Builder()
                        .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                        .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, getAlbumArtUri(albumID))
                        .putLong(MediaMetadata.METADATA_KEY_DURATION, mMediaPlayer.getDuration())
                        .build()
        );

        mediaSession.setPlaybackState(
                new PlaybackState.Builder()
                        .setState(
                                PlaybackState.STATE_PLAYING,

                                // Playback position.
                                // Used to update the elapsed time and the progress bar. 
                                (long) mMediaPlayer.getCurrentPosition(),

                                // Playback speed. 
                                // Determines the rate at which the elapsed time changes. 
                                mMediaPlayer.getPlaybackParams().getSpeed()
                        )

                        // isSeekable. 
                        // Adding the SEEK_TO action indicates that seeking is supported 
                        // and makes the seekbar position marker draggable. If this is not 
                        // supplied seek will be disabled but progress will still be shown.
                        .setActions(PlaybackState.ACTION_SEEK_TO)
                        .build()
        );

        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                mMediaPlayer.seekTo((int)pos);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getPlaybackState() {
        if(mMediaPlayer.isPlaying()){
            return PlaybackState.STATE_PLAYING;
        }
        else
            return PlaybackState.STATE_PAUSED;
    }


    public void updateNotiUI() {
        getApplicationContext().getSharedPreferences("Notification",Context.MODE_PRIVATE).edit().putLong("albumId",albumID).apply();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
            buildMediaNotification();
        else
            this.startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, this.status);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    void setAlbumArt(){
        Long albumId = albumID;
        if(albumId<=0L) Objects.requireNonNull(MainScreenFragment.Statified.getSongImg()).setImageDrawable(getApplicationContext()
                .getResources().getDrawable(R.drawable.echo_icon));
        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, albumId);
        Glide.with(getApplicationContext()).load(uri).placeholder(R.drawable.echo_icon).into(Objects.requireNonNull(MainScreenFragment.Statified.getSongImg()));


        if(albumId<=0L) Objects.requireNonNull(FavoriteFragment.Statified.getSongImg()).setImageDrawable(getApplicationContext().getResources()
                .getDrawable(R.drawable.echo_icon));
         sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        uri = ContentUris.withAppendedId(sArtworkUri, albumId);
        Glide.with(getApplicationContext()).load(uri).placeholder(R.drawable.echo_icon).into(Objects.requireNonNull(FavoriteFragment.Statified.getSongImg()));


        if(albumId<=0L) Objects.requireNonNull(OfflineAlbumsFragment.Statified.getSongImg()).setImageDrawable(getApplicationContext()
                .getResources().getDrawable(R.drawable.echo_icon));
        sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        uri = ContentUris.withAppendedId(sArtworkUri, albumId);
        Glide.with(getApplicationContext()).load(uri).placeholder(R.drawable.echo_icon).into(Objects.requireNonNull(OfflineAlbumsFragment.Statified.getSongImg()));


        if(albumId<=0L) Objects.requireNonNull(AlbumTracksFragment.Statified.getSongImg()).setImageDrawable(getApplicationContext()
                .getResources().getDrawable(R.drawable.echo_icon));
        sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        uri = ContentUris.withAppendedId(sArtworkUri, albumId);
        Glide.with(getApplicationContext()).load(uri).placeholder(R.drawable.echo_icon).into(Objects.requireNonNull(AlbumTracksFragment.Statified.getSongImg()));
    }


}