package com.tinnvec.dctvandroid.channel;

import android.os.Parcel;

import java.util.Properties;

public class DctvChannel extends AbstractChannel {

    public static final Creator<DctvChannel> CREATOR = new Creator<DctvChannel>() {
        public DctvChannel createFromParcel(Parcel in) {
            return new DctvChannel(in);
        }

        public DctvChannel[] newArray(int size) {
            return new DctvChannel[size];
        }
    };

    public DctvChannel() { }

    public DctvChannel(Parcel in) {
        super(in);
    }

    public static DctvChannel get247Channel(Properties app_config) {
        DctvChannel chan = new DctvChannel();
        chan.setFriendlyAlias("DCTV 24/7");
        chan.setName("dctv_247");
        chan.setChannelID(0);
        chan.setDescription("All Diamondclub, all the time.");
        chan.setImageAssetUrl("http://i.imgur.com/6hsN55B.png");
        chan.setImageAssetHDUrl("http://i.imgur.com/GVeytTB.png");

        chan.setStreamUrl(String.format("dctv.m3u8"));
        return chan;
    }


    @Override
    public Quality[] getAllowedQualities() {
        return Quality.values();
    }
}
