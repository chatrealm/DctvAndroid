package com.tinnvec.dctvandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.tinnvec.dctvandroid.channel.AbstractChannel;
import com.tinnvec.dctvandroid.tasks.ImageDownloaderTask;
import com.tinnvec.dctvandroid.tasks.LoadLiveChannelsTask;

import java.util.ArrayList;
import java.util.List;

public class LiveChannelsActivity extends AppCompatActivity {

    public static final String CHANNEL_DATA = "com.tinnvec.dctv_android.CHANNEL_MESSAGE";
    private static final String TAG = LiveChannelsActivity.class.getName();
    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private SwipeRefreshLayout swipeContainer;

    // added for cast SDK v3
    private CastContext mCastContext;
    private MenuItem mediaRouteMenuItem;
    private IntroductoryOverlay mIntroductoryOverlay;
    private CastStateListener mCastStateListener;
    private int lastPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_channels);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCastStateListener = new CastStateListener() {
            @Override
            public void onCastStateChanged(int newState) {
                if (newState != CastState.NO_DEVICES_AVAILABLE) {
                    showIntroductoryOverlay();
                }
            }
        };

        mCastContext = CastContext.getSharedInstance(this); // initialises castcontext

        mRecyclerView = (RecyclerView) findViewById(R.id.live_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getBaseContext(), null));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new ImageAdapter();
        mRecyclerView.setAdapter(mAdapter);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new LoadLiveChannelsTask(mRecyclerView) {


                    protected void onPostExecute(List<AbstractChannel> result) {
                        ImageAdapter adapter = (ImageAdapter) mRecyclerView.getAdapter();
                        adapter.clear();
                        if (result != null && !result.isEmpty()) {
                            adapter.addAll(result);
                        }
                        swipeContainer.setRefreshing(false);
                    }
                }.execute();
            }
        });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ArrayList<AbstractChannel> savedChannels = null;
        if (savedInstanceState != null) {
            savedChannels = savedInstanceState.getParcelableArrayList("CHANNEL_LIST");
        }
        if (savedChannels != null) {
            mAdapter.addAll(savedChannels);
        } else {
            new LoadLiveChannelsTask(mRecyclerView).execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_live_channels, menu);

        // add media router button for cast
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_chat) {
            Intent intent = new Intent(getBaseContext(), JustChatActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay.remove();
        }
        if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mIntroductoryOverlay = new IntroductoryOverlay.Builder(
                            LiveChannelsActivity.this, mediaRouteMenuItem)
                            .setTitleText("Introducing Cast")
                            .setSingleTime()
                            .setOnOverlayDismissedListener(
                                    new IntroductoryOverlay.OnOverlayDismissedListener() {
                                        @Override
                                        public void onOverlayDismissed() {
                                            mIntroductoryOverlay = null;
                                        }
                                    })
                            .build();
                    mIntroductoryOverlay.show();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        mCastContext.addCastStateListener(mCastStateListener);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mCastContext.removeCastStateListener(mCastStateListener);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("CHANNEL_LIST", mAdapter.getChannelList());
    }

    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        private final ArrayList<AbstractChannel> channelList = new ArrayList<>();

        public ImageAdapter() {
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

        private ArrayList<AbstractChannel> getChannelList() {
            return this.channelList;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            LinearLayout v = (LinearLayout) LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.live_list_item, parent, false);

            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final AbstractChannel chan = channelList.get(position);

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

            holder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = mRecyclerView.getChildAdapterPosition(v);
                    Intent intent = new Intent(getBaseContext(), PlayStreamActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(CHANNEL_DATA, chan);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

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
            if (channelList == null) {
                return 0;
            }
            return channelList.size();
        }

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout mLinearLayout;
            TextView channelName, channelDescription;
            ImageView channelArt;

            public ViewHolder(LinearLayout v) {
                super(v);
                mLinearLayout = v;
                channelName = (TextView) v.findViewById(R.id.live_item_name);
                channelDescription = (TextView) v.findViewById(R.id.live_item_description);
                channelArt = (ImageView) v.findViewById(R.id.live_item_art);
            }
        }
    }
}
