package com.tinnvec.dctvandroid.channel;

public class ChannelReader {
    private int streamid;
    private String channelname;
    private String friendlyalias;
    private String streamtype;
    private String nowonline;
    private boolean alerts;
    private String twitch_currentgame;
    private String twitch_yt_description;
    private boolean yt_upcoming;
    private String yt_liveurl;
    private String imageasset;
    private String imageassethd;
    private String urltoplayer;
    private int channel;

    public AbstractChannel getChannelInstance() {
        switch (streamtype) {
            case "rtmp-hls":
                return getDctvChannel();
            case "youtube":
                return getYoutubeChannel();
            case "twitch":
                return getTwitchChannel();
            default:
                throw new IllegalArgumentException("Streamtype " + streamtype + "is unknown or undefined");
        }
    }

    private void setCommonProperties(AbstractChannel chan) {
        chan.setChannelID(channel);
        chan.setStreamID(streamid);
        chan.setName(channelname);
        chan.setFriendlyAlias(friendlyalias);
        chan.setNowOnline(nowonline.equals("yes"));
        chan.setHasAlerts(alerts);
        chan.setDescription(twitch_yt_description);
        chan.setImageAssetUrl(imageasset);
        chan.setImageAssetHDUrl(imageassethd);
        chan.setUrlToPlayer(urltoplayer);
    }

    private DctvChannel getDctvChannel() {
        DctvChannel chan = new DctvChannel();
        setCommonProperties(chan);
        return chan;
    }

    private TwitchChannel getTwitchChannel() {
        TwitchChannel chan = new TwitchChannel();
        chan.setCurrentGame(twitch_currentgame);
        return chan;
    }

    private YoutubeChannel getYoutubeChannel() {
        YoutubeChannel chan = new YoutubeChannel();
        chan.setLiveUrl(yt_liveurl);
        chan.setUpcoming(yt_upcoming);
        return chan;
    }

    public void setStreamid(int streamid) {
        this.streamid = streamid;
    }

    public void setChannelname(String channelname) {
        this.channelname = channelname;
    }

    public void setFriendlyalias(String friendlyalias) {
        this.friendlyalias = friendlyalias;
    }

    public void setStreamtype(String streamtype) {
        this.streamtype = streamtype;
    }

    public void setNowonline(String nowonline) {
        this.nowonline = nowonline;
    }

    public void setAlerts(boolean alerts) {
        this.alerts = alerts;
    }

    public void setTwitch_currentgame(String twitch_currentgame) {
        this.twitch_currentgame = twitch_currentgame;
    }

    public void setTwitch_yt_description(String twitch_yt_description) {
        this.twitch_yt_description = twitch_yt_description;
    }

    public void setYt_upcoming(boolean yt_upcoming) {
        this.yt_upcoming = yt_upcoming;
    }

    public void setYt_liveurl(String yt_liveurl) {
        this.yt_liveurl = yt_liveurl;
    }

    public void setImageasset(String imageasset) {
        this.imageasset = imageasset;
    }

    public void setImageassethd(String imageassethd) {
        this.imageassethd = imageassethd;
    }

    public void setUrltoplayer(String urltoplayer) {
        this.urltoplayer = urltoplayer;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
}

