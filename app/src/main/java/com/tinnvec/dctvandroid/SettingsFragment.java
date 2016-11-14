package com.tinnvec.dctvandroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import static android.R.attr.key;
import static android.R.attr.y;
import static com.tinnvec.dctvandroid.R.string.chat;

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
        if (chatName != null && !"".equals(chatName)) {
            connectionPref.setSummary(chatName);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals("chat_name")) {
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
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


