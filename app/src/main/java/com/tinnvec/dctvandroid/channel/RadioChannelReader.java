package com.tinnvec.dctvandroid.channel;

public class RadioChannelReader {
    private int streamid;
    private String channelname;
    private String streamUrl;
    private String friendlyalias;
    private String streamtype;
    private String description;
    private String imageasset;
    private String imageassethd;
    private int channel;

    public RadioChannel getChannelInstance() {
        switch (streamtype) {
            case "dcfm":
                return getDcfmChannel();
            default:
                throw new IllegalArgumentException("Streamtype " + streamtype + "is unknown or undefined");
        }
    }

    private void setCommonProperties(RadioChannel chan) {
        chan.setChannelID(channel);
        chan.setStreamID(streamid);
        chan.setName(channelname);
        chan.setStreamUrl(streamUrl);
        chan.setFriendlyAlias(friendlyalias);
        chan.setDescription(description);
        chan.setImageAssetUrl(imageasset.isEmpty() ? null : imageasset);
        chan.setImageAssetHDUrl(imageassethd.isEmpty() ? null : imageassethd);
    }

    private DcfmChannel getDcfmChannel() {
        DcfmChannel chan = new DcfmChannel();
        setCommonProperties(chan);
        return chan;
    }

    public void setStreamid(int streamid) {
        this.streamid = streamid;
    }

    public void setChannelname(String channelname) {
        this.channelname = channelname;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public void setFriendlyalias(String friendlyalias) {
        this.friendlyalias = friendlyalias;
    }

    public void setStreamtype(String streamtype) {
        this.streamtype = streamtype;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageasset(String imageasset) {
        this.imageasset = imageasset;
    }

    public void setImageassethd(String imageassethd) {
        this.imageassethd = imageassethd;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
}

