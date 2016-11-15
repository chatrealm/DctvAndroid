package com.tinnvec.dctvandroid.channel;


import android.content.Context;
import android.os.Parcel;

import com.tinnvec.dctvandroid.R;

import java.util.Properties;

public class TwitchChannel extends AbstractChannel {
    public static final Creator<TwitchChannel> CREATOR = new Creator<TwitchChannel>() {
        public TwitchChannel createFromParcel(Parcel in) {
            return new TwitchChannel(in);
        }

        public TwitchChannel[] newArray(int size) {
            return new TwitchChannel[size];
        }
    };

    private String currentGame;

    public TwitchChannel() { }

    public TwitchChannel(Parcel in) {
        super(in);
        currentGame = in.readString();
    }
    @Override
    public String getStreamUrl(Properties app_config, Quality quality) {
        if (streamUrl != null) return streamUrl;
        String baseUrl = app_config.getProperty("api.dctv.base_url");
        String url = String.format("%sapi/hlsredirect.php?c=%d&q=%s", baseUrl, channelID, quality.toString());
        return url;
    }

    @Override
    public Quality[] getAllowedQualities() {
        return Quality.values();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(currentGame);
    }

    public String getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(String currentGame) {
        this.currentGame = currentGame;
    }
}
