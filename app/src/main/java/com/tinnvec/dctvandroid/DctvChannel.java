package com.tinnvec.dctvandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

public class DctvChannel implements Parcelable {

    public static final Creator<DctvChannel> CREATOR = new Creator<DctvChannel>() {
        public DctvChannel createFromParcel(Parcel in) {
            return new DctvChannel(in);
        }

        public DctvChannel[] newArray(int size) {
            return new DctvChannel[size];
        }
    };
    private static String TAG = DctvChannel.class.getName();
    public int streamid;
    public String channelname;
    public String friendlyalias;
    public String streamtype;
    public String nowonline;
    public boolean alerts;
    public String twitch_currentgame;
    public String twitch_yt_description;
    public boolean yt_upcoming;
    public String yt_liveurl;
    public String imageasset;
    public String imageassethd;
    public String urltoplayer;
    public int channel;

    private DctvChannel() {
        this(null);
    }

    public DctvChannel(Parcel source) {
        if (source != null) {
            this.streamid = source.readInt();
            this.channelname = source.readString();
            this.friendlyalias = source.readString();
            this.streamtype = source.readString();
            this.nowonline = source.readString();
            this.alerts = source.readByte() != 0;
            this.twitch_currentgame = source.readString();
            this.twitch_yt_description = source.readString();
            this.yt_upcoming = source.readByte() != 0;
            this.yt_liveurl = source.readString();
            this.imageasset = source.readString();
            this.imageassethd = source.readString();
            this.urltoplayer = source.readString();
            this.channel = source.readInt();
        }
    }

    public static final DctvChannel get247Channel(Context context) {
        DctvChannel chan = new DctvChannel();
        chan.friendlyalias = "DCTV 24/7";
        chan.channelname = "dctv";
        chan.channel = 0;
        chan.twitch_yt_description = "All Diamondclub, all the time.";
        return chan;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.streamid);
        dest.writeString(this.channelname);
        dest.writeString(this.friendlyalias);
        dest.writeString(this.streamtype);
        dest.writeString(this.nowonline);
        dest.writeByte((byte) (this.alerts ? 1 : 0));
        dest.writeString(this.twitch_currentgame);
        dest.writeString(this.twitch_yt_description);
        dest.writeByte((byte) (this.yt_upcoming ? 1 : 0));
        dest.writeString(this.yt_liveurl);
        dest.writeString(this.imageasset);
        dest.writeString(this.imageassethd);
        dest.writeString(this.urltoplayer);
        dest.writeInt(this.channel);
    }

    public Bitmap getImageBitmap(Context context) {
        if (this.channelname.equals("dctv")) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.dctv_247_channel);
            return Bitmap.createScaledBitmap(bitmap, 300, 120, true);
        } else {
            return null;

        }
    }

    public boolean hasLocalChannelArt() {
        return this.channelname.equals("dctv");
    }

    public String getChannelArtUrl() {
        if (this.channelname.equals("dctv")) {
            return "http://i.imgur.com/GVeytTB.png";
        } else {
            return this.imageasset;
        }
    }
    // for Cast MediaInfobuilder
    public String getBigChannelArtUrl() {
        if (this.channelname.equals("dctv")) {
            return "http://i.imgur.com/GVeytTB.png";
        }
        else {
            return this.imageassethd;
        }
    }

    public String getStreamtype() {
        if (this.channelname.equals("dctv")) {
            return "rtmp-hls";
        }
        else {
            return this.streamtype;
        }
    }

    public String getFriendlyAlias() {
        return this.friendlyalias;
    }

    public String getChannelname() {
        return this.channelname;
    }

    public boolean isAlerts() {
        return alerts;
    }

    public String getStreamUrl(Context context) {
        if (this.channelname.equals("dctv")) {
            String baseUrl = context.getString(R.string.dctv_ingest_base_url);

            return String.format("%shls2/dctv.m3u8", baseUrl);
        } else {
            String baseUrl = context.getString(R.string.dctv_base_url);

            return String.format("%sapi/hlsredirect.php?c=%d", baseUrl, this.channel);
        }
    }
}
