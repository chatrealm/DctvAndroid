package com.tinnvec.dctvandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;


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

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.product_logo_header)
                .build();

        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(1).withName(R.string.live_video).withIcon(R.drawable.ic_live_video),
                        new PrimaryDrawerItem().withIdentifier(2).withName(R.string.live_audio).withIcon(R.drawable.ic_live_radio),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(3).withName(R.string.chat_activity).withIcon(R.drawable.ic_chatrealm_bubble_white_24px),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(4).withName(R.string.action_settings).withIcon(R.drawable.settings_white),
                        new PrimaryDrawerItem().withIdentifier(5).withName(R.string.about).withIcon(R.drawable.ic_about)

                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            Intent intent = null;
                            if (drawerItem.getIdentifier() == 1) {
                                intent = new Intent(JustChatActivity.this, LiveChannelsActivity.class);
                            } else if (drawerItem.getIdentifier() == 2) {
                                intent = new Intent(JustChatActivity.this, RadioChannelsActivity.class);
                            } else if (drawerItem.getIdentifier() == 3) {
                                intent = new Intent(JustChatActivity.this, JustChatActivity.class);
                            } else if (drawerItem.getIdentifier() == 4) {
                                intent = new Intent(JustChatActivity.this, SettingsActivity.class);
                            } else if (drawerItem.getIdentifier() == 5) {
                                intent = new Intent(JustChatActivity.this, AboutActivity.class);
                            }
                            if (intent != null) {
                                JustChatActivity.this.startActivity(intent);
                                JustChatActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .build();
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 1
            result.setSelection(3, false);
        }

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
        }
        return true;
    }
}
