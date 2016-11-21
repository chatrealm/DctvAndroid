package com.tinnvec.dctvandroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.tinnvec.dctvandroid.channel.Quality;


/**
 * Created by kev on 11/13/16.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        Preference connectionPref = findPreference("chat_name");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        // Set summary to be the user-description for the selected value
        String chatName = sharedPreferences.getString("chat_name", "");
        if (!chatName.isEmpty()) {
            connectionPref.setSummary(chatName);
        }

        // set quality list from Quality enum
        ListPreference qualityPreference = (ListPreference) findPreference("stream_quality");
        qualityPreference.setEntries(Quality.allAsStrings());
        qualityPreference.setEntryValues(Quality.allAsStrings());

        String streamQuality = sharedPreferences.getString("stream_quality", "");
        if (!streamQuality.isEmpty()) {
            qualityPreference.setSummary(streamQuality);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals("chat_name")) {
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
        } else if (key.equals("stream_quality")) {
            Preference qualityPref = findPreference("stream_quality");
            qualityPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

}


