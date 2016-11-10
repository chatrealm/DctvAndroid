package com.tinnvec.dctvandroid;

import android.app.Application;

import com.onesignal.OneSignal;

/**
 * Created by kev on 11/7/16.
 */

public class DctvApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.startInit(this).init();
    }

}
