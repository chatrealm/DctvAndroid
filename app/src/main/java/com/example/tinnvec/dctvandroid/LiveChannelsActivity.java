package com.example.tinnvec.dctvandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LiveChannelsActivity extends AppCompatActivity {
    public static final String CHANNEL_DATA = "com.tinnvec.dctv_android.CHANNEL_MESSAGE";
    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private SwipeRefreshLayout swipeContainer;

    private List<DctvChannel> fetchLiveChannels() {
        URL url;
        HttpURLConnection urlConnection;
        InputStream in;
        List<DctvChannel> liveChannels = null;
        try {
            String apiURL = "http://diamondclub.tv/api/channelsv2.php";
            url = new URL(apiURL);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());
            liveChannels = readDctvApi(in);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return liveChannels;
    }

    private List<DctvChannel> readDctvApi(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        List<DctvChannel> channels = new ArrayList<>();
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                channels.addAll(readDctvObject(reader));
            }
            reader.endObject();
            return channels;
        } finally {
            reader.close();
        }
    }

    private List<DctvChannel> readDctvObject(JsonReader reader) throws IOException {
        List<DctvChannel> channels = new ArrayList<>();
        if (!reader.nextName().isEmpty()) {
            reader.beginArray();
            while (reader.hasNext()) {
                channels.addAll(readDctvChannelsArray(reader));
            }
            reader.endArray();
        }
        return channels;
    }

    private List<DctvChannel> readDctvChannelsArray(JsonReader reader) throws IOException {
        List<DctvChannel> channels = new ArrayList<>();
        reader.beginObject();
        while (reader.hasNext()) {
            channels.add(readDctvChannel(reader));
        }
        reader.endObject();
        return channels;
    }

    private DctvChannel readDctvChannel(JsonReader reader) throws IOException {
        DctvChannel channel = new DctvChannel(null);
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "streamid":
                    channel.streamid = reader.nextInt();
                    break;
                case "channelname":
                    channel.channelname = reader.nextString();
                    break;
                case "friendlyalias":
                    channel.friendlyalias = reader.nextString();
                    break;
                case "streamtype":
                    channel.streamtype = reader.nextString();
                    break;
                case "nowonline":
                    channel.nowonline = reader.nextString();
                    break;
                case "alerts":
                    channel.alerts = reader.nextBoolean();
                    break;
                case "twitch_currentgame":
                    channel.twitch_currentgame = reader.nextString();
                    break;
                case "twitch_yt_description":
                    channel.twitch_yt_description = reader.nextString();
                    break;
                case "yt_upcoming":
                    channel.yt_upcoming = reader.nextBoolean();
                    break;
                case "yt_liveurl":
                    channel.yt_liveurl = reader.nextString();
                    break;
                case "imageasset":
                    channel.imageasset = reader.nextString();
                    try {
                        InputStream is = (InputStream) new URL(channel.imageasset).getContent();
                        Bitmap bitmap = ((BitmapDrawable) Drawable.createFromStream(is, "src name")).getBitmap();
                        channel.imageassetBitmap = Bitmap.createScaledBitmap(bitmap, 300, 120, true);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                case "imageassethd":
                    channel.imageassethd = reader.nextString();
                    break;
                case "urltoplayer":
                    channel.urltoplayer = reader.nextString();
                    break;
                case "channel":
                    channel.channel = reader.nextInt();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        return channel;
    }

    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        private List<DctvChannel> channelList;
        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public LinearLayout mLinearLayout;
            public ViewHolder(LinearLayout v) {
                super(v);
                mLinearLayout = v;
            }
        }

        public ImageAdapter(List<DctvChannel> channelList) {
            this.channelList = channelList;
        }

        // Clean all elements of the recycler
        public void clear() {
            channelList.clear();
            notifyDataSetChanged();
        }

        // Add a list of items
        public void addAll(List<DctvChannel> list) {
            channelList.addAll(list);
            notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            LinearLayout v = (LinearLayout) LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.live_list_item, parent, false);

            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ImageView channelArt = (ImageView) holder.mLinearLayout.findViewById(R.id.live_item_art);
            channelArt.setImageBitmap(channelList.get(position).imageassetBitmap);

            TextView channelName = (TextView) holder.mLinearLayout.findViewById(R.id.live_item_name);
            channelName.setText(channelList.get(position).friendlyalias);
            channelName.setSelected(true);

            TextView channelDescription = (TextView) holder.mLinearLayout.findViewById(R.id.live_item_description);
            if (channelList.get(position).twitch_yt_description.equals("")) {
                channelDescription.setVisibility(View.GONE);
            } else {
                channelDescription.setVisibility(View.VISIBLE);
                channelDescription.setText(channelList.get(position).twitch_yt_description);
                channelDescription.setSelected(true);
            }

            holder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = mRecyclerView.getChildAdapterPosition(v);
                    Intent intent = new Intent(getBaseContext(), PlayStreamActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(CHANNEL_DATA, channelList.get(position));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            if (channelList == null) {
                return 0;
            }
            return channelList.size();
        }
    }

    private class BuildMainLayout extends AsyncTask<String, String, List<DctvChannel>> {
        @Override
        protected List<DctvChannel> doInBackground(String... params) {
            return fetchLiveChannels();
        }

        protected void onPostExecute(List<DctvChannel> result) {
            mAdapter = new ImageAdapter(result);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_channels);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncTask<String, String, List<DctvChannel>>() {
                    @Override
                    protected List<DctvChannel> doInBackground(String... params) {
                        mAdapter.clear();
                        return fetchLiveChannels();
                    }

                    protected void onPostExecute(List<DctvChannel> result) {
                        mAdapter.addAll(result);
                        swipeContainer.setRefreshing(false);
                    }
                }.execute();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.live_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getBaseContext(), null));
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        new BuildMainLayout().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_live_channels, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
