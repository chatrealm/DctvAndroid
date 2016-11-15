package com.tinnvec.dctvandroid.channel;

import android.content.Context;
import android.os.Parcel;

import com.tinnvec.dctvandroid.R;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static android.R.attr.format;

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
    public String getStreamUrl(Properties app_config, Quality quality) {
        if (streamUrl != null) {
            String quality247;
            if (quality.toString().equals("source")) {
                quality247 = "hls2";
            } else {
                quality247 = quality.toString();
            }
            String baseUrl = app_config.getProperty("api.dctv.ingest_url");
            String url247 = String.format("%s%s/%s", baseUrl, quality247, streamUrl);
            return url247;
        }
        String baseUrl = app_config.getProperty("api.dctv.base_url");
        String url = String.format("%sapi/hlsredirect.php?c=%d&q=%s", baseUrl, channelID, quality.toString());
        return url;
    }

    @Override
    public Quality[] getAllowedQualities() {
        return Quality.values();
    }
}
