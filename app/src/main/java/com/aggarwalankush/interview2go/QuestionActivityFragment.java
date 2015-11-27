package com.aggarwalankush.interview2go;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aggarwalankush.interview2go.QuestionAdapter.QuestionAdapterOnClickHandler;
import com.aggarwalankush.interview2go.QuestionAdapter.QuestionAdapterViewHolder;
import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;

public class QuestionActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = QuestionActivityFragment.class.getSimpleName();

    static final String TOPIC_URI = "URI";
    private static Uri mUri;

    private QuestionAdapter mQuestionAdapter;

    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private static final int QUESTION_LOADER = 0;

    private static final String[] QUESTION_COLUMNS = {
            InterviewEntry._ID,
            InterviewEntry.COLUMN_QUESTION
    };

    public static final int COL_ID = 0;
    public static final int COL_QUESTION = 1;


    public interface Callback {
        void onItemSelected(Uri questionUri);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
//        Log.d(LOG_TAG, "onActivityCreated");
        getLoaderManager().initLoader(QUESTION_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Log.d(LOG_TAG, "onCreateView");

        Bundle arguments = getArguments();
//        Log.d(LOG_TAG, arguments.toString());
        if (arguments != null) {
            Uri oldUri = mUri;
            mUri = arguments.getParcelable(QuestionActivityFragment.TOPIC_URI);
            if (mUri == null) {
                mUri = oldUri;
            }
        }

        View rootView = inflater.inflate(R.layout.fragment_question, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_question);

        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        View emptyView = rootView.findViewById(R.id.recyclerview_question_empty);
        mRecyclerView.setHasFixedSize(true);

        mQuestionAdapter = new QuestionAdapter(getActivity(), new QuestionAdapterOnClickHandler() {
            @Override
            public void onClick(String question, QuestionAdapterViewHolder vh) {
                String topic = InterviewEntry.getTopicFromUri(mUri);
                ((Callback) getActivity()).onItemSelected(InterviewEntry.buildTopicWithQuestion(topic, question));
                mPosition = vh.getAdapterPosition();
            }
        }, emptyView);

        mRecyclerView.setAdapter(mQuestionAdapter);


        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
//        Log.d(LOG_TAG, "onSaveInstanceState");

        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        Log.d(LOG_TAG, "onCreateLoader");

        if (mUri != null) {
            // Sort order:  Ascending, by topic name.
            String sortOrder = InterviewEntry.COLUMN_QUESTION + " ASC";
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    QUESTION_COLUMNS,
                    null,
                    null,
                    sortOrder);
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//        Log.d(LOG_TAG, "onLoadFinished");

        mQuestionAdapter.swapCursor(cursor);
        if (mPosition != RecyclerView.NO_POSITION) {
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
        updateEmptyView();
    }

    private void updateEmptyView() {
//        Log.d(LOG_TAG, "updateEmptyView");

        if (mQuestionAdapter.getItemCount() == 0) {
            TextView tv = (TextView) getView().findViewById(R.id.recyclerview_question_empty);
            if (null != tv && !Utility.isNetworkAvailable(getActivity())) {
                tv.setText(R.string.empty_list_no_network);
            }
        }
    }


    @Override
    public void onDestroy() {
//        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
        if (null != mRecyclerView) {
            mRecyclerView.clearOnScrollListeners();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//        Log.d(LOG_TAG, "onLoaderReset");
        mQuestionAdapter.swapCursor(null);
    }

}