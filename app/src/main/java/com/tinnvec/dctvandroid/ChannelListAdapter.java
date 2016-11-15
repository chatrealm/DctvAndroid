package com.tinnvec.dctvandroid;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tinnvec.dctvandroid.channel.AbstractChannel;
import com.tinnvec.dctvandroid.tasks.ImageDownloaderTask;

import java.util.ArrayList;
import java.util.List;

public class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ChannelViewHolder> {
    private final List<AbstractChannel> channelList;
    private int lastPosition = -1;
    private ChannelListCallback callback;

    public ChannelListAdapter(ChannelListCallback callback) {
        this.channelList = new ArrayList<>();
        this.callback = callback;
    }

    // Clean all elements of the recycler
    public void clear() {
        channelList.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<AbstractChannel> list) {
        channelList.addAll(list);
        notifyDataSetChanged();
    }

    public List<AbstractChannel> getChannelList() {
        return this.channelList;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChannelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.live_list_item, parent, false);

        return new ChannelListAdapter.ChannelViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ChannelViewHolder holder, int position) {
        final AbstractChannel chan = channelList.get(position);
        if (chan.getImageAssetUrl() != null)
            new ImageDownloaderTask(holder.channelArt).execute(chan.getImageAssetUrl());

        holder.channelName.setText(chan.getFriendlyAlias());
        holder.channelName.setSelected(true);


        if (chan.getDescription().isEmpty()) {
            holder.channelDescription.setVisibility(View.GONE);
        } else {
            holder.channelDescription.setVisibility(View.VISIBLE);
            holder.channelDescription.setText(chan.getDescription());
            holder.channelDescription.setSelected(true);
        }

        setAnimation(holder.mLinearLayout, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            animation.setStartOffset(position * 75);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return channelList.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ChannelViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{
        LinearLayout mLinearLayout;
        TextView channelName, channelDescription;
        ImageView channelArt;

        public ChannelViewHolder(LinearLayout v) {
            super(v);
            mLinearLayout = v;
            channelName = (TextView) v.findViewById(R.id.live_item_name);
            channelDescription = (TextView) v.findViewById(R.id.live_item_description);
            channelArt = (ImageView) v.findViewById(R.id.live_item_art);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            AbstractChannel channel = channelList.get(getAdapterPosition());
            callback.onChannelClicked(channel);
        }
    }
}