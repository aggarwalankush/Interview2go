package com.aggarwalankush.interview2go;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;
import com.aggarwalankush.interview2go.sync.InterviewSyncAdapter;

public class MainActivity extends AppCompatActivity implements TopicFragment.Callback, NavigationView.OnNavigationItemSelectedListener {
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        InterviewSyncAdapter.initializeSyncAdapter(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        Utility.setActivityType(this, Utility.HOME);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onItemSelected(Uri topicUri) {
        Log.d(LOG_TAG, "topic clicked " + topicUri);
        Intent intent = new Intent(this, QuestionActivity.class).setData(topicUri);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {

            Utility.setActivityType(this, Utility.HOME);

        } else if (id == R.id.nav_done) {

            Utility.setActivityType(this, Utility.DONE);

        } else if (id == R.id.nav_bookmark) {

            Utility.setActivityType(this, Utility.BOOKMARK);

        } else if (id == R.id.nav_settings) {

            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_reset) {

            new Builder(this)
                    .setTitle(getString(R.string.reset_alert_title))
                    .setMessage(getString(R.string.reset_alert_message))
                    .setPositiveButton(getString(R.string.reset_alert_positive_label), new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(InterviewEntry.COLUMN_DONE, 0);
                            contentValues.put(InterviewEntry.COLUMN_BOOKMARK, 0);
                            MainActivity.this.getContentResolver().update(
                                    InterviewEntry.CONTENT_URI,
                                    contentValues,
                                    null,
                                    null
                            );
                        }
                    })
                    .setNegativeButton(getString(R.string.reset_alert_negative_label), new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(R.drawable.ic_alert)
                    .show();

        } else if (id == R.id.nav_rate_app) {
            RateActivity.showRateAppDialog(MainActivity.this);
        }

        Utility.changeActivityColor(this, toolbar, null);
        Utility.changeActivityName(this, getSupportActionBar());

        TopicFragment topicFragment = (TopicFragment) getSupportFragmentManager().findFragmentById(R.id.topic_fragment);

        if (null != topicFragment) {
            topicFragment.onActivityTypeChanged();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
