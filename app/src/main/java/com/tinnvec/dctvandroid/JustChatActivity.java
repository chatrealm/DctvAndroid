package com.tinnvec.dctvandroid;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;


public class JustChatActivity extends AppCompatActivity {

    private MenuItem mediaRouteMenuItem;
    private CastContext mCastContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_just_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.navigate_back:
                WebView chatWebview = (WebView) findViewById(R.id.chat_webview);
                chatWebview.goBack();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }
}
