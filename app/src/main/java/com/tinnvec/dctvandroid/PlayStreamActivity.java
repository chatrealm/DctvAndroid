package com.tinnvec.dctvandroid;

import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;
import io.vov.vitamio.widget.MediaController;

public class PlayStreamActivity extends AppCompatActivity
        implements OnInfoListener, OnPreparedListener, OnErrorListener {
    private ProgressDialog progressDialog;
    private VideoView vidView;
    private String dctvBaseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vitamio.isInitialized(getApplicationContext());

        this.dctvBaseUrl = getString(R.string.dctv_base_url);

        setContentView(R.layout.activity_play_stream);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        DctvChannel channel = getIntent().getExtras().getParcelable(LiveChannelsActivity.CHANNEL_DATA);
        String title;
        if (channel != null) {
            title = channel.friendlyalias;

            Bitmap resizedBitmap = channel.getImageBitmap(this);
            Drawable smallerArt = new BitmapDrawable(getResources(), resizedBitmap);
            toolbar.setLogo(smallerArt);
            toolbar.setLogoDescription(R.string.channel_art_description);
        } else {
            title = "Unknown";
        }

        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setTitle(title);
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(title);
        progressDialog.setMessage("Loading...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);

        vidView = (VideoView) findViewById(R.id.video_view);
        vidView.setOnInfoListener(this);
        vidView.setOnPreparedListener(this);
        vidView.setOnErrorListener(this);

        MediaController mediaController = new MediaController(vidView.getContext());
        mediaController.setAnchorView(vidView);
        vidView.setMediaController(mediaController);

        WebView chatWebview = (WebView) findViewById(R.id.chat_webview);
        WebSettings settings = chatWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        chatWebview.loadUrl("http://irc.chatrealm.net");

        try {
            if (channel !=  null) {
                String urlString = String.format("%sapi/hlsredirect.php?c=%d", dctvBaseUrl, channel.channel);
                Uri vidUri = Uri.parse(urlString);
                progressDialog.show();
                vidView.setVideoURI(vidUri);
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
            case android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mp.isPlaying()) {
                    mp.pause();
                }
                progressDialog.setMessage("Buffering...");
                progressDialog.show();
                break;
            case android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END:
                progressDialog.dismiss();
                mp.start();
                break;
        }
        return true;
//        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        progressDialog.dismiss();
        vidView.requestFocus();
        mp.start();
    }

}
