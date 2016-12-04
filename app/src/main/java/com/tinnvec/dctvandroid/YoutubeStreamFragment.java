package com.tinnvec.dctvandroid;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.tinnvec.dctvandroid.channel.Quality;

import java.util.Properties;

/**
 * Created by kev on 11/21/16.
 */

public class YoutubeStreamFragment extends YouTubePlayerFragment implements YouTubePlayer.OnInitializedListener,VideoFragment {
    private static final int RQS_ErrorDialog = 1;
    private Properties appConfig;
    private String videoId;
    private YouTubePlayer youTubePlayer;
    private MyPlayerStateChangeListener myPlayerStateChangeListener;
    private MyPlaybackEventListener myPlaybackEventListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.videoId = getArguments().getString("VIDEO_ID");
        appConfig = ((DctvApplication)getActivity().getApplication()).getAppConfig();
        this.initialize(appConfig.getProperty("api.youtube.api_key"), this);
    }


    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult result) {

        if (result.isUserRecoverableError()) {
            result.getErrorDialog(this.getActivity(), RQS_ErrorDialog).show();
        } else {
            Toast.makeText(this.getActivity().getApplicationContext(),
                    "YouTubePlayer.onInitializationFailure(): " + result.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {

        youTubePlayer = player;

        Toast.makeText(getActivity().getApplicationContext(),
                "YouTubePlayer.onInitializationSuccess()",
                Toast.LENGTH_LONG).show();

        youTubePlayer.setPlayerStateChangeListener(myPlayerStateChangeListener);
        youTubePlayer.setPlaybackEventListener(myPlaybackEventListener);

        if (!wasRestored) {
            player.cueVideo(videoId);
        }

    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {

        private void updateLog(String prompt){
            Log.i("YoutubeState",prompt);
        };

        @Override
        public void onAdStarted() {
            updateLog("onAdStarted()");
        }

        @Override
        public void onError(
                YouTubePlayer.ErrorReason arg0) {
            updateLog("onError(): " + arg0.toString());
        }

        @Override
        public void onLoaded(String arg0) {
            updateLog("onLoaded(): " + arg0);
        }

        @Override
        public void onLoading() {
            updateLog("onLoading()");
        }

        @Override
        public void onVideoEnded() {
            updateLog("onVideoEnded()");
        }

        @Override
        public void onVideoStarted() {
            updateLog("onVideoStarted()");
        }

    }

    private final class MyPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {

        private void updateLog(String prompt){
            Log.i("YoutubeEvent",prompt);
        };

        @Override
        public void onBuffering(boolean arg0) {
            updateLog("onBuffering(): " + String.valueOf(arg0));
        }

        @Override
        public void onPaused() {
            updateLog("onPaused()");
        }

        @Override
        public void onPlaying() {
            updateLog("onPlaying()");
        }

        @Override
        public void onSeekTo(int arg0) {
            updateLog("onSeekTo(): " + String.valueOf(arg0));
        }

        @Override
        public void onStopped() {
            updateLog("onStopped()");
        }

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

    @Override
    public void setLandscapeMode() {

    }

    @Override
    public void setPortraitMode() {

    }
}
