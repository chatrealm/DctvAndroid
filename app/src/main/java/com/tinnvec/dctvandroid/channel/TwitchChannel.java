package com.tinnvec.dctvandroid.channel;


import android.content.Context;
import android.os.Parcel;

import com.tinnvec.dctvandroid.R;

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

    public TwitchChannel() { this(null); }

    public TwitchChannel(Parcel in) {
        super(in);
        currentGame = in.readString();
    }
    @Override
    public String getStreamUrl(Context context) {
        if (streamUrl != null) return streamUrl;

        String baseUrl = context.getString(R.string.dctv_base_url);
        return String.format("%sapi/hlsredirect.php?c=%d", baseUrl, channelID);
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
