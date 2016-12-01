package com.tinnvec.dctvandroid.tasks;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;

import com.tinnvec.dctvandroid.R;
import com.tinnvec.dctvandroid.adapters.RadioListAdapter;
import com.tinnvec.dctvandroid.channel.RadioChannel;
import com.tinnvec.dctvandroid.channel.RadioChannelReader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by kev on 5/22/16.
 */
public class LoadRadioChannelsTask extends AsyncTask<Void, Void, List<RadioChannel>> {

    private static String TAG = LoadRadioChannelsTask.class.getName();

    private final RecyclerView mRecyclerView;
    private final Context context;
    private final String dctvChannelsUrl;
    private final Properties appConfig;

    public LoadRadioChannelsTask(RecyclerView mRecyclerView, Properties app_config) {
        this.mRecyclerView = mRecyclerView;
        this.appConfig = app_config;
        this.context = mRecyclerView.getContext();
        this.dctvChannelsUrl = app_config.getProperty("api.dctv.channels_url");

    }

    @Override
    protected List<RadioChannel> doInBackground(Void... voids) {
        return fetchRadioChannels();
    }


    @Override
    protected void onPostExecute(List<RadioChannel> result) {
        RadioListAdapter adapter = (RadioListAdapter) mRecyclerView.getAdapter();
        adapter.clear();
        if (result != null && !result.isEmpty()) {
            adapter.addAll(result);
        }
    }


    private List<RadioChannel> fetchRadioChannels() {
        InputStream is = context.getResources().openRawResource(R.raw.radio);

/*        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String jsonString = writer.toString();
*/
        List<RadioChannel> liveChannels = new ArrayList<>();
        try {
            is = new BufferedInputStream(is);
            liveChannels.addAll(readRadioJson(is));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return liveChannels;
    }

    private List<RadioChannel> readRadioJson(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        List<RadioChannel> channels = new ArrayList<>();
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                channels.addAll(readRadioObject(reader));
            }
            reader.endObject();
            return channels;
        } finally {
            reader.close();
        }
    }

    private List<RadioChannel> readRadioObject(JsonReader reader) throws IOException {
        List<RadioChannel> channels = new ArrayList<>();
        if (!reader.nextName().isEmpty()) {
            reader.beginArray();
            while (reader.hasNext()) {
                channels.addAll(readRadioChannelsArray(reader));
            }
            reader.endArray();
        }
        return channels;
    }

    private List<RadioChannel> readRadioChannelsArray(JsonReader reader) throws IOException {
        List<RadioChannel> channels = new ArrayList<>();
        reader.beginObject();
        while (reader.hasNext()) {
            channels.add(readRadioChannel(reader));
        }
        reader.endObject();
        return channels;
    }

    private RadioChannel readRadioChannel(JsonReader reader) throws IOException {
        RadioChannelReader channelReader = new RadioChannelReader();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "streamid":
                    channelReader.setStreamid(reader.nextInt());
                    break;
                case "channelname":
                    channelReader.setChannelname(reader.nextString());
                    break;
                case "streamurl":
                    channelReader.setStreamUrl(reader.nextString());
                    break;
                case "friendlyalias":
                    channelReader.setFriendlyalias(reader.nextString());
                    break;
                case "streamtype":
                    channelReader.setStreamtype(reader.nextString());
                    break;
                case "description":
                    channelReader.setDescription(reader.nextString());
                    break;
                case "imageasset":
                    channelReader.setImageasset(reader.nextString());
                    break;
                case "imageassethd":
                    channelReader.setImageassethd(reader.nextString());
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
