package com.tinnvec.dctvandroid;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import de.psdev.licensesdialog.LicensesDialog;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        TextView versionNameView = (TextView) findViewById(R.id.version_name);
        String version = BuildConfig.VERSION_NAME;
        String versionName = "Build version: " + version;
        versionNameView.setText(versionName);
    }

    public void onLicencesClick(final View view) {
        new LicensesDialog.Builder(this)
                .setNotices(R.raw.notices)
                .build()
                .show();
    }

    public void onGithubClick(View view) {
        String url = "https://github.com/chatrealm/DctvAndroid";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
