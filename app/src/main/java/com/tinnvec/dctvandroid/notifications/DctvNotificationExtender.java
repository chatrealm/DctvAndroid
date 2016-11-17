package com.tinnvec.dctvandroid.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

/**
 * Created by kev on 11/16/16.
 */

public class DctvNotificationExtender extends NotificationExtenderService {

        @Override
        protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
            Context ctx = getApplicationContext();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

            boolean shouldNotify = prefs.getBoolean("pref_notify", true);

            // Return true to stop the notification from displaying.
            return !shouldNotify;
        }

}

