package com.aggarwalankush.interview2go;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class QuestionActivity extends AppCompatActivity implements QuestionActivityFragment.Callback {
    private final String LOG_TAG = QuestionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(QuestionActivityFragment.QUESTION_URI, getIntent().getData());

            QuestionActivityFragment questionActivityFragment = new QuestionActivityFragment();
            questionActivityFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.question_container, questionActivityFragment)
                    .commit();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onItemSelected(Uri questionUri) {
        Log.d(LOG_TAG, "item clicked " + questionUri);
    }
}
