package com.tinnvec.dctvandroid.channel;

import android.os.Parcel;

import java.util.Properties;

public class DcfmChannel extends RadioChannel {

    public static final Creator<DcfmChannel> CREATOR = new Creator<DcfmChannel>() {
        public DcfmChannel createFromParcel(Parcel in) {
            return new DcfmChannel(in);
        }

        public DcfmChannel[] newArray(int size) {
            return new DcfmChannel[size];
        }
    };

    public DcfmChannel() {
    }

    public DcfmChannel(Parcel in) {
        super(in);
    }
}