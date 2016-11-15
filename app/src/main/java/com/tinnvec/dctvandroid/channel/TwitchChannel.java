package com.tinnvec.dctvandroid.channel;


import android.os.Parcel;

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
