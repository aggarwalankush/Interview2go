package com.aggarwalankush.interview2go;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.aggarwalankush.interview2go.sync.InterviewSyncAdapter;

public class MainActivity extends AppCompatActivity implements TopicFragment.Callback {
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        InterviewSyncAdapter.initializeSyncAdapter(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        TextView tv = (TextView) findViewById(R.id.textview);
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            InterviewSyncAdapter.syncImmediately(this);
//
//            Cursor c = getContentResolver().query(InterviewEntry.CONTENT_URI, null, null, null, null);
//            if(c!=null){
////                Log.d("hello", DatabaseUtils.dumpCursorToString(c));
//                c.moveToFirst();
//                c.moveToNext();
//               tv.setText(c.getString(0) +"\n"+c.getString(1) +"\n"+c.getString(2) +"\n"+c.getString(3) +"\n"+c.getString(4) +"\n");
//
//            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri topicUri) {
        Log.d(LOG_TAG, "item clicked " + topicUri);
    }
}
