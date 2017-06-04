package com.tinnvec.dctvandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.tinnvec.dctvandroid.tasks.TwitchAPIUsernameTask;

import java.util.Properties;
import java.util.concurrent.ExecutionException;


public class JustChatActivity extends AppCompatActivity {

    private MenuItem mediaRouteMenuItem;
    private CastContext mCastContext;
    private ChatLoginFragment chatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        String username = null;
        if (uri != null && !uri.toString().contains("error")) {
            String[] parts = uri.toString().split("#access_token=");
            String tokenAndScope = parts[1];
            String[] smallerParts = tokenAndScope.split("&scope=");
            String token = smallerParts[0];
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("TWITCH_TOKEN", token).apply();

            Properties appConfig = ((DctvApplication) getApplication()).getAppConfig();
            String apiUrl = appConfig.getProperty("api.twitch.api_url");
            String clientId = appConfig.getProperty("api.twitch.client_id");


            try {
                username = new TwitchAPIUsernameTask(apiUrl, clientId, token).execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (username != null) {
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("TWITCH_USERNAME", username).apply();
            Toast.makeText(this, "Twitch Authorization successful for " + username, Toast.LENGTH_LONG).show();
            Intent refresh = new Intent(this, JustChatActivity.class);
            startActivity(refresh);
            this.finish();
        }


        setContentView(R.layout.activity_just_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
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
                .withCloseOnClick(false)
                .build();
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 1
            result.setSelection(3, false);
        }

        mCastContext = CastContext.getSharedInstance(this); // initialises castcontext

        chatFragment = new ChatLoginFragment();

        Bundle bundle = new Bundle();
        bundle.putString("streamService", "none");
        bundle.putString("channelName", "none");
        chatFragment.setArguments(bundle);

        getSupportFragmentManager()
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
