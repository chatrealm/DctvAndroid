package com.tinnvec.dctvandroid;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.tinnvec.dctvandroid.R.string.chat;


public class JustChatActivity extends AppCompatActivity {

    private MenuItem mediaRouteMenuItem;
    private CastContext mCastContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_just_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitle("Chatrealm");

        mCastContext = CastContext.getSharedInstance(this); // initialises castcontext

        ChatFragment chatFragment = new ChatFragment();

        Bundle bundle = new Bundle();
        bundle.putString("streamService", "none");
        bundle.putString("channelName", "none");
        chatFragment.setArguments(bundle);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.chat_fragment, chatFragment)
                .commit();


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
