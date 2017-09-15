package com.tinnvec.dctvandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.tinnvec.dctvandroid.adapters.RadioListAdapter;
import com.tinnvec.dctvandroid.adapters.RadioListCallback;
import com.tinnvec.dctvandroid.channel.RadioChannel;

import java.util.Properties;

import static android.support.v7.media.MediaControlIntent.ACTION_PLAY;

public class RadioChannelsActivity extends AppCompatActivity implements RadioListCallback {

    public static final String CHANNEL_DATA = "com.tinnvec.dctv_android.CHANNEL_MESSAGE";
    private static final String TAG = RadioChannelsActivity.class.getName();
    private Properties appConfig;
    private RecyclerView mRecyclerView;
    private RadioListAdapter mAdapter;
    private SwipeRefreshLayout swipeContainer;
    private SwipeRefreshLayout.OnRefreshListener swipeRefreshListener;

    // added for cast SDK v3
    private CastContext mCastContext;
    private MenuItem mediaRouteMenuItem;
    private IntroductoryOverlay mIntroductoryOverlay;
    private CastStateListener mCastStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        appConfig = ((DctvApplication) getApplication()).getAppConfig();
        setContentView(R.layout.activity_live_channels);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.product_logo_header)
                .build();


//create the drawer and remember the `Drawer` result object
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
                                intent = new Intent(RadioChannelsActivity.this, LiveChannelsActivity.class);
                            } else if (drawerItem.getIdentifier() == 2) {
                                intent = new Intent(RadioChannelsActivity.this, RadioChannelsActivity.class);
                            } else if (drawerItem.getIdentifier() == 3) {
                                intent = new Intent(RadioChannelsActivity.this, JustChatActivity.class);
                            } else if (drawerItem.getIdentifier() == 4) {
                                intent = new Intent(RadioChannelsActivity.this, SettingsActivity.class);
                            } else if (drawerItem.getIdentifier() == 5) {
                                intent = new Intent(RadioChannelsActivity.this, AboutActivity.class);
                            }
                            if (intent != null) {
                                RadioChannelsActivity.this.startActivity(intent);
                                RadioChannelsActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
            result.setSelection(2, false);
        }

        mCastStateListener = new CastStateListener() {
            @Override
            public void onCastStateChanged(int newState) {
                if (newState != CastState.NO_DEVICES_AVAILABLE) {
                    showIntroductoryOverlay();
                }
            }
        };

        mCastContext = CastContext.getSharedInstance(this); // initialises castcontext

/*        mRecyclerView = (RecyclerView) findViewById(R.id.live_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getBaseContext(), null));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new RadioListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new LoadRadioChannelsTask(mRecyclerView, appConfig) {

                    @Override
                    protected void onPostExecute(List<RadioChannel> result) {
                        super.onPostExecute(result);
                        swipeContainer.setRefreshing(false);
                    }
                }.execute();
            }
        };
        swipeContainer.setColorSchemeColors(getResources().getColor(R.color.colorAccentDark));
        swipeContainer.setOnRefreshListener(swipeRefreshListener);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ArrayList<RadioChannel> savedChannels = null;
        if (savedInstanceState != null) {
            savedChannels = savedInstanceState.getParcelableArrayList("CHANNEL_LIST");
        }
        if (savedChannels != null) {
            mAdapter.addAll(savedChannels);
        } else {
            new LoadRadioChannelsTask(mRecyclerView, appConfig).execute();
        }*/

        Button dcNoticeButton = (Button) findViewById(R.id.dc_notice_button);

        dcNoticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://diamondclub.tv/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_live_channels, menu);

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

/*        if (id == R.id.refresh) {
            swipeContainer.post(new Runnable() {
                @Override
                public void run() {
                    swipeContainer.setRefreshing(true);
                    // directly call onRefresh() method
                    swipeRefreshListener.onRefresh();
                }
            });
        }*/

        return super.onOptionsItemSelected(item);
    }

    private void showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay.remove();
        }
        if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mIntroductoryOverlay = new IntroductoryOverlay.Builder(
                            RadioChannelsActivity.this, mediaRouteMenuItem)
                            .setTitleText(getString(R.string.cast_introduction))
                            .setSingleTime()
                            .setOnOverlayDismissedListener(
                                    new IntroductoryOverlay.OnOverlayDismissedListener() {
                                        @Override
                                        public void onOverlayDismissed() {
                                            mIntroductoryOverlay = null;
                                        }
                                    })
                            .build();
                    mIntroductoryOverlay.show();
                }
            });
        }
    }

    @Override
    public void onChannelClicked(RadioChannel channel) {
        Intent intent = new Intent(this, RadioPlayerService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(CHANNEL_DATA, channel);
        intent.putExtras(bundle);
        intent.setAction(ACTION_PLAY);
        startService(intent);
        String text = String.format(getString(R.string.loading_radio), channel.getFriendlyAlias());
        Snackbar.make(findViewById(R.id.root_coordinator_layout), text, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    protected void onResume() {
        mCastContext.addCastStateListener(mCastStateListener);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mCastContext.removeCastStateListener(mCastStateListener);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putParcelableArrayList(
//                "CHANNEL_LIST", (ArrayList<RadioChannel>) mAdapter.getChannelList());
    }
}
