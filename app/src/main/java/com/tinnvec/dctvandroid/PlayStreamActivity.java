package com.tinnvec.dctvandroid;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.squareup.picasso.Picasso;
import com.tinnvec.dctvandroid.channel.AbstractChannel;
import com.tinnvec.dctvandroid.channel.DctvChannel;
import com.tinnvec.dctvandroid.channel.Quality;
import com.tinnvec.dctvandroid.channel.TwitchChannel;
import com.tinnvec.dctvandroid.channel.YoutubeChannel;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;


import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static com.tinnvec.dctvandroid.PlayStreamActivity.PlaybackState.IDLE;
import static com.tinnvec.dctvandroid.PlayStreamActivity.PlaybackState.PLAYING;

public class PlayStreamActivity extends AppCompatActivity {
    private static final String TAG = PlayStreamActivity.class.getName();

    private final Handler mHideHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hideSystemUI();
        }
    };

    private final Handler mLocationHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            PlaybackLocation location = PlaybackLocation.valueOf(msg.getData().getString("location"));
            updatePlaybackLocation(location);
        }
    };


   // private ProgressDialog progressDialog;

    private String streamUrl;
    //converted to global for interaction with cast methods
    private AbstractChannel channel;
    // added for cast SDK v3
    private CastContext mCastContext;
    private MenuItem mediaRouteMenuItem;
    private Menu menu;
    private CastSession mCastSession;
    private SessionManagerListener<CastSession> mSessionManagerListener;
    private PlaybackLocation mLocation;


    private LandscapeChatState mLandscapeChatState;


    private Properties appConfig;
    private Quality currentQuality;
    private RelativeLayout chatContainer;
    private String streamService;
    private ChatFragment chatFragment;
    private VideoFragment mVideoFragment;
    private ChatVisibilityChangeListener mChatVisiblityChangeListener;


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        channel = getIntent().getExtras().getParcelable(LiveChannelsActivity.CHANNEL_DATA);
        if (channel == null)
            throw new NullPointerException("No Channel passed to PlayStreamActivity");

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        PropertyReader pReader = new PropertyReader(this);
        appConfig = pReader.getMyProperties("app.properties");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.activity_play_stream);

        currentQuality = Quality.valueOf(sharedPreferences.getString("stream_quality", "high").toUpperCase());
        this.streamUrl = channel.getStreamUrl(appConfig, currentQuality);

        // for cast SDK v3
//        setupControlsCallbacks();
        setupCastListener();
        mCastContext = CastContext.getSharedInstance(this);
        mCastContext.registerLifecycleCallbacksBeforeIceCreamSandwich(this, savedInstanceState);
        mCastSession = mCastContext.getSessionManager().getCurrentCastSession();


        chatFragment = new ChatFragment();

        streamService = "dctv";
        if (channel instanceof YoutubeChannel) {
            streamService = "youtube";
        }
        if (channel instanceof TwitchChannel) {
            streamService = "twitch";
        }
        if (channel instanceof DctvChannel) {
            streamService = "dctv";
        }

        Bundle chatBundle = new Bundle();
        chatBundle.putString("streamService", streamService);
        chatBundle.putString("channelName", channel.getName());

        chatFragment.setArguments(chatBundle);

        VitamioStreamFragment videoFragment = new VitamioStreamFragment();
        mVideoFragment = videoFragment;

        Bundle videoBundle = new Bundle();
        videoBundle.putParcelable("channel", channel);

        videoFragment.setArguments(videoBundle);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.chat_fragment, chatFragment)
                .add(R.id.video_fragment, videoFragment)
                .commit();

        // fragment holds the button, but we control the views
        // that need to be mutated
        videoFragment.setChatRevelerClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLandscapeChatState == PlayStreamActivity.LandscapeChatState.HIDDEN) {
                    revealChat();
                } else if (mLandscapeChatState == PlayStreamActivity.LandscapeChatState.SHOWING) {
                    hideChat();
                }
            }
        });

        mChatVisiblityChangeListener = videoFragment;

        chatContainer = (RelativeLayout) findViewById(R.id.chat_fragment);

        try {
            if (mCastSession != null && mCastSession.isConnected()) {
                updatePlaybackLocation(PlaybackLocation.REMOTE);
                RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
                if (remoteMediaClient.getMediaInfo() == null) {
                    loadRemoteMedia(true);
                } else if (remoteMediaClient.getMediaInfo() != null) {
                    if (!remoteMediaClient.getMediaInfo().getMetadata().getString(MediaMetadata.KEY_TITLE).equals(channel.getFriendlyAlias())) {
                        loadRemoteMedia(true);
                    }
                }
            } else {
                delayUpdatePlaybackLocation(PlaybackLocation.LOCAL, 600l);

            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            throw e;
        }

        if (mLocation == PlaybackLocation.LOCAL) {
            if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
                setLandscapeMode();
            } else if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
                setPortraitMode();
            }
        }
    }

    //sets up a listener for cast events
    private void setupCastListener() {
        mSessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionResumed(CastSession session, boolean wasSuspended) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarted(CastSession session, String sessionId) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionStartFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarting(CastSession session) {
            }

            @Override
            public void onSessionEnding(CastSession session) {
            }

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {
            }

            @Override
            public void onSessionSuspended(CastSession session, int reason) {
            }

            private void onApplicationConnected(CastSession castSession) {
                mCastSession = castSession;


                updatePlaybackLocation(PlaybackLocation.REMOTE);
                PlaybackState playbackState = mVideoFragment.getPlaybackState();
                if (playbackState == PLAYING) {
                    loadRemoteMedia(true);
                    mVideoFragment.setPlaybackState(PlaybackState.IDLE);
                    updatePlaybackLocation(PlaybackLocation.REMOTE);
                    return;
                } else {
                    updatePlaybackLocation(PlaybackLocation.REMOTE);
                }
                invalidateOptionsMenu();
            }

            private void onApplicationDisconnected() {
                updatePlaybackLocation(PlaybackLocation.LOCAL);

                mLocation = PlaybackLocation.LOCAL;
                mVideoFragment.setPlaybackState(PlaybackState.IDLE);
                invalidateOptionsMenu();
            }
        };
    }

    // building MediaInfo package to pass to chromecast and its logic on the phone

    // loads channel to cast device
    private void loadRemoteMedia(boolean autoPlay) {
        if (mCastSession == null) {
            return;
        }
        RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            return;
        }
        remoteMediaClient.load(buildMediaInfo(), autoPlay);
    }

    private MediaInfo buildMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, channel.getName());
        movieMetadata.putString(MediaMetadata.KEY_TITLE, channel.getFriendlyAlias());
        if (channel.getImageAssetHDUrl() != null)
            movieMetadata.addImage(new WebImage(Uri.parse(channel.getImageAssetHDUrl())));
        if (channel.getImageAssetUrl() != null)
            movieMetadata.addImage(new WebImage(Uri.parse(channel.getImageAssetUrl())));

        String resolvedStreamUrl = "";
        try {
            resolvedStreamUrl = channel.getResolvedStreamUrl(streamUrl);
        } catch (InterruptedException | ExecutionException ex) {
            Log.e(TAG, "Exception when trying to get full Stream URL", ex);
        }

        Log.d(TAG, "Passing this url to ChromeCast: " + resolvedStreamUrl);

        return new MediaInfo.Builder(resolvedStreamUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                .setContentType("videos/m3u8")
                .setMetadata(movieMetadata)
//                .setStreamDuration(mSelectedMedia.getDuration() * 1000) // not needed
                .build();
    }

    private void updatePlaybackLocation(PlaybackLocation location) {
        mLocation = location;
        if (location == PlaybackLocation.LOCAL) {
            showVideoView();
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
            findViewById(R.id.actionbarspacer).setVisibility(View.GONE);

            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            p.addRule(RelativeLayout.BELOW, R.id.view_group_video);

            chatContainer.setLayoutParams(p);

            mVideoFragment.setPlaybackState(PLAYING);

        } else {
            if (mLandscapeChatState == LandscapeChatState.SHOWING) {
                hideChat();
            }
            mVideoFragment.setPlaybackState(IDLE);
            hideVideoView();
            setPortraitMode();

            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#212121")));
            getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.actionbar_elevation));
            getSupportActionBar().show();

            findViewById(R.id.actionbarspacer).setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            p.addRule(RelativeLayout.BELOW, R.id.actionbarspacer);

            findViewById(R.id.chat_fragment).setLayoutParams(p);


//            setCoverArtStatus(mSelectedMedia.getImage(0));
            updateControllersVisibility(false);
        }
    }

    private void hideVideoView() {
        if (findViewById(R.id.video_fragment).getVisibility() == View.VISIBLE) {
            findViewById(R.id.video_fragment).setVisibility(View.GONE);
//            mControllers.setVisibility(View.GONE);
//            mLoading.setVisibility(View.GONE);
        }
    }

    private void showVideoView() {
        if (findViewById(R.id.video_fragment).getVisibility() != View.VISIBLE) {
            findViewById(R.id.video_fragment).setVisibility(View.VISIBLE);
//            mControllers.setVisibility(View.VISIBLE);
//            mLoading.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play_stream, menu);
        this.menu = menu;

        // add media router button for cast
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);

        updateQualityButton(currentQuality);

        if (chatFragment.shouldDisplayChatroomSwitcher()) {
            menu.findItem(R.id.switch_chat).setVisible(true);
            if (chatFragment.getDisplayedChatroomType().equals("alt")) {
                menu.findItem(R.id.switch_chat).setTitle("Switch to #chat");
                String msg = getString(R.string.alt_chat_msg);
                Snackbar.make(findViewById(R.id.root_coordinator), msg, Snackbar.LENGTH_LONG)
                        .show();
            }
            if (chatFragment.getDisplayedChatroomType().equals("main")) {
                menu.findItem(R.id.switch_chat).setTitle("Switch to alt. chat room");
            }
        } else {
            menu.findItem(R.id.switch_chat).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        String chatroomType = chatFragment.getDisplayedChatroomType();
        if (chatroomType.equals("alt")) {
            menu.findItem(R.id.switch_chat).setTitle("Switch to #chat");
        } else if (chatroomType.equals("main")) {
            menu.findItem(R.id.switch_chat).setTitle("Switch to channel chat room");
        }

        WebView chatWebview = (WebView) findViewById(R.id.chat_webview);
        if (chatWebview.canGoBack()) {
            menu.findItem(R.id.navigate_back).setVisible(true);
        } else {
            menu.findItem(R.id.navigate_back).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_quality:
                String qualities[] = Quality.allAsStrings(channel.getAllowedQualities());

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Pick a stream quality");
                builder.setItems(qualities, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Quality newQuality = channel.getAllowedQualities()[which];
                        if (newQuality != currentQuality) {
                            currentQuality = newQuality;
                            videoQualityChanged();
                        }
                    }
                });
                builder.show();

                return true;
            case R.id.switch_chat:
                String chatroomType = chatFragment.getDisplayedChatroomType();
                if (chatroomType.equals("alt")) {
                    chatFragment.setChatroom("dctv", "dummy");
                    menu.findItem(R.id.switch_chat).setTitle("Switch to alternative chat room");
                } else if (chatroomType.equals("main")) {
                    chatFragment.setChatroom(streamService, channel.getName());
                    menu.findItem(R.id.switch_chat).setTitle("Switch to #chat");
                }
            case R.id.navigate_back:
                WebView chatWebview = (WebView) findViewById(R.id.chat_webview);
                chatWebview.goBack();
        }


        return super.onOptionsItemSelected(item);
    }


    private void videoQualityChanged() {
        this.streamUrl = channel.getStreamUrl(appConfig, currentQuality);
        updateQualityButton(currentQuality);
        if (mLocation.equals(PlaybackLocation.REMOTE)) {
            if (mCastSession != null && mCastSession.isConnected()) {
                // reload chromecast
                loadRemoteMedia(true);
            }
        }
    }

    private void updateQualityButton(Quality currentQuality) {
        switch (currentQuality) {
            case HIGH:
                menu.findItem(R.id.action_quality).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.settings_hq_white, null));
                break;
            case LOW:
                menu.findItem(R.id.action_quality).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.settings_lq_white, null));
                break;
            case SOURCE:
                menu.findItem(R.id.action_quality).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.settings_src_white, null));
                break;
            default:
                menu.findItem(R.id.action_quality).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.settings_white, null));
                break;
        }

    }


    // should be called from the main thread
    private void updateControllersVisibility(boolean show) {
        if (show) {
            getSupportActionBar().show();
        } else if (mLocation == PlaybackLocation.LOCAL) {
            getSupportActionBar().hide();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() was called");
        mCastContext.getSessionManager().removeSessionManagerListener(
                mSessionManagerListener, CastSession.class);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
        mCastContext.getSessionManager().addSessionManagerListener(
                mSessionManagerListener, CastSession.class);
        if (mCastSession != null && mCastSession.isConnected()) {
            updatePlaybackLocation(PlaybackLocation.REMOTE);
        } else {
            updatePlaybackLocation(PlaybackLocation.LOCAL);
        }
        if (this.getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE && mLocation == PlaybackLocation.LOCAL) {
            setLandscapeMode();
        } else if (this.getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT && mLocation == PlaybackLocation.LOCAL) {
            setPortraitMode();
        }
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == ORIENTATION_LANDSCAPE && mLocation == PlaybackLocation.LOCAL) {
            setLandscapeMode();

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && mLocation == PlaybackLocation.LOCAL) {
            setPortraitMode();

        }
        if (newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_YES && getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE && mLocation == PlaybackLocation.LOCAL) {
            hideSystemUI();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE && mLocation == PlaybackLocation.LOCAL) {
                delayedHide(300);
            }
        } else {
            mHideHandler.removeMessages(0);
        }


    }

    public void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                //    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void setLandscapeMode() {
        delayedHide(600);
        mVideoFragment.setLandscapeMode();

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                hideSystemUI(); // Needed to avoid exiting immersive_sticky when keyboard is displayed
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            findViewById(R.id.root_coordinator).setFitsSystemWindows(false);
        }


        chatContainer.setVisibility(View.INVISIBLE);

        mLandscapeChatState = LandscapeChatState.HIDDEN;

    }

    public void setPortraitMode() {
        mVideoFragment.setPortraitMode();

        RelativeLayout artFillView = (RelativeLayout) findViewById(R.id.art_fill_container);
        artFillView.setVisibility(View.GONE);

        if (mLandscapeChatState == LandscapeChatState.SHOWING) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) chatContainer.getLayoutParams();
            params.addRule(RelativeLayout.RIGHT_OF, 0);
            params.addRule(RelativeLayout.BELOW, R.id.view_group_video);
            chatContainer.setLayoutParams(params);

            mLandscapeChatState = LandscapeChatState.HIDDEN;
        }
        chatContainer.setVisibility(View.VISIBLE);


        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(null);
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(uiOptions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            findViewById(R.id.root_coordinator).setFitsSystemWindows(true);
        }


    }

    public void revealChat() {
        chatContainer.setVisibility(View.VISIBLE);

        FrameLayout container = (FrameLayout) findViewById(R.id.view_group_video);
        container.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) chatContainer.getLayoutParams();
        params.addRule(RelativeLayout.BELOW, 0);
        params.addRule(RelativeLayout.RIGHT_OF, R.id.view_group_video);
        chatContainer.setLayoutParams(params);

        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) findViewById(R.id.toolbar_layout).getLayoutParams();
        params2.addRule(RelativeLayout.ALIGN_RIGHT, R.id.view_group_video);
        findViewById(R.id.toolbar_layout).setLayoutParams(params2);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);
        } else {
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        }
        final int w = displaymetrics.widthPixels;
        final int h = displaymetrics.heightPixels;

        final View videoView = ((Fragment)mVideoFragment).getView();

        ValueAnimator anim = ValueAnimator.ofFloat((float) 1, (float) 0.55);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float valScale = (float) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
                layoutParams.width = Math.round(w * valScale);
                layoutParams.height = Math.round(h * valScale);
                videoView.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(500);
        anim.start();

        RelativeLayout artFillView = (RelativeLayout) findViewById(R.id.art_fill_container);
        ImageView artFillImg = (ImageView) findViewById(R.id.art_fill);
        String urlChannelart = channel.getImageAssetHDUrl();

        if (urlChannelart != null) {
            Picasso.with(this)
                    .load(urlChannelart)
                    .into(artFillImg);
        } else {
            Drawable defaultArt = ResourcesCompat.getDrawable(getResources(), R.drawable.dctv_bg, null);
            artFillImg.setImageDrawable(defaultArt);
        }
        artFillView.setVisibility(View.VISIBLE);

        mLandscapeChatState = LandscapeChatState.SHOWING;
        mChatVisiblityChangeListener.onChatVisibiltiyChanged(mLandscapeChatState);

    }

    public void hideChat() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);
        } else {
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        }
        final int w = displaymetrics.widthPixels;
        final int h = displaymetrics.heightPixels;

        final View videoView = ((Fragment)mVideoFragment).getView();

        ValueAnimator anim = ValueAnimator.ofFloat((float) 0.55, (float) 1);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float valScale = (float) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
                layoutParams.width = Math.round(w * valScale);
                layoutParams.height = Math.round(h * valScale);
                videoView.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(500);
        anim.start();

        chatContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                RelativeLayout artFillView = (RelativeLayout) findViewById(R.id.art_fill_container);
                artFillView.setVisibility(View.GONE);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) chatContainer.getLayoutParams();
                params.addRule(RelativeLayout.RIGHT_OF, 0);
                params.addRule(RelativeLayout.BELOW, R.id.view_group_video);
                chatContainer.setLayoutParams(params);
                chatContainer.setVisibility(View.INVISIBLE);
            }
        }, 500);


        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) findViewById(R.id.toolbar_layout).getLayoutParams();
        params2.addRule(RelativeLayout.ALIGN_RIGHT, 0);
        findViewById(R.id.toolbar_layout).setLayoutParams(params2);

        mLandscapeChatState = LandscapeChatState.HIDDEN;
        mChatVisiblityChangeListener.onChatVisibiltiyChanged(mLandscapeChatState);

    }

    private void delayedHide(long delayMillis) {
        mHideHandler.removeMessages(0);
        mHideHandler.sendEmptyMessageDelayed(0, delayMillis);
    }

    private void delayUpdatePlaybackLocation(PlaybackLocation location, long delayMillis ) {
        mLocationHandler.removeMessages(0);
        Message msg = new Message();
        msg.getData().putString("location", location.toString());
        mLocationHandler.sendMessageDelayed(msg, delayMillis);
    }

    /**
     * indicates whether we are doing a local or a remote playback
     */
    public enum PlaybackLocation {
        LOCAL,
        REMOTE
    }

    /**
     * List of various states that we can be in
     */
    public enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }

    public enum LandscapeChatState {
        SHOWING, HIDDEN
    }


}
