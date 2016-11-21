package com.tinnvec.dctvandroid;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

/**
 * Created by kev on 11/21/16.
 */

public class ChatFragment extends Fragment {
    private Properties appConfig;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appConfig = ((DctvApplication)getActivity().getApplication()).getAppConfig();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        View chatLayout = inflater.inflate(R.layout.fragment_chat, null);
        WebView chatWebview = (WebView)chatLayout.findViewById(R.id.chat_webview);

        WebSettings settings = chatWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        String url = appConfig.getProperty("irc.web_url");
        String nick = "";
        try {
            nick = URLEncoder.encode(sharedPreferences.getString("chat_name", ""), "UTF-8");
        } catch (UnsupportedEncodingException e) {

        }
        url = url + "?nick=" + nick;
        chatWebview.loadUrl(url);

        return chatLayout;
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
