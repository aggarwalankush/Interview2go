package com.aggarwalankush.interview2go;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;

public class QuestionActivity extends AppCompatActivity implements QuestionActivityFragment.Callback {
    private final String LOG_TAG = QuestionActivity.class.getSimpleName();
    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(QuestionActivityFragment.TOPIC_URI, getIntent().getData());
            QuestionActivityFragment questionActivityFragment = new QuestionActivityFragment();
            questionActivityFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.question_container, questionActivityFragment)
                    .commit();
        }

        String topic = Utility.getDisplayTopicName(InterviewEntry.getTopicFromUri(getIntent().getData()));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(topic);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onItemSelected(Uri questionUri) {
        Log.d(LOG_TAG, "question clicked " + questionUri);
        Intent intent = new Intent(this, SolutionActivity.class).setData(questionUri);
        startActivity(intent);
    }
}
