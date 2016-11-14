package com.tinnvec.dctvandroid;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by kev on 11/13/16.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}


