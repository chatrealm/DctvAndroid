package com.tinnvec.dctvandroid.adapters;


import com.tinnvec.dctvandroid.channel.AbstractChannel;
import com.tinnvec.dctvandroid.channel.RadioChannel;

public interface RadioListCallback {
    void onChannelClicked(RadioChannel channel);
}
