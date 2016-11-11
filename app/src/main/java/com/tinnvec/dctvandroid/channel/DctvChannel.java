package com.tinnvec.dctvandroid.channel;

import android.content.Context;
import android.os.Parcel;

import com.tinnvec.dctvandroid.R;

public class DctvChannel extends AbstractChannel {

    public static final Creator<DctvChannel> CREATOR = new Creator<DctvChannel>() {
        public DctvChannel createFromParcel(Parcel in) {
            return new DctvChannel(in);
        }

        public DctvChannel[] newArray(int size) {
            return new DctvChannel[size];
        }
    };

    public DctvChannel() { this(null); }

    public DctvChannel(Parcel in) {
        super(in);
    }

    public static DctvChannel get247Channel(Context context) {
        DctvChannel chan = new DctvChannel();
        chan.setFriendlyAlias("DCTV 24/7");
        chan.setName("dctv_247");
        chan.setChannelID(0);
        chan.setDescription("All Diamondclub, all the time.");
        chan.setImageAssetUrl("http://i.imgur.com/GVeytTB.png");
        chan.setImageAssetHDUrl("http://i.imgur.com/GVeytTB.png");

        String baseUrl = context.getString(R.string.dctv_ingest_base_url);
        chan.setStreamUrl(String.format("%shls2/dctv.m3u8", baseUrl));
        return chan;
    }

    @Override
    public String getStreamUrl(Context context) {
        if (streamUrl != null) return streamUrl;

        String baseUrl = context.getString(R.string.dctv_base_url);
        return String.format("%sapi/hlsredirect.php?c=%d", baseUrl, channelID);
    }
}
