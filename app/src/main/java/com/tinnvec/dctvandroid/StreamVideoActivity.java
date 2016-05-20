package com.tinnvec.dctvandroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class StreamVideoActivity extends Activity
        implements MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, View.OnSystemUiVisibilityChangeListener, View.OnClickListener {
    private ProgressDialog progressDialog;
    private boolean needResume;
    private int mLastSystemUIVisibility;
    private final Handler mLeanBackHandler = new Handler();
    private final Runnable mEnterLeanback = new Runnable() {
        @Override
        public void run() {
            enableFullScreen(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this)) {
            Log.e("unable to load/initialize vitamio libraries");
            return;
        }
        enableFullScreen(true);
        setContentView(R.layout.activity_stream_video);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);

        DctvChannel channel = getIntent().getExtras().getParcelable(LiveChannelsActivity.CHANNEL_DATA);
        String title;
        if (channel != null) {
            title = channel.friendlyalias;
        } else {
            title = "Unknown";
        }

        VideoView vidView = (VideoView) findViewById(R.id.videoView);
        vidView.setOnInfoListener(this);
        vidView.setOnPreparedListener(this);
        vidView.setOnErrorListener(this);
        vidView.setOnClickListener(this);

        MediaController mediaController = new MediaController(vidView.getContext());
        mediaController.setAnchorView(vidView);
        vidView.setMediaController(mediaController);

/*        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(title);
        progressDialog.setMessage("Loading...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);*/
//        progressDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
//        progressDialog.show();
//        progressDialog.getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility());
//        progressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        try {
            if (channel !=  null) {
                String urlString = String.format("http://diamondclub.tv/api/hlsredirect.php?c=%d", channel.channel);
                vidView.setVideoPath(urlString);
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.stop();
        progressDialog.dismiss();
        startActivity(new Intent(getBaseContext(), LiveChannelsActivity.class));
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mp.isPlaying()) {
                    mp.stop();
                    needResume = true;
                }
                progressDialog.setMessage("Buffering...");
                progressDialog.show();
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (needResume)
                    mp.start();
                progressDialog.dismiss();
                break;
        }
        return true;
//        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        progressDialog.dismiss();
    }

    protected void enableFullScreen(boolean enabled) {
        int newVisibility =  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        if(enabled) {
            newVisibility |= View.SYSTEM_UI_FLAG_FULLSCREEN
                    |  View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Set the visibility
        getWindow().getDecorView().setSystemUiVisibility(newVisibility);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        if((mLastSystemUIVisibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0
                && (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
            resetHideTimer();
        }
        mLastSystemUIVisibility = visibility;
    }

    @Override
    public void onClick(View v) {
        // If the `mainView` receives a click event then reset the leanback-mode clock
        resetHideTimer();
    }

    private void resetHideTimer() {
        // First cancel any queued events - i.e. resetting the countdown clock
        mLeanBackHandler.removeCallbacks(mEnterLeanback);
        // And fire the event in 3s time
        mLeanBackHandler.postDelayed(mEnterLeanback, 3000);
    }
}
