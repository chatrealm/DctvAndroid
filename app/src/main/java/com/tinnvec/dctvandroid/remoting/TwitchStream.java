package com.tinnvec.dctvandroid.remoting;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.impl.client.HttpClients;


/**
 * Created by kev on 11/13/16.
 */

public class TwitchStream {

    public static class TokenInfo {
        public String token;
        public String sig;
    }

    private String clientId;
    private String apiUrl;

    public TwitchStream(String api_url, String client_id) {
        this.apiUrl = api_url;
        this.clientId = client_id;
    }

    TokenInfo getTokenInfo(String channel_name) throws Exception {
        String fullUrl = String.format(apiUrl, channel_name);
        CloseableHttpClient client = HttpClients.createDefault();
        TokenInfo info = new TokenInfo();

        try {
            HttpGet get = new HttpGet(fullUrl);
            get.setHeader("Accept", "application/vnd.twitchtv.v3+json");
            get.setHeader("Client-Id", clientId);

            CloseableHttpResponse response = client.execute(get);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 200) {
                throw new IOException("request failed: " + status.getStatusCode() + ", " + status.getReasonPhrase());
            }

            InputStream in = response.getEntity().getContent();
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "token":
                        info.token = reader.nextString();
                        break;
                    case "sig":
                        info.sig = reader.nextString();
                        break;

                }
            }
            reader.endObject();
            reader.close();
        } finally {
            client.close();
        }
        return info;
    }

}
