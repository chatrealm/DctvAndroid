package com.tinnvec.dctvandroid.remoting;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by kev on 11/13/16.
 */

@RunWith(AndroidJUnit4.class)
public class TwitchStreamTest {

    @Test
    public void testGetTokenInfo() throws Exception {
        TwitchStream ts = new TwitchStream("https://api.twitch.tv/api/channels/%s/access_token", "ihky2eak062lzbfq1ktfhim3v08vy1b");

        TwitchStream.TokenInfo info = ts.getTokenInfo("omgchad");

        System.out.println(info);

    }
}
