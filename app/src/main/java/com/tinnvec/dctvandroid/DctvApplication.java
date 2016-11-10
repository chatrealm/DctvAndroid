package com.tinnvec.dctvandroid;

import android.app.Application;

import com.onesignal.OneSignal;
import com.tinnvec.dctvandroid.notifications.DctvNotificationOpenedHandler;


/**
 * Created by kev on 11/7/16.
 */

public class DctvApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.NONE);
        OneSignal.Builder ob = OneSignal.startInit(this);
        ob.setNotificationOpenedHandler(new DctvNotificationOpenedHandler());
        ob.init();
    }

}
