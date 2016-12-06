package com.tinnvec.dctvandroid;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.squareup.picasso.Picasso;
import com.tinnvec.dctvandroid.channel.RadioChannel;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class RadioPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    public static final String
            BROADCAST_PLAYBACK_STOP = "stop";
    private static final String ACTION_PLAY = "android.media.intent.action.PLAY";
    final int NOTIFICATION_ID = 420;
    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BROADCAST_PLAYBACK_STOP)) {
                stopSelf();
            }
        }
    };
    MediaPlayer mMediaPlayer = null;
    private RadioChannel channel;
    private WifiManager.WifiLock wifiLock;
    private boolean mRunning = false;
    private String dataSource = "";
    private android.support.v4.app.NotificationCompat.Builder notificationBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        mRunning = false;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_PLAYBACK_STOP);
        registerReceiver(broadcastReceiver, intentFilter);
        showNotification();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {
            channel = intent.getExtras().getParcelable(LiveChannelsActivity.CHANNEL_DATA);
            if (channel == null)
                throw new NullPointerException("No Channel passed to RadioPlayerService");
            String url = channel.getStreamUrl();
            if (mRunning && !url.equals(dataSource)) {
                mMediaPlayer.reset();
                mRunning = false;
            }
            if (!mRunning) {
                mRunning = true;
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mMediaPlayer.setDataSource(url);
                    dataSource = url;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                        .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
                wifiLock.acquire();

                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.prepareAsync(); // prepare async to not block main thread
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        mMediaPlayer.release();
        wifiLock.release();
        stopForeground(true);
        unregisterReceiver(broadcastReceiver);
        mRunning = false;
        stopSelf();
        return true;
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            wifiLock.release();
            stopForeground(true);
            unregisterReceiver(broadcastReceiver);
            mRunning = false;
        }
    }

    private PendingIntent makePendingIntent(String broadcast) {
        Intent intent = new Intent(broadcast);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
    }

    private void showNotification() {
        notificationBuilder = new NotificationCompat.Builder(this)
                .setStyle(new NotificationCompat.MediaStyle()
                        .setCancelButtonIntent(makePendingIntent(BROADCAST_PLAYBACK_STOP))
                        .setShowCancelButton(true)
                        .setShowActionsInCompactView(0))
                .setSmallIcon(R.drawable.ic_dctvlogo)
                .setContentTitle("Diamond Club FM")
                .setContentText("Loading...")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 1, new Intent(getApplicationContext(), RadioChannelsActivity.class), 0))
                .setDeleteIntent(makePendingIntent(BROADCAST_PLAYBACK_STOP))
                .addAction(R.drawable.big_stop_button, "Stop", makePendingIntent(BROADCAST_PLAYBACK_STOP));
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    /**
     * Called when MediaPlayer is ready
     */
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        notificationBuilder.setContentText(getRadioTitle()).setLargeIcon(getNotifIcon());
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    public String getRadioTitle() {
        String title = channel.getFriendlyAlias();
        return title;
    }

    public String getRadioDescription() {
        String desc = channel.getDescription();
        return desc;
    }

    public String getRadioImgUrl() {
        String url = channel.getImageAssetUrl();

        return url;
    }

    private Bitmap getNotifIcon() {
        Bitmap bitmap = null;
        try {
            bitmap = new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    try {
                        return Picasso.with(getApplicationContext()).load(getRadioImgUrl())
                                .resize(200, 200)
                                .placeholder(R.drawable.ic_dctvlogo)
                                .error(R.drawable.ic_dctvlogo)
                                .get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_dctvlogo);
        }
        return bitmap;
    }

    public boolean isRunning() {
        return mRunning;
    }

    public class LocalBinder extends Binder {
        RadioPlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return RadioPlayerService.this;
        }
    }

}
