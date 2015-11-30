package com.aggarwalankush.interview2go;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;
import com.aggarwalankush.interview2go.sync.InterviewSyncAdapter;

public class MainActivity extends AppCompatActivity implements TopicFragment.Callback, NavigationView.OnNavigationItemSelectedListener {
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.title_activity_topic);

        InterviewSyncAdapter.initializeSyncAdapter(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_done) {

        } else if (id == R.id.nav_bookmark) {

        } else if (id == R.id.nav_settings) {

            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_reset) {


            new AlertDialog.Builder(this)
                    .setTitle("Undo Done and Delete Bookmarks")
                    .setMessage("Are you sure you want to reset all questions?")
                    .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
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
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(R.drawable.ic_alert)
                    .show();

        } else if (id == R.id.nav_rate_app) {
            RateActivity.showRateAppDialog(MainActivity.this);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
