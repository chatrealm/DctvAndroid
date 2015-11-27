package com.example.tinnvec.dctvandroid;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class DctvChannel implements Parcelable {
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
    public Bitmap imageassetBitmap;
    public String imageassethd;
    public String urltoplayer;
    public int channel;

    protected DctvChannel(Parcel source) {
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
            this.imageassetBitmap = (Bitmap) source.readValue(Bitmap.class.getClassLoader());
            this.imageassethd = source.readString();
            this.urltoplayer = source.readString();
            this.channel = source.readInt();
        }
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
        dest.writeValue(this.imageassetBitmap);
        dest.writeString(this.imageassethd);
        dest.writeString(this.urltoplayer);
        dest.writeInt(this.channel);
    }

    public static final Creator<DctvChannel> CREATOR = new Creator<DctvChannel>() {
        public DctvChannel createFromParcel(Parcel in) {
            return new DctvChannel(in);
        }

        public DctvChannel[] newArray(int size) {
            return new DctvChannel[size];
        }
    };
}
