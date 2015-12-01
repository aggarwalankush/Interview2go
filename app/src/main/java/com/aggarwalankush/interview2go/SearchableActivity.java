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
            actionSearchHelper(query);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // Handle a suggestions click (because the suggestions all use ACTION_VIEW)
            Uri data = intent.getData();
            Log.d(LOG_TAG, "Search Intent uri recieved " + data.toString());
            String uriString = data.toString();
            if (!uriString.startsWith("content")) {
                uriString = "content://com.aggarwalankush.interview2go.provider/interview/";
                uriString += data;
            }
            actionViewHelper(Uri.parse(uriString));
        }
    }


    private void actionViewHelper(Uri rowNoUri) {

        Cursor cursor = getContentResolver().query(
                rowNoUri,
                new String[]{InterviewEntry.COLUMN_TOPIC, InterviewEntry.COLUMN_QUESTION},
                null,
                null,
                null);

        if (null != cursor && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String topic = cursor.getString(0);
            String question = cursor.getString(1);
            Intent intent = new Intent(this, SolutionActivity.class);
            intent.setData(InterviewEntry.buildTopicWithQuestion(topic, question));
            intent.putExtra("search", 1);
            startActivity(intent);
            cursor.close();
            return;
        }

        Intent h = new Intent(SearchableActivity.this, MainActivity.class);
        h.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(h);
    }


    private void actionSearchHelper(String query) {
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
                    cursor.close();
                    return;
                }
            }
        }

        Intent h = new Intent(SearchableActivity.this, MainActivity.class);
        h.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(h);
    }
}
