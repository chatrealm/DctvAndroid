package com.tinnvec.dctvandroid.tasks;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by robin on 15.11.16.
 */

public class ResolveStreamUrlTask  extends AsyncTask<String, Void, String> {

    private IOException exception;

    @Override
    protected String doInBackground(String... params) {
        try {
            return resolveStreamUrl(params[0]);
        } catch (IOException ex) {
            this.exception = ex;
            return null;
        }
    }

    private String resolveStreamUrl(String streamUrl) throws IOException {
        URL url = new URL(streamUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.connect();
            urlConnection.getHeaderFields();

            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Video ressource at " + streamUrl + " is not available");
            }

            return urlConnection.getURL().toString();
        } finally {
            urlConnection.disconnect();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if (this.exception != null) {
            // TODO: Handle exception properly
            exception.printStackTrace();
        }
    }
}
