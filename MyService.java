
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.text.format.DateUtils;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MyService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,

        AudioManager.OnAudioFocusChangeListener {
    MediaPlayer mp;
    File u;
    String filepath;

    private final IBinder mBinder = new MyBinder();
    private Messenger outMessenger;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";


    public class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent arg0) {

        Bundle extras = arg0.getExtras();
        // Get messager from the Activity
        if (extras != null) {
            outMessenger = (Messenger) extras.get("MESSENGER");
        }
        return mBinder;
    }
    @Override
    public void onCreate() {


    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return super.bindService(service, conn, flags);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();

        filepath = intent.getExtras().getString("file");
        u = new File(filepath);
        try {
            mp = new MediaPlayer();
            mp.setDataSource(filepath);
            mp.prepare();
            mp.start();
/*
            int audioSessionId = mp.getAudioSessionId();
            if (audioSessionId != -1)
                barVisualizer.setAudioSessionId(audioSessionId);
                */

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
        mp.stop();
    }
    @Override
    public boolean onUnbind(Intent intent) {
        String input = intent.getStringExtra("inputExtra");
        Intent notificationIntent = new Intent(this, MediaPlayerActivity.class);
        createNotificationChannel();
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        return super.onUnbind(intent);
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    @Override
    public void onAudioFocusChange(int i) {

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {


    }



    private void sendNotification() {

        RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.view_expanded_notification);
        expandedView.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
        expandedView.setTextViewText(R.id.notification_message, u.getName());
        // adding action to left button
        Intent leftIntent = new Intent(this, MediaPlayerActivity.class);
        leftIntent.setAction("Prev");
        expandedView.setOnClickPendingIntent(R.id.left_button, PendingIntent.getService(this, 0, leftIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        // adding action to right button
            Intent rightIntent = new Intent(this, MediaPlayerActivity.class);
        rightIntent.setAction("Next");
        expandedView.setOnClickPendingIntent(R.id.right_button, PendingIntent.getService(this, 1, rightIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        RemoteViews collapsedView = new RemoteViews(getPackageName(), R.layout.view_collapsed_notification);
        collapsedView.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                // these are the three things a NotificationCompat.Builder object requires at a minimum
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Musify")
                .setContentText(u.getName())
                // notification will be dismissed when tapped
                .setAutoCancel(true)
                // tapping notification will open MainActivity
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                // setting the custom collapsed and expanded views
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                // setting style to DecoratedCustomViewStyle() is necessary for custom views to display
            ;

        // retrieves android.app.NotificationManager
        NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}

