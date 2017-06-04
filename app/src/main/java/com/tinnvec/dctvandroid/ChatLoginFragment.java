package com.tinnvec.dctvandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Properties;


public class ChatLoginFragment extends Fragment {

    private String AUTH_URL;
    private String CLIENT_ID;
    private String REDIRECT_URL;

    private RadioButton twitchRadioButton;
    private RadioButton chatrealmRadioButton;
    private Button loginButton;
    private Button twitchAuthButton;
    private TextView twitchAuthStatusView;
    private String server;
    private EditText nicknameEditText;
    private EditText channelEditText;
    private String twitchToken;
    private String twitchUsername;

    private String streamService;
    private String channelName;

    private View rootview;

    private View.OnClickListener onRadioButtonClickListener;

    private SharedPreferences sharedPreferences;
    private Properties appConfig;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        appConfig = ((DctvApplication) getActivity().getApplication()).getAppConfig();
        AUTH_URL = appConfig.getProperty("api.twitch.auth_url");
        CLIENT_ID = appConfig.getProperty("api.twitch.client_id");
        REDIRECT_URL = appConfig.getProperty("api.twitch.redirect_url");

        View v = inflater.inflate(R.layout.fragment_chat_login, container, false);

        rootview = container;

        if (IRCClient.isRunningAndConnected()) {
            NativeChatFragment newFragment = new NativeChatFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(rootview.getId(), newFragment);
            transaction.commit();
        }

        onRadioButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRadioButtonClicked(view);
            }
        };

        twitchRadioButton = (RadioButton) v.findViewById(R.id.twitchRadioButton);
        chatrealmRadioButton = (RadioButton) v.findViewById(R.id.chatrealmRadioButton);

        twitchRadioButton.setOnClickListener(onRadioButtonClickListener);
        chatrealmRadioButton.setOnClickListener(onRadioButtonClickListener);

        loginButton = (Button) v.findViewById(R.id.loginButton);
        twitchAuthButton = (Button) v.findViewById(R.id.twitchLoginButton);

        twitchAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTwitchAuthButtonClicked(view);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLoginButtonClicked(view);
            }
        });

        twitchAuthStatusView = (TextView) v.findViewById(R.id.twitchLinkStatus);
        nicknameEditText = (EditText) v.findViewById(R.id.nickNameEditText);
        channelEditText = (EditText) v.findViewById(R.id.channelEditText);

        twitchToken = sharedPreferences.getString("TWITCH_TOKEN", null);
        twitchUsername = sharedPreferences.getString("TWITCH_USERNAME", null);

        if (twitchToken != null && twitchUsername != null) {
            twitchAuthButton.setText(R.string.unlink_twitch);
            twitchAuthButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cancel_red, 0, 0, 0);
            twitchAuthStatusView.setText(String.format(getString(R.string.twitch_link_status_positive), twitchUsername));
        }

        if (this.getArguments() != null) {
            streamService = this.getArguments().getString("streamService");
            channelName = this.getArguments().getString("channelName");
            if (channelName != null && (channelName.equals("jurystream") || channelName.equals("politics"))) {
                channelName = "justinryoung";
            }
            if (channelName != null && (channelName.equals("cordkillers") || channelName.equals("weridthings") || channelName.equals("bizarre") || channelName.equals("none"))) {
                channelName = "nightattack";
            }
        } else if (((PlayStreamActivity) getActivity()).getNowPlayingService() != null && ((PlayStreamActivity) getActivity()).getNowPlayingChannelName() != null) {
            streamService = ((PlayStreamActivity) getActivity()).getNowPlayingService();
            channelName = ((PlayStreamActivity) getActivity()).getNowPlayingChannelName();
        } else {
            streamService = "";
            channelName = "";
        }


        if (streamService.equals("twitch")) {
            twitchRadioButton.setChecked(true);
            selectTwitch();
        } else {
            chatrealmRadioButton.setChecked(true);
            selectChatrealm();
        }

        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.twitchRadioButton:
                if (checked)
                    chatrealmRadioButton.setChecked(false);
                selectTwitch();
                break;
            case R.id.chatrealmRadioButton:
                if (checked)
                    selectChatrealm();
                break;
        }
    }

    private void selectTwitch() {
        chatrealmRadioButton.setChecked(false);
        server = appConfig.getProperty("irc.twitch_url");
        twitchAuthStatusView.setVisibility(View.VISIBLE);
        twitchAuthButton.setVisibility(View.VISIBLE);
        if (twitchUsername == null) {
            nicknameEditText.setText("Link to Twitch to login");
            nicknameEditText.setEnabled(false);
            loginButton.setEnabled(false);
        } else {
            nicknameEditText.setText(twitchUsername);
            nicknameEditText.setEnabled(false);
            channelEditText.setText(channelName);
        }
    }

    private void selectChatrealm() {
        twitchRadioButton.setChecked(false);
        server = appConfig.getProperty("irc.chatrealm_url");
        twitchAuthStatusView.setVisibility(View.GONE);
        twitchAuthButton.setVisibility(View.GONE);
        String preferenceNickname = sharedPreferences.getString("chat_name", null);
        String nickname;
        if (preferenceNickname == null || preferenceNickname.isEmpty()) {
            nickname = "AndroidDCTV-" + (int) (Math.random() * 1000);
        } else {
            nickname = preferenceNickname;
        }
        nicknameEditText.setText(nickname);
        nicknameEditText.setEnabled(true);
        channelEditText.setText("chat");
    }

    public void onTwitchAuthButtonClicked(View view) {
        String twitchToken = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("TWITCH_TOKEN", null);
        if (twitchToken == null) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AUTH_URL + "?client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URL + "&response_type=token&scope=openid+chat_login+channel_read"));
            startActivity(browserIntent);
        } else {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("TWITCH_TOKEN", null).apply();
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("TWITCH_USERNAME", null).apply();
            twitchAuthButton.setText(R.string.login_to_twitch);
            twitchAuthButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_twitch_white, 0, 0, 0);
        }
    }

    public void onLoginButtonClicked(View view) {

        String channel = "#" + channelEditText.getText().toString();
        boolean twitchServer = false;
        if (twitchRadioButton.isChecked()) {
            twitchServer = true;
        }
        String nickName = nicknameEditText.getText().toString();

        NativeChatFragment newFragment = new NativeChatFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        Bundle bundle = new Bundle();
        bundle.putString("server", server);
        if (twitchServer) {
            String twitchPass = "oauth:" + twitchToken;
            bundle.putString("pass", twitchPass);
            bundle.putString("nick", nickName.toLowerCase());
        } else {
            bundle.putString("pass", "");
            bundle.putString("nick", nickName);
        }
        bundle.putString("channel", channel);
        bundle.putString("channelName", channelName);
        bundle.putString("streamService", streamService);
        newFragment.setArguments(bundle);

        transaction.replace(rootview.getId(), newFragment);
//        transaction.addToBackStack(null);
        transaction.commit();
    }
}
