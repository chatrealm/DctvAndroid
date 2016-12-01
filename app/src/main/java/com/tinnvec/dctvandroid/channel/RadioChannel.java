package com.tinnvec.dctvandroid.channel;

import android.os.Parcel;
import android.os.Parcelable;

import com.tinnvec.dctvandroid.tasks.ResolveStreamUrlTask;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public abstract class RadioChannel implements Parcelable {
    int channelID;
    int streamID;
    String name;
    String description;
    String friendlyAlias;
    String imageAssetUrl;
    String imageAssetHDUrl;
    String streamUrl;

    public RadioChannel() {
    }

    ;

    public RadioChannel(Parcel in) {
        channelID = in.readInt();
        streamID = in.readInt();
        name = in.readString();
        description = in.readString();
        friendlyAlias = in.readString();
        imageAssetUrl = in.readString();
        imageAssetHDUrl = in.readString();
        streamUrl = in.readString();
    }

    public int getChannelID() {
        return channelID;
    }

    public void setChannelID(int channelID) {
        this.channelID = channelID;
    }

    public int getStreamID() {
        return streamID;
    }

    public void setStreamID(int streamID) {
        this.streamID = streamID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFriendlyAlias() {
        return friendlyAlias;
    }

    public void setFriendlyAlias(String friendlyAlias) {
        this.friendlyAlias = friendlyAlias;
    }

    public String getImageAssetUrl() {
        return imageAssetUrl;
    }

    public void setImageAssetUrl(String imageAssetUrl) {
        this.imageAssetUrl = imageAssetUrl;
    }

    public String getImageAssetHDUrl() {
        return imageAssetHDUrl;
    }

    public void setImageAssetHDUrl(String imageAssetHDUrl) {
        this.imageAssetHDUrl = imageAssetHDUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getStreamUrl() {
        return streamUrl;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(channelID);
        dest.writeInt(streamID);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(friendlyAlias);
        dest.writeString(imageAssetUrl);
        dest.writeString(imageAssetHDUrl);
        dest.writeString(streamUrl);
    }
}
