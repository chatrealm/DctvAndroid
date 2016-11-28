package com.tinnvec.dctvandroid;

import android.app.Fragment;

import com.tinnvec.dctvandroid.channel.Quality;

/**
 * Created by kev on 11/21/16.
 */
public interface VideoFragment {
    public void setStreamQuality(Quality quality_);

    public void hideSysUi();

    public void showSysUi();

    public PlayStreamActivity.PlaybackState getPlaybackState();

    public void setPlaybackState(PlayStreamActivity.PlaybackState state_);
}
