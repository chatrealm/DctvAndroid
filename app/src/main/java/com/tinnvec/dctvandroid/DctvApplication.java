package com.tinnvec.dctvandroid;

import android.app.Application;

import com.onesignal.OneSignal;
import com.tinnvec.dctvandroid.notifications.DctvNotificationOpenedHandler;

import java.util.Properties;

import io.vov.vitamio.Vitamio;


/**
 * Created by kev on 11/7/16.
 */

public class DctvApplication extends Application {

    private Properties appConfig;

    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.NONE);
        OneSignal.Builder ob = OneSignal.startInit(this);
        ob.setNotificationOpenedHandler(new DctvNotificationOpenedHandler());
        ob.init();

        Vitamio.isInitialized(this);

        // load appconfig
        PropertyReader pReader = new PropertyReader(this);
        appConfig = pReader.getMyProperties("app.properties");
    }

    public Properties getAppConfig() {
        return appConfig;
    }

}
