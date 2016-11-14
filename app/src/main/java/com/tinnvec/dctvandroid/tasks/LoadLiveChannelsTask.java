package com.tinnvec.dctvandroid.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;

import com.tinnvec.dctvandroid.ChannelListAdapter;
import com.tinnvec.dctvandroid.channel.*;
import com.tinnvec.dctvandroid.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by kev on 5/22/16.
 */
public class LoadLiveChannelsTask extends AsyncTask<Void,Void,List<AbstractChannel>> {

    private static String TAG = LoadLiveChannelsTask.class.getName();

    private final RecyclerView mRecyclerView;
    private final Context context;
    private final String dctvChannelsUrl;
    private final Properties appConfig;

    public LoadLiveChannelsTask(RecyclerView mRecyclerView, Properties app_config) {
        this.mRecyclerView = mRecyclerView;
        this.appConfig = app_config;
        this.context = mRecyclerView.getContext();
        this.dctvChannelsUrl = app_config.getProperty("api.dctv.channels_url");

    }

    @Override
    protected List<AbstractChannel> doInBackground(Void... voids) {
        return fetchLiveChannels();
    }


    @Override
    protected void onPostExecute(List<AbstractChannel> result) {
        ChannelListAdapter adapter = (ChannelListAdapter) mRecyclerView.getAdapter();
        adapter.clear();
        if (result != null && !result.isEmpty()) {
            adapter.addAll(result);
        }
    }

    private List<AbstractChannel> fetchLiveChannels() {
        URL url;
        HttpURLConnection urlConnection;
        InputStream in;
        List<AbstractChannel> liveChannels = new ArrayList<>();
        liveChannels.add(DctvChannel.get247Channel(appConfig));
        try {
            url = new URL(dctvChannelsUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());
            liveChannels.addAll(readDctvApi(in));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return liveChannels;
    }

    private List<AbstractChannel> readDctvApi(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        List<AbstractChannel> channels = new ArrayList<>();
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

    private List<AbstractChannel> readDctvObject(JsonReader reader) throws IOException {
        List<AbstractChannel> channels = new ArrayList<>();
        if (!reader.nextName().isEmpty()) {
            reader.beginArray();
            while (reader.hasNext()) {
                channels.addAll(readDctvChannelsArray(reader));
            }
            reader.endArray();
        }
        return channels;
    }

    private List<AbstractChannel> readDctvChannelsArray(JsonReader reader) throws IOException {
        List<AbstractChannel> channels = new ArrayList<>();
        reader.beginObject();
        while (reader.hasNext()) {
            channels.add(readDctvChannel(reader));
        }
        reader.endObject();
        return channels;
    }

    private AbstractChannel readDctvChannel(JsonReader reader) throws IOException {
        ChannelReader channelReader = new ChannelReader();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "streamid":
                    channelReader.setStreamid(reader.nextInt());
                    break;
                case "channelname":
                    channelReader.setChannelname(reader.nextString());
                    break;
                case "friendlyalias":
                    channelReader.setFriendlyalias(reader.nextString());
                    break;
                case "streamtype":
                    channelReader.setStreamtype(reader.nextString());
                    break;
                case "nowonline":
                    channelReader.setNowonline(reader.nextString());
                    break;
                case "alerts":
                    channelReader.setAlerts(reader.nextBoolean());
                    break;
                case "twitch_currentgame":
                    channelReader.setTwitch_currentgame(reader.nextString());
                    break;
                case "twitch_yt_description":
                    channelReader.setTwitch_yt_description(reader.nextString());
                    break;
                case "yt_upcoming":
                    channelReader.setYt_upcoming(reader.nextBoolean());
                    break;
                case "yt_liveurl":
                    channelReader.setYt_liveurl(reader.nextString());
                    break;
                case "imageasset":
                    channelReader.setImageasset(reader.nextString());
                    break;
                case "imageassethd":
                    channelReader.setImageassethd(reader.nextString());
                    break;
                case "urltoplayer":
                    channelReader.setUrltoplayer(reader.nextString());
                    break;
                case "channel":
                    channelReader.setChannel(reader.nextInt());
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        return channelReader.getChannelInstance();
    }

}
