package com.aggarwalankush.interview2go;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.aggarwalankush.interview2go.data.InterviewContract;
import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;

public class SearchableActivity extends AppCompatActivity {
    private static final String LOG_TAG = SearchableActivity.class.getSimpleName();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void doMySearch(String query) {

        Log.d(LOG_TAG, query);


        Uri uri = InterviewContract.BASE_CONTENT_URI.buildUpon()
                .appendPath("search")
                .appendPath(InterviewContract.PATH_INTERVIEW)
                .appendPath(query)
                .build();

        Cursor cursor = getContentResolver().query(uri, null, null, new String[]{query}, null);
        if (null != cursor) {
            while (cursor.moveToNext()) {
                String questionName = cursor.getString(2);
                if (questionName.equalsIgnoreCase(query)) {
                    Intent intent = new Intent(this, SolutionActivity.class);
                    intent.setData(InterviewEntry.buildTopicWithQuestion(cursor.getString(1), questionName));
                    intent.putExtra("search", 1);
                    startActivity(intent);
                    return;
                }
            }
        }

        Intent h = new Intent(SearchableActivity.this, MainActivity.class);
        h.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(h);
    }
}
