package com.tinnvec.dctvandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;

import java.util.Properties;


public class JustChatActivity extends AppCompatActivity {

    private MenuItem mediaRouteMenuItem;
    private CastContext mCastContext;
    private Properties appConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PropertyReader pReader = new PropertyReader(this);
        appConfig = pReader.getMyProperties("app.properties");

        setContentView(R.layout.activity_just_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitle("Chatrealm");

        mCastContext = CastContext.getSharedInstance(this); // initialises castcontext

        WebView chatWebview = (WebView) findViewById(R.id.chat_webview);
        WebSettings settings = chatWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        chatWebview.loadUrl(appConfig.getProperty("irc.web_url"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_just_chat, menu);

        // add media router button for cast
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);

        return true;
    }

}
