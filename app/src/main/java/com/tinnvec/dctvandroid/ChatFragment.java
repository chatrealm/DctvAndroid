package com.tinnvec.dctvandroid;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

/**
 * Created by kev on 11/21/16.
 */

public class ChatFragment extends Fragment {
    private Properties appConfig;
    private WebView chatWebview;
    private SharedPreferences sharedPreferences;
    private String chatRoomType;
    private boolean shouldDisplayChatroomSwitcher;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appConfig = ((DctvApplication) getActivity().getApplication()).getAppConfig();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        View chatLayout = inflater.inflate(R.layout.fragment_chat, null);
        chatWebview = (WebView) chatLayout.findViewById(R.id.chat_webview);

        WebSettings settings = chatWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

// Needed to allow users to log in to twitch chat while opening other links externally
        chatWebview.setWebChromeClient(new WebChromeClient());
        chatWebview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("twitch.tv")) {
                    return false;
                } else {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    return true;
                }
            }
        });

        String streamService = this.getArguments().getString("streamService");
        String channelName = this.getArguments().getString("channelName");

        setChatroom(streamService, channelName);

        return chatLayout;
    }

    public void setChatroom(String streamService, String channelName) {
        String url = appConfig.getProperty("irc.web_url");
        String chatRoom = "#chat";
        String nick = "";
        chatRoomType = "main";
        try {
            nick = URLEncoder.encode(sharedPreferences.getString("chat_name", ""), "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        if (streamService.equals("dctv") || streamService.equals("youtube")) {
            chatRoomType = "main";
            shouldDisplayChatroomSwitcher = false;
            if (channelName.equals("thegizwiz") || channelName.equals("OMGChad")) {
                chatRoom = "#gizwiz";
                chatRoomType = "alt";
                shouldDisplayChatroomSwitcher = true;
            } else if (channelName.equals("frogpantsstudios")) {
                chatRoom = "#frogpants";
                chatRoomType = "alt";
                shouldDisplayChatroomSwitcher = true;
            } else if (channelName.startsWith("gfq")) {
                url = "http://irc.gfqnetwork.com/";
                chatRoom = "#GFQNetwork";
                chatRoomType = "alt";
                shouldDisplayChatroomSwitcher = true;
            } else if (channelName.equals("vodsquad")) {
                chatRoom = "#vodsquad";
                chatRoomType = "alt";
                shouldDisplayChatroomSwitcher = true;
            }
            url = url + "?nick=" + nick + chatRoom;
            chatWebview.clearHistory();
            chatWebview.loadUrl(url);
            return;
        } else if (streamService.equals("twitch")) {
            if (channelName.equals("coverville")){
                chatRoom = "#frogpants";
                chatRoomType = "alt";
                shouldDisplayChatroomSwitcher = true;
            } else {
                url = "https://www.twitch.tv/";
                chatRoom = channelName;
                url = url + chatRoom + "/chat?popout=";
                chatWebview.clearHistory();
                chatWebview.loadUrl(url);
                chatRoomType = "alt";
                shouldDisplayChatroomSwitcher = true;
                return;
            }
        }
        url = url + "?nick=" + nick + chatRoom;
        chatWebview.clearHistory();
        chatWebview.loadUrl(url);
    }

    public String getDisplayedChatroomType() {
        return chatRoomType;
    }

    public boolean shouldDisplayChatroomSwitcher(){
        return shouldDisplayChatroomSwitcher;
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
