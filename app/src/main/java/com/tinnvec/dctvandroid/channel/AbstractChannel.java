package com.tinnvec.dctvandroid.channel;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.Properties;

public abstract class AbstractChannel implements Parcelable {
    int channelID;
    int streamID;
    String name;
    String description;
    String friendlyAlias;
    boolean nowOnline;
    boolean hasAlerts;
    String imageAssetUrl;
    String imageAssetHDUrl;
    String urlToPlayer;
    String streamUrl;

    public AbstractChannel() {};

    public AbstractChannel(Parcel in) {
        channelID = in.readInt();
        streamID = in.readInt();
        name = in.readString();
        description = in.readString();
        friendlyAlias = in.readString();
        nowOnline = in.readByte() != 0;
        hasAlerts = in.readByte() != 0;
        imageAssetUrl = in.readString();
        imageAssetHDUrl = in.readString();
        urlToPlayer = in.readString();
        streamUrl = in.readString();
    }

    public abstract String getStreamUrl(Properties app_conf, Quality quality);

    public String getDirectStreamUrl(Properties app_config, Quality quality) {
        String url = getStreamUrl(app_config, quality);
        throw new UnsupportedOperationException("need to implement method");
    }


    public abstract Quality[] getAllowedQualities();

    public int getChannelID() {
        return channelID;
    }

    public int getStreamID() {
        return streamID;
    }

    public String getName() {
        return name;
    }

    public String getFriendlyAlias() {
        return friendlyAlias;
    }

    public boolean isNowOnline() {
        return nowOnline;
    }

    public boolean hasAlerts() {
        return hasAlerts;
    }

    public String getImageAssetUrl() {
        return imageAssetUrl;
    }

    public String getImageAssetHDUrl() {
        return imageAssetHDUrl;
    }

    public String getUrlToPlayer() {
        return urlToPlayer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void setChannelID(int channelID) {
        this.channelID = channelID;
    }

    public void setStreamID(int streamID) {
        this.streamID = streamID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFriendlyAlias(String friendlyAlias) {
        this.friendlyAlias = friendlyAlias;
    }

    public void setNowOnline(boolean nowOnline) {
        this.nowOnline = nowOnline;
    }

    public void setHasAlerts(boolean hasAlerts) {
        this.hasAlerts = hasAlerts;
    }

    public void setImageAssetUrl(String imageAssetUrl) {
        this.imageAssetUrl = imageAssetUrl;
    }

    public void setImageAssetHDUrl(String imageAssetHDUrl) {
        this.imageAssetHDUrl = imageAssetHDUrl;
    }

    public void setUrlToPlayer(String urlToPlayer) {
        this.urlToPlayer = urlToPlayer;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(channelID);
        dest.writeInt(streamID);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(friendlyAlias);
        dest.writeByte((byte) (nowOnline ? 1 : 0));
        dest.writeByte((byte) (hasAlerts ? 1 : 0));
        dest.writeString(imageAssetUrl);
        dest.writeString(imageAssetHDUrl);
        dest.writeString(urlToPlayer);
        dest.writeString(streamUrl);
    }
}
