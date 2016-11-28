package com.tinnvec.dctvandroid;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.tinnvec.dctvandroid.channel.AbstractChannel;
import com.tinnvec.dctvandroid.channel.Quality;
import com.tinnvec.dctvandroid.channel.YoutubeChannel;

import java.util.Properties;
import java.util.Timer;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static com.tinnvec.dctvandroid.PlayStreamActivity.PlaybackState.BUFFERING;
import static com.tinnvec.dctvandroid.PlayStreamActivity.PlaybackState.PLAYING;


/**
 * Created by kev on 11/20/16.
 */

public class VitamioStreamFragment extends Fragment implements VideoFragment {
    private static final String TAG = VitamioStreamFragment.class.getName();

    private Properties appConfig;
    private AbstractChannel channel;
    private PlayStreamActivity.PlaybackState mPlaybackState;

    private VideoView vidView;

    private ImageButton mPlayPause;
    private RelativeLayout mLoading;
    private View mControllers;
    private ImageButton mFullscreenSwitch;
    private ImageButton mChatrealmRevealer;

    private boolean mControllersVisible;
    private Timer mControllersTimer;
    private boolean artShown;
    private PlayStreamActivity.PlaybackState mPlaybackState;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Vitamio.isInitialized(getActivity().getApplicationContext());

        PropertyReader pReader = new PropertyReader(this.getActivity());
        appConfig = pReader.getMyProperties("app.properties");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        channel = savedInstanceState.getParcelable("channel");

        View view = inflater.inflate(R.layout.fragment_vitamio_video, null);
        vidView = (VideoView) view.findViewById(R.id.video_view);

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
                if (mLocation == PlayStreamActivity.PlaybackLocation.LOCAL) {
                    vidView.requestFocus();
                    mp.start();
                    mPlaybackState = PLAYING;
                    updatePlayButton(mPlaybackState);
                }
                if (mLocation == PlayStreamActivity.PlaybackLocation.REMOTE) {
                    vidView.pause();
                    mp.stop();
                    setPortraitMode();
                    updatePlayButton(mPlaybackState);
                    if (mCastSession != null && mCastSession.isConnected()) loadRemoteMedia(true);
                }
            }
        });

        vidView.setOnInfoListener(new io.vov.vitamio.MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (mLocation == PlayStreamActivity.PlaybackLocation.LOCAL) {
                    switch (what) {
                        case android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            if (mp.isPlaying()) {
                                mp.pause();
                            }
                            mPlaybackState = BUFFERING;
                            updatePlayButton(mPlaybackState);
                            updateControllersVisibility(true);
                            if (!artShown) {
                                ImageView channelart = (ImageView) findViewById(R.id.channelart);
                                channelart.setVisibility(View.VISIBLE);
                            }
                            break;
                        case android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            mLocation = PlayStreamActivity.PlaybackLocation.LOCAL;
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
                if (mLandscapeChatState == PlayStreamActivity.LandscapeChatState.HIDDEN) {
                    revealChat();
                } else if (mLandscapeChatState == PlayStreamActivity.LandscapeChatState.SHOWING) {
                    hideChat();
                }
            }
        });
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
                mPlaybackState = PlayStreamActivity.PlaybackState.PAUSED;
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


    @Override
    public void setStreamQuality(Quality quality_) {

    }


    @Override
    public void hideSysUi() {
    }

    @Override
    public void showSysUi() {
    }

    @Override
    public PlayStreamActivity.PlaybackState getPlaybackState() {
        return null;
    }

    @Override
    public void setPlaybackState(PlayStreamActivity.PlaybackState state_) {

    }

}
