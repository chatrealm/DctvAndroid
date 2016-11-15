package com.tinnvec.dctvandroid.channel;

import android.os.Parcel;
import android.os.Parcelable;

import com.tinnvec.dctvandroid.tasks.ResolveStreamUrlTask;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

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

    public String getStreamUrl(Properties app_config, Quality quality) {
        if (streamUrl.equals("dctv.m3u8")) {
            String quality247;
            if (quality.toString().equals("SOURCE")) {
                quality247 = "hls2";
            } else {
                quality247 = quality.toString().toLowerCase();
            }
            String baseUrl = app_config.getProperty("api.dctv.ingest_url");
            String url247 = String.format("%s%s/%s", baseUrl, quality247, streamUrl);
            return url247;
        }
        String baseUrl = app_config.getProperty("api.dctv.base_url");
        String url = String.format("%sapi/hlsredirect.php?c=%d&q=%s", baseUrl, channelID, quality.toString().toLowerCase());
        return url;
    }

    public String getResolvedStreamUrl(String url) throws ExecutionException, InterruptedException{
        ResolveStreamUrlTask task = new ResolveStreamUrlTask();
        return task.execute(url).get();
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
