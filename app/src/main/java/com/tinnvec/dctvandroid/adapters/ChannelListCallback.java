package com.tinnvec.dctvandroid.adapters;


import android.widget.ImageView;

import com.tinnvec.dctvandroid.channel.AbstractChannel;

public interface ChannelListCallback {
    void onChannelClicked(AbstractChannel channel, int position, ImageView channelArt);
}
