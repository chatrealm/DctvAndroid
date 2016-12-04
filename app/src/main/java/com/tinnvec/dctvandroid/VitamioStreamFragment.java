package com.tinnvec.dctvandroid;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.tinnvec.dctvandroid.channel.AbstractChannel;
import com.tinnvec.dctvandroid.channel.Quality;
import com.tinnvec.dctvandroid.channel.YoutubeChannel;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static com.tinnvec.dctvandroid.PlayStreamActivity.PlaybackState.BUFFERING;
import static com.tinnvec.dctvandroid.PlayStreamActivity.PlaybackState.PAUSED;
import static com.tinnvec.dctvandroid.PlayStreamActivity.PlaybackState.PLAYING;


/**
 * Created by kev on 11/20/16.
 */

public class VitamioStreamFragment extends Fragment implements VideoFragment, ChatVisibilityChangeListener {
    private static final String TAG = VitamioStreamFragment.class.getName();
    private final Handler mHandler = new Handler();
    private View.OnClickListener mChatRevelerButtonListener;
    private String streamUrl;

    private final Handler mHideHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hideSysUi();
        }
    };
    private Properties appConfig;
    private AbstractChannel channel;
    private PlayStreamActivity.PlaybackState mPlaybackState;

    private VideoView vidView;
    private Quality currentQuality;
    private ImageButton mPlayPause;
    private RelativeLayout mLoading;
    private View mControllers;
    private ImageButton mFullscreenSwitch;
    private ImageButton mChatrealmRevealer;

    private boolean mControllersVisible;
    private Timer mControllersTimer;
    private boolean artShown;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Vitamio.isInitialized(getActivity().getApplicationContext());

        PropertyReader pReader = new PropertyReader(this.getActivity());
        appConfig = pReader.getMyProperties("app.properties");

        Bundle bundle = getArguments();
        channel = bundle.getParcelable("channel");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        currentQuality = Quality.valueOf(sharedPreferences.getString("stream_quality", "high").toUpperCase());
        this.streamUrl = channel.getStreamUrl(appConfig, currentQuality);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_vitamio_video, null);
        vidView = (VideoView) view.findViewById(R.id.video_view);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        String title = channel.getFriendlyAlias();
        title = title != null ? title : "Unknown";

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setTitle(title);
        }

        actionbar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        actionbar.setElevation(0);

        ImageView channelArtView = (ImageView) view.findViewById(R.id.channelart);
        String urlChannelart = channel.getImageAssetHDUrl();

        if (urlChannelart != null) {
            Picasso.with(this.getActivity())
                    .load(urlChannelart)
                    .into(channelArtView);
        } else {
            Drawable defaultArt = ResourcesCompat.getDrawable(getResources(), R.drawable.dctv_bg, null);
            channelArtView.setImageDrawable(defaultArt);
        }

        mPlayPause = (ImageButton) view.findViewById(R.id.play_pause_button);
        mLoading = (RelativeLayout) view.findViewById(R.id.buffer_circle);
        mControllers = view.findViewById(R.id.mediacontroller_anchor);
        mFullscreenSwitch = (ImageButton) view.findViewById(R.id.fullscreen_switch_button);
        mChatrealmRevealer = (ImageButton) view.findViewById(R.id.reveal_chat_button);


        setupControlsCallbacks();

        try {
            vidView.setVideoPath(streamUrl);
            Log.d(TAG, "Setting url of the VideoView to: " + streamUrl);
            mPlaybackState = PLAYING;
            updatePlayButton(mPlaybackState);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            throw e;
        }


            return view;
    }



    private void setupControlsCallbacks() {
        vidView.setOnErrorListener(new io.vov.vitamio.MediaPlayer.OnErrorListener() {

            @SuppressLint("NewApi")
            @Override
            public boolean onError(io.vov.vitamio.MediaPlayer mp, int what, int extra) {
                Log.e(TAG, "OnErrorListener.onError(): VideoView encountered an " +
                        "error, what: " + what + ", extra: " + extra);
                String msg = "";
                if (extra == android.media.MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    msg = getString(R.string.video_error_media_load_timeout);
                } else if (what == android.media.MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    msg = getString(R.string.video_error_server_unaccessible);
                } else if (channel instanceof YoutubeChannel) {
                    msg = getString(R.string.video_error_youtube);
                } else {
                    msg = getString(R.string.video_error_unknown_error);
                }
                String text = "Error: " + msg;
                Snackbar.make(getActivity().findViewById(R.id.root_coordinator), text, Snackbar.LENGTH_LONG)
                        .show();
                vidView.stopPlayback();
                mPlaybackState = PlayStreamActivity.PlaybackState.IDLE;
                updatePlayButton(mPlaybackState);
                return true;
            }
        });

        vidView.setOnPreparedListener(new io.vov.vitamio.MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {

                if (mPlaybackState == PLAYING) {
                    vidView.requestFocus();
                    mp.start();
                    mPlaybackState = PLAYING;
                    updatePlayButton(mPlaybackState);
                } else {
                    vidView.pause();
                    mp.stop();
                    updatePlayButton(mPlaybackState);
                }
                updatePlayButton(mPlaybackState);

            }
        });

        vidView.setOnInfoListener(new io.vov.vitamio.MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                 switch (what) {
                        case android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            if (mp.isPlaying()) {
                                mp.pause();
                            }
                            mPlaybackState = BUFFERING;
                            updatePlayButton(mPlaybackState);
                            updateControllersVisibility(true);
                            if (!artShown) {
                                ImageView channelart = (ImageView) getView().findViewById(R.id.channelart);
                                channelart.setVisibility(View.VISIBLE);
                            }
                            break;
                        case android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            mp.start();
                            mPlaybackState = PLAYING;
                            updatePlayButton(mPlaybackState);
                            ImageView channelart = (ImageView) getView().findViewById(R.id.channelart);
                            channelart.setVisibility(View.GONE);
                            artShown = true;
                            startControllersTimer();
                            break;
                    }


                return true;
                //           return false;
            }
        });

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
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                } else if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                }
            }
        });

        mChatrealmRevealer.setOnClickListener(mChatRevelerButtonListener);
    }

    public void setChatRevelerClickListener(View.OnClickListener listener) {
        mChatRevelerButtonListener = listener;
    }

    private void updatePlayButton(PlayStreamActivity.PlaybackState state) {
        switch (state) {
            case PLAYING:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(
                        ResourcesCompat.getDrawable(getResources(), R.drawable.big_pause_button, null));
                break;
            case PAUSED:
            case IDLE:
                mLoading.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.VISIBLE);
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

    public void setLandscapeMode() {
        updateFullscreenButton(false);
        vidView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mChatrealmRevealer.setVisibility(View.VISIBLE);
        getView().findViewById(R.id.view_group_video).setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

    }

    public void setPortraitMode() {
        updateFullscreenButton(true);

        mChatrealmRevealer.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_chatrealm_reveal, null));
        mChatrealmRevealer.setVisibility(View.GONE);
        getView().findViewById(R.id.view_group_video).setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        // getting the videoview to be 16:9
        DisplayMetrics displaymetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);
        } else {
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        }
        float h = displaymetrics.heightPixels;
        float w = displaymetrics.widthPixels;
        float floatHeight = (float) (w * 0.5625);
        int intHeight = Math.round(floatHeight);
        int intWidth = (int) w;
        vidView.setLayoutParams(new FrameLayout.LayoutParams(intWidth, intHeight));

        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) getView().findViewById(R.id.toolbar_layout).getLayoutParams();
        params2.addRule(RelativeLayout.ALIGN_RIGHT, 0);
        getView().findViewById(R.id.toolbar_layout).setLayoutParams(params2);

    }

    private void togglePlayback() {
        stopControllersTimer();
        switch (mPlaybackState) {
            case PAUSED:
                vidView.start();
                mPlaybackState = PLAYING;
                startControllersTimer();
                break;

            case PLAYING:
                mPlaybackState = PAUSED;
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
        mControllersTimer = new Timer();
        mControllersTimer.schedule(new HideControllersTask(), 3000);
    }

    // should be called from the main thread
    private void updateControllersVisibility(boolean show) {
        if (show) {
            mControllers.setVisibility(View.VISIBLE);
        } else  {
            mControllers.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onChatVisibiltiyChanged(PlayStreamActivity.LandscapeChatState state) {
        if (state.equals(PlayStreamActivity.LandscapeChatState.SHOWING)) {
            mChatrealmRevealer.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_chatrealm_hide, null));

        } else if (state.equals(PlayStreamActivity.LandscapeChatState.HIDDEN)) {
            mChatrealmRevealer.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_chatrealm_reveal, null));

        } else {
            Log.wtf(TAG, "WTF?");
        }
    }

    @Override
    public void setStreamQuality(Quality quality_) {
        if (quality_.equals(currentQuality)) {
            return;
        }

        currentQuality = quality_;

        // update stream url and videoview
        this.streamUrl = channel.getStreamUrl(appConfig, currentQuality);

        vidView.setVideoPath(this.streamUrl);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mControllers != null) {
            mControllersTimer.cancel();
        }

        vidView.pause();
        mPlaybackState = PlayStreamActivity.PlaybackState.PAUSED;
    }

    @Override
    public void onResume() {
        super.onResume();



    }

    @Override
    public void hideSysUi() {
    }

    @Override
    public void showSysUi() {
    }

    @Override
    public PlayStreamActivity.PlaybackState getPlaybackState() {
        return mPlaybackState;
    }

    @Override
    public void setPlaybackState(PlayStreamActivity.PlaybackState state_) {
        this.mPlaybackState = state_;

        switch (state_) {
            case IDLE:
                vidView.stopPlayback();
                stopControllersTimer();
                break;
            case PLAYING:
                vidView.start();
                startControllersTimer();
                break;

        }
        updatePlayButton(state_);
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

