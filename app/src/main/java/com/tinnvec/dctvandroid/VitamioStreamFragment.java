package com.tinnvec.dctvandroid;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tinnvec.dctvandroid.channel.Quality;
import io.vov.vitamio.Vitamio;


/**
 * Created by kev on 11/20/16.
 */

public class VitamioStreamFragment extends Fragment implements VideoFragment {
    private static final String TAG = VitamioStreamFragment.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Vitamio.isInitialized(getActivity().getApplicationContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
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
