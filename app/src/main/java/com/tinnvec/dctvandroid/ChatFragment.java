package com.tinnvec.dctvandroid;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

// Needed to allow users to log in to twitch chat
        chatWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
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
            chatWebview.loadUrl(url);
            chatWebview.reload();
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
                chatWebview.loadUrl(url);
                chatWebview.reload();
                chatRoomType = "alt";
                shouldDisplayChatroomSwitcher = true;
                return;
            }
        }
        url = url + "?nick=" + nick + chatRoom;
        chatWebview.loadUrl(url);
        chatWebview.reload();
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
