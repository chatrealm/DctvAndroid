package com.tinnvec.dctvandroid;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.transition.Slide;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.EMVideoView;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.images.WebImage;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
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
import static com.tinnvec.dctvandroid.PlayStreamActivity.PlaybackState.BUFFERING;
import static com.tinnvec.dctvandroid.PlayStreamActivity.PlaybackState.PLAYING;

public class PlayStreamActivity extends AppCompatActivity {
    private static final String TAG = PlayStreamActivity.class.getName();
    private final Handler mHandler = new Handler();
    private final Handler mHideHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hideSystemUI();
        }
    };
    private ProgressDialog progressDialog;
    private EMVideoView vidView;
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
    private PlaybackState mPlaybackState;
    private ImageButton mPlayPause;
    private ImageButton mFullscreenSwitch;
    private ImageButton mChatrealmRevealer;
    private LandscapeChatState mLandscapeChatState;
    private ProgressBar mLoading;
    private View mControllers;
    private boolean mControllersVisible;
    private Timer mControllersTimer;
    private Properties appConfig;
    private Quality currentQuality;
    private RelativeLayout chatContainer;
    private String streamService;
    private ChatFragment chatFragment;
    private boolean artShown;
    private int actionBarColorIfShown;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private Target mTarget;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        PropertyReader pReader = new PropertyReader(this);
        appConfig = pReader.getMyProperties("app.properties");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.activity_play_stream);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        channel = getIntent().getExtras().getParcelable(LiveChannelsActivity.CHANNEL_DATA);
        if (channel == null)
            throw new NullPointerException("No Channel passed to PlayStreamActivity");

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

        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

        if (isMobile) {
            currentQuality = Quality.valueOf(sharedPreferences.getString("stream_quality_mobile", "low").toUpperCase());
        } else {
            currentQuality = Quality.valueOf(sharedPreferences.getString("stream_quality", "high").toUpperCase());
        }
        this.streamUrl = channel.getStreamUrl(appConfig, currentQuality);

        // for cast SDK v3
//        setupControlsCallbacks();
        setupCastListener();
        mCastContext = CastContext.getSharedInstance(this);
        mCastContext.registerLifecycleCallbacksBeforeIceCreamSandwich(this, savedInstanceState);
        mCastSession = mCastContext.getSessionManager().getCurrentCastSession();

        String title = channel.getFriendlyAlias();
        title = title != null ? title : "Unknown";

        final ImageView channelArtView = (ImageView) findViewById(R.id.channelart);
        final ImageView artFillImg = (ImageView) findViewById(R.id.art_fill);
        String urlChannelart = channel.getImageAssetHDUrl();
        vidView = (EMVideoView) findViewById(R.id.video_view);
//        vidView.setOnInfoListener(this);
        //       vidView.setOnPreparedListener(this);
//        vidView.setOnErrorListener(this);
        mPlayPause = (ImageButton) findViewById(R.id.play_pause_button);
        mLoading = (ProgressBar) findViewById(R.id.buffer_circle);
        mControllers = findViewById(R.id.mediacontroller_anchor);
        mFullscreenSwitch = (ImageButton) findViewById(R.id.fullscreen_switch_button);
        mChatrealmRevealer = (ImageButton) findViewById(R.id.reveal_chat_button);

        chatFragment = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString("streamService", streamService);
        bundle.putString("channelName", channel.getName());
        chatFragment.setArguments(bundle);

        chatContainer = (RelativeLayout) findViewById(R.id.chat_fragment);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.black));
            window.setNavigationBarColor(ContextCompat.getColor(this, android.R.color.black));
            window.setBackgroundDrawable(new ColorDrawable(getColor(android.R.color.black)));
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.BOTTOM);
            window.setEnterTransition(slide);
            window.setExitTransition(new Slide());
        }

        getFragmentManager()
                .beginTransaction()
                .add(chatContainer.getId(), chatFragment)
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .commit();

        if (urlChannelart != null) {
            loadArt(this, urlChannelart);
        } else {
            Drawable defaultArt = ResourcesCompat.getDrawable(getResources(), R.drawable.dctv_bg, null);
            channelArtView.setImageDrawable(defaultArt);
            artFillImg.setImageDrawable(defaultArt);
        }

        if (actionBarColorIfShown == 0) {
            actionBarColorIfShown = ContextCompat.getColor(this, R.color.primary);
        }

        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setTitle(title);
        }

        actionbar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        actionbar.setElevation(0);

        setupControlsCallbacks();

        setVideoUrl(streamUrl);

        if (mLocation == PlaybackLocation.LOCAL) {
            if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
                setLandscapeMode();
                updateFullscreenButton(false);
            } else if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
                setPortraitMode();
                updateFullscreenButton(true);
            }
        }
    }

    void loadArt(Context context, String url) {
        final ImageView channelArtView = (ImageView) findViewById(R.id.channelart);
        final ImageView artFillImg = (ImageView) findViewById(R.id.art_fill);

        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                //Do something
                channelArtView.setImageBitmap(bitmap);
                artFillImg.setImageBitmap(bitmap);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    Palette.PaletteAsyncListener paletteListener = new Palette.PaletteAsyncListener() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        public void onGenerated(Palette palette) {
                            int standard = ContextCompat.getColor(PlayStreamActivity.this, R.color.primary_dark);
                            int vibrantDark = palette.getDarkVibrantColor(standard);

                            channelArtView.setBackground(new ColorDrawable(vibrantDark));
                            artFillImg.setBackground(new ColorDrawable(vibrantDark));
                            actionBarColorIfShown = vibrantDark;
                        }


                    };

                    if (bitmap != null && !bitmap.isRecycled()) {
                        Palette.from(bitmap).generate(paletteListener);
                    }
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        Picasso.with(context)
                .load(url)
                .into(mTarget);
    }

    private void setVideoUrl(String streamUrl) {
        String resolvedStreamUrl = "";
        try {
            resolvedStreamUrl = channel.getResolvedStreamUrl(streamUrl);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "Exception when trying to get full Stream URL", e);
        }

        if (resolvedStreamUrl == null) {
            String msg = "";
            if (channel instanceof YoutubeChannel) {
                msg = getString(R.string.video_error_youtube);
                String errortext = "Error: " + msg;
                Snackbar.make(findViewById(R.id.root_coordinator), errortext, Snackbar.LENGTH_LONG)
                        .show();
            } else {
                msg = getString(R.string.video_error_fail);
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage(msg)
                        .setPositiveButton("Change Quality", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String qualities[] = Quality.allAsStrings(channel.getAllowedQualities());

                                AlertDialog.Builder builder = new AlertDialog.Builder(PlayStreamActivity.this);
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
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                startActivity(new Intent(PlayStreamActivity.this, LiveChannelsActivity.class));
                            }
                        })
                        .show();
            }
            vidView.stopPlayback();
            mPlaybackState = PlaybackState.IDLE;
            updatePlayButton(mPlaybackState);
            return;
        }
        vidView.setVideoPath(resolvedStreamUrl);
        Log.d(TAG, "Setting url of the VideoView to: " + resolvedStreamUrl);
        mPlaybackState = BUFFERING;
        updatePlayButton(mPlaybackState);
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
            updatePlaybackLocation(PlaybackLocation.LOCAL);
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

                if (mPlaybackState == PLAYING) {
                    loadRemoteMedia(true);
                    vidView.stopPlayback();
                    updatePlaybackLocation(PlaybackLocation.REMOTE);
                    mPlaybackState = PlaybackState.IDLE;
//                        mediaPlayer.stop();
//                        finish();
                    return;
                } else {
                    mPlaybackState = PlaybackState.IDLE;
                    updatePlaybackLocation(PlaybackLocation.REMOTE);
                }

                updatePlayButton(mPlaybackState);
                invalidateOptionsMenu();
            }

            private void onApplicationDisconnected() {
                updatePlaybackLocation(PlaybackLocation.LOCAL);
                mPlaybackState = PlaybackState.IDLE;
                mLocation = PlaybackLocation.LOCAL;
                updatePlayButton(mPlaybackState);
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
        try {
            remoteMediaClient.load(buildMediaInfo(), autoPlay);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Exception when trying to load stream to chromecast", e);
            Log.e(TAG, "Resolved Stream URL returns null", e);
            String msg = "";
            if (channel instanceof YoutubeChannel) {
                msg = getString(R.string.video_error_youtube);
                String errortext = "Error: " + msg;
                Snackbar.make(findViewById(R.id.root_coordinator), errortext, Snackbar.LENGTH_LONG)
                        .show();
            } else {
                msg = getString(R.string.video_error_fail);
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage(msg)
                        .setPositiveButton("Change Quality", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String qualities[] = Quality.allAsStrings(channel.getAllowedQualities());

                                AlertDialog.Builder builder = new AlertDialog.Builder(PlayStreamActivity.this);
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
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                startActivity(new Intent(PlayStreamActivity.this, LiveChannelsActivity.class));
                            }
                        })
                        .show();
            }
        }
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
        } catch (InterruptedException | ExecutionException | NullPointerException ex) {
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

            if (mPlaybackState == PLAYING
                    || mPlaybackState == PlaybackState.BUFFERING) {
                startControllersTimer();
            } else {
                stopControllersTimer();
            }
        } else {
            if (mLandscapeChatState == LandscapeChatState.SHOWING) {
                hideChat();
            }

            hideVideoView();
            setPortraitMode();

            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColorIfShown));
            getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.actionbar_elevation));
            getSupportActionBar().show();

            findViewById(R.id.actionbarspacer).setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            p.addRule(RelativeLayout.BELOW, R.id.actionbarspacer);

            findViewById(R.id.chat_fragment).setLayoutParams(p);


            stopControllersTimer();
//            setCoverArtStatus(mSelectedMedia.getImage(0));
            updateControllersVisibility(false);
        }
    }

    private void hideVideoView() {
        if (findViewById(R.id.view_group_video).getVisibility() == View.VISIBLE) {
            findViewById(R.id.view_group_video).setVisibility(View.GONE);
            mControllers.setVisibility(View.GONE);
            mLoading.setVisibility(View.GONE);
        }
    }

    private void showVideoView() {
        if (findViewById(R.id.view_group_video).getVisibility() != View.VISIBLE) {
            findViewById(R.id.view_group_video).setVisibility(View.VISIBLE);
            mControllers.setVisibility(View.VISIBLE);
            mLoading.setVisibility(View.VISIBLE);
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
                return true;
            case R.id.navigate_back:
                WebView chatWebview = (WebView) findViewById(R.id.chat_webview);
                chatWebview.goBack();
                return true;
            case R.id.switch_to_custom_twitch_chat:
                final EditText twitchUser = new EditText(this);
                twitchUser.setHint(channel.getName());
                twitchUser.setText(channel.getName());
                twitchUser.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.custom_twitch_chat_dialog_title)
                        .setMessage(R.string.custom_twitch_chat_dialog_msg)
                        .setView(twitchUser)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                chatFragment.setChatroom("twitch", twitchUser.getText().toString());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
                return true;
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void setupControlsCallbacks() {
        vidView.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError() {
                Log.e(TAG, "OnErrorListener.onError(): EMVideoView encountered an " +
                        "error");
                String msg = "";
                if (channel instanceof YoutubeChannel) {
                    msg = getString(R.string.video_error_youtube);
                    String errortext = "Error: " + msg;
                    Snackbar.make(findViewById(R.id.root_coordinator), errortext, Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    msg = getString(R.string.video_error_fail);
                    new AlertDialog.Builder(PlayStreamActivity.this)
                            .setTitle("Error")
                            .setMessage(msg)
                            .setPositiveButton("Change Quality", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String qualities[] = Quality.allAsStrings(channel.getAllowedQualities());

                                    AlertDialog.Builder builder = new AlertDialog.Builder(PlayStreamActivity.this);
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
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    startActivity(new Intent(PlayStreamActivity.this, LiveChannelsActivity.class));
                                }
                            })
                            .show();
                }
                vidView.stopPlayback();
                mPlaybackState = PlaybackState.IDLE;
                updatePlayButton(mPlaybackState);
                return true;
            }
        });

        vidView.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared() {
                if (mLocation == PlaybackLocation.LOCAL) {
                    vidView.requestFocus();
                    vidView.start();
                    mPlaybackState = PLAYING;
                    updatePlayButton(mPlaybackState);
                    mLoading.setVisibility(View.GONE);
                    ImageView channelart = (ImageView) findViewById(R.id.channelart);
                    channelart.setVisibility(View.GONE);
                }
                if (mLocation == PlaybackLocation.REMOTE) {
                    vidView.stopPlayback();
                    setPortraitMode();
                    updatePlayButton(mPlaybackState);
                    if (mCastSession != null && mCastSession.isConnected()) loadRemoteMedia(true);
                }
            }
        });

        // support for these kind of play state listeners are not yet supported in ExoMedia. keeping this for reference when they do

     /*   vidView.setOnBufferUpdateListener(new InfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (mLocation == PlaybackLocation.LOCAL) {
                    switch (what) {
                        case android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            if (mp.isPlaying()) {
                                mp.pause();
                            }
                            mPlaybackState = PlaybackState.BUFFERING;
                            updatePlayButton(mPlaybackState);
                            updateControllersVisibility(true);
                            if (!artShown) {
                                ImageView channelart = (ImageView) findViewById(R.id.channelart);
                                channelart.setVisibility(View.VISIBLE);
                            }
                            break;
                        case android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            mLocation = PlaybackLocation.LOCAL;
                            mp.start();
                            mPlaybackState = PLAYING;
                            updatePlayButton(mPlaybackState);
                            ImageView channelart = (ImageView) findViewById(R.id.channelart);
                            channelart.setVisibility(View.GONE);
                            artShown = true;
                            startControllersTimer();
                            break;
                    }
                }

                return true;
                //           return false;
            }
        });
*/
        vidView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mControllersVisible) {
                    updateControllersVisibility(true);
                }
                if (mPlaybackState == PLAYING) {
                    startControllersTimer();
                }
                return false;
            }
        });


        mPlayPause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                togglePlayback();
            }
        });

        mFullscreenSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                } else if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                }
            }
        });
        mChatrealmRevealer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLandscapeChatState == LandscapeChatState.HIDDEN) {
                    revealChat();
                } else if (mLandscapeChatState == LandscapeChatState.SHOWING) {
                    hideChat();
                }
            }
        });
    }

    private void updatePlayButton(PlaybackState state) {
        switch (state) {
            case PLAYING:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(
                        ResourcesCompat.getDrawable(getResources(), R.drawable.big_pause_button, null));
                break;
            case PAUSED:
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(
                        ResourcesCompat.getDrawable(getResources(), R.drawable.big_play_button, null));
                break;
            case IDLE:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.INVISIBLE);
                mPlayPause.setImageDrawable(
                        ResourcesCompat.getDrawable(getResources(), R.drawable.big_play_button, null));
                break;
            case BUFFERING:
                mPlayPause.setVisibility(View.INVISIBLE);
                mLoading.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void updateFullscreenButton(boolean isPortrait) {
        if (isPortrait) {
            mFullscreenSwitch.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.fullscreen_button, null));
        } else {
            mFullscreenSwitch.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.fullscreen_exit_button, null));
        }
    }

    private void videoQualityChanged() {
        this.streamUrl = channel.getStreamUrl(appConfig, currentQuality);
        updateQualityButton(currentQuality);
        setVideoUrl(streamUrl);
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

    private void togglePlayback() {
        stopControllersTimer();
        switch (mPlaybackState) {
            case PAUSED:
                switch (mLocation) {
                    case LOCAL:
                        vidView.start();
                        mPlaybackState = PLAYING;
                        startControllersTimer();
                        break;
                    case REMOTE:
                        break;
                    default:
                        break;
                }
                break;

            case PLAYING:
                mPlaybackState = PlaybackState.PAUSED;
                vidView.pause();
                break;

            case IDLE:
                vidView.setVideoPath(streamUrl);
                vidView.start();
                mPlaybackState = PLAYING;
                break;

            default:
                break;
        }
        updatePlayButton(mPlaybackState);
    }

    private void stopControllersTimer() {
        if (null != mControllersTimer) {
            mControllersTimer.cancel();
        }
    }

    private void startControllersTimer() {
        if (null != mControllersTimer) {
            mControllersTimer.cancel();
        }
        if (mLocation == PlaybackLocation.REMOTE) {
            return;
        }
        mControllersTimer = new Timer();
        mControllersTimer.schedule(new HideControllersTask(), 3000);
    }

    // should be called from the main thread
    private void updateControllersVisibility(boolean show) {
        if (show) {
            getSupportActionBar().show();
            mControllers.setVisibility(View.VISIBLE);
        } else if (mLocation == PlaybackLocation.LOCAL) {
            getSupportActionBar().hide();
            mControllers.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() was called");
        if (mLocation == PlaybackLocation.LOCAL) {
            if (mControllersTimer != null) {
                mControllersTimer.cancel();
            }
            // since we are playing locally, we need to stop the playback of
            // video (if user is not watching, pause it!)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (!isInMultiWindowMode()) {
                    vidView.pause();
                    mPlaybackState = PlaybackState.PAUSED;
                    //           updatePlayButton(PlaybackState.PAUSED);
                }
            } else {
                vidView.pause();
                mPlaybackState = PlaybackState.PAUSED;
            }
            updatePlayButton(mPlaybackState);
        }
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
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onPause() was called");
        if (mLocation == PlaybackLocation.LOCAL) {
            if (mControllersTimer != null) {
                mControllersTimer.cancel();
            }
            vidView.release();
            mPlaybackState = PlaybackState.IDLE;
            updatePlayButton(mPlaybackState);
        }
        mCastContext.getSessionManager().removeSessionManagerListener(
                mSessionManagerListener, CastSession.class);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == ORIENTATION_LANDSCAPE && mLocation == PlaybackLocation.LOCAL) {
            setLandscapeMode();
            updateFullscreenButton(false);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && mLocation == PlaybackLocation.LOCAL) {
            setPortraitMode();
            updateFullscreenButton(true);
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
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void setLandscapeMode() {
        delayedHide(600);

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                hideSystemUI(); // Needed to avoid exiting immersive_sticky when keyboard is displayed
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode()) {
            findViewById(R.id.root_coordinator).setFitsSystemWindows(true);
            findViewById(R.id.view_group_video).setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            vidView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                findViewById(R.id.root_coordinator).setFitsSystemWindows(false);
            }

            DisplayMetrics displaymetrics = new DisplayMetrics();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);
            } else {
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            }

            int w = displaymetrics.widthPixels;
            int h = displaymetrics.heightPixels;

            findViewById(R.id.view_group_video).setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            vidView.setLayoutParams(new FrameLayout.LayoutParams(w, h));
            mChatrealmRevealer.setVisibility(View.VISIBLE);
        }
        chatContainer.setVisibility(View.INVISIBLE);
        mLandscapeChatState = LandscapeChatState.HIDDEN;
    }

    public void setPortraitMode() {
        if (mLandscapeChatState == LandscapeChatState.SHOWING) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) chatContainer.getLayoutParams();
            params.addRule(RelativeLayout.RIGHT_OF, 0);
            params.addRule(RelativeLayout.BELOW, R.id.view_group_video);
            chatContainer.setLayoutParams(params);

            RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) findViewById(R.id.toolbar_layout).getLayoutParams();
            params2.addRule(RelativeLayout.ALIGN_RIGHT, 0);
            findViewById(R.id.toolbar_layout).setLayoutParams(params2);

            RelativeLayout artFillView = (RelativeLayout) findViewById(R.id.art_fill_container);
            artFillView.setVisibility(View.GONE);

            mLandscapeChatState = LandscapeChatState.HIDDEN;
            mChatrealmRevealer.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_chatrealm_reveal, null));
        }
        chatContainer.setVisibility(View.VISIBLE);

        mChatrealmRevealer.setVisibility(View.GONE);

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(null);
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(uiOptions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            findViewById(R.id.root_coordinator).setFitsSystemWindows(true);
        }

        findViewById(R.id.view_group_video).setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // getting the videoview to be 16:9
        DisplayMetrics displaymetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);
        } else {
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        }
        float h = displaymetrics.heightPixels;
        float w = displaymetrics.widthPixels;
        float floatHeight = (float) (w * 0.5625);
        int intHeight = Math.round(floatHeight);
        int intWidth = (int) w;
        vidView.setLayoutParams(new FrameLayout.LayoutParams(intWidth, intHeight));
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
        int videoWidth = (int) Math.round(0.6 * w);
        int videoHeight = (int) Math.round(0.6 * h);

        ValueAnimator anim = ValueAnimator.ofFloat((float) 1, (float) 0.55);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float valScale = (float) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = vidView.getLayoutParams();
                layoutParams.width = Math.round(w * valScale);
                layoutParams.height = Math.round(h * valScale);
                vidView.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(500);
        anim.start();

        RelativeLayout artFillView = (RelativeLayout) findViewById(R.id.art_fill_container);
        artFillView.setVisibility(View.VISIBLE);

        mLandscapeChatState = LandscapeChatState.SHOWING;
        mChatrealmRevealer.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_chatrealm_hide, null));
//        vidView.setLayoutParams(new FrameLayout.LayoutParams(videoWidth, videoHeight)); //without animation
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

        ValueAnimator anim = ValueAnimator.ofFloat((float) 0.55, (float) 1);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float valScale = (float) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = vidView.getLayoutParams();
                layoutParams.width = Math.round(w * valScale);
                layoutParams.height = Math.round(h * valScale);
                vidView.setLayoutParams(layoutParams);
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
        mChatrealmRevealer.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_chatrealm_reveal, null));
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeMessages(0);
        mHideHandler.sendEmptyMessageDelayed(0, delayMillis);
    }

    @Override
    public void onStart() {
        super.onStart();
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

    private class HideControllersTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateControllersVisibility(false);
                    mControllersVisible = false;
                }
            });

        }
    }
}
