package com.aggarwalankush.interview2go;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;
import com.aggarwalankush.interview2go.sync.InterviewSyncAdapter;

public class MainActivity extends AppCompatActivity implements TopicFragment.Callback, NavigationView.OnNavigationItemSelectedListener {
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    Toolbar toolbar;
    AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.navigation_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);

//        InterviewSyncAdapter.initializeSyncAdapter(this);

//        Log.d(LOG_TAG, new InterviewSyncAdapter(this, true).isEmptyDB() + "");

        if (new InterviewSyncAdapter(this, true).isEmptyDB()) {
            InterviewSyncAdapter.syncLocally(this);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        Utility.setActivityType(this, Utility.HOME);
        Utility.changeActivityName(this, getSupportActionBar());


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    Intent i = new Intent(MainActivity.this, IntroActivity.class);
                    startActivity(i);

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();
                }
            }
        });

        // Start the thread
        t.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
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
//        Log.d(LOG_TAG, "topic clicked " + topicUri);
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
        } else if (id == R.id.nav_overview) {


            final Dialog dialog = new Dialog(this);
            dialog.getWindow();
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.overview_layout);

            TextView tv_total = (TextView) dialog.findViewById(R.id.tv_dialog_total);
            TextView tv_bookmark = (TextView) dialog.findViewById(R.id.tv_dialog_bookmark);
            TextView tv_done = (TextView) dialog.findViewById(R.id.tv_dialog_done);

            tv_total.setText(R.string.overview_total_questions);
            tv_done.setText(Utility.getQuestionsCount(this, InterviewEntry.COLUMN_DONE));
            tv_bookmark.setText(Utility.getQuestionsCount(this, InterviewEntry.COLUMN_BOOKMARK));
            dialog.show();


        }

        TopicFragment topicFragment = (TopicFragment) getSupportFragmentManager().findFragmentById(R.id.topic_fragment);

        if (null != topicFragment) {
            topicFragment.onActivityTypeChanged();
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        Utility.changeActivityColor(this, toolbar, null, drawer);
        Utility.changeActivityName(this, getSupportActionBar());
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            appBarLayout.setPadding(0, getStatusBarHeight(), 0, 0);
        }
        return true;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
