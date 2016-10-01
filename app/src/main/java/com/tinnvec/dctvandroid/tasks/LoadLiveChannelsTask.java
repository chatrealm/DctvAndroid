package com.tinnvec.dctvandroid.tasks;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.Log;

import com.tinnvec.dctvandroid.DctvChannel;
import com.tinnvec.dctvandroid.LiveChannelsActivity;
import com.tinnvec.dctvandroid.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kev on 5/22/16.
 */
public class LoadLiveChannelsTask extends AsyncTask<Void,Void,List<DctvChannel>> {

    private static String TAG = LoadLiveChannelsTask.class.getName();

    private final RecyclerView mRecyclerView;
    private final String dctvBaseUrl;

    public LoadLiveChannelsTask(RecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;
        this.dctvBaseUrl = mRecyclerView.getContext().getString(R.string.dctv_base_url);

    }

    @Override
    protected List<DctvChannel> doInBackground(Void... voids) {
        return fetchLiveChannels();
    }


    @Override
    protected void onPostExecute(List<DctvChannel> result) {
        LiveChannelsActivity.ImageAdapter adapter = (LiveChannelsActivity.ImageAdapter )mRecyclerView.getAdapter();
        adapter.clear();
        if (result != null && !result.isEmpty()) {
            adapter.addAll(result);
        }
    }

    private List<DctvChannel> fetchLiveChannels() {
        URL url;
        HttpURLConnection urlConnection;
        InputStream in;
        List<DctvChannel> liveChannels = null;
        try {
            String channelsURL = String.format("%sapi/channelsv2.php",  dctvBaseUrl);
            url = new URL(channelsURL);
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
                        Log.e(TAG, "problem loading image for channel ", e);
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

}
