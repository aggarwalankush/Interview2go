package com.aggarwalankush.interview2go;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.aggarwalankush.interview2go.TopicAdapter.TopicAdapterOnClickHandler;
import com.aggarwalankush.interview2go.TopicAdapter.TopicAdapterViewHolder;
import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;

import java.util.HashMap;

public class TopicFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = TopicFragment.class.getSimpleName();
    public static ProgressDialog progressDialog;
    private TopicAdapter mTopicAdapter;
    public static Handler handler;

    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private static final int TOPIC_LOADER = 0;

    private static final String[] TOPIC_COLUMNS = {
            InterviewEntry._ID,
            InterviewEntry.COLUMN_TOPIC,
            "count(" + InterviewEntry.COLUMN_TOPIC + ") AS total"
    };
    static final int COL_ID = 0;
    static final int COL_TOPIC = 1;
    static final int COL_TOTAL = 2;

    //done = ?
    private static final String sDoneSelection = InterviewEntry.COLUMN_DONE + " = ? ";

    //bookmark = ?
    private static final String sBookmarkSelection = InterviewEntry.COLUMN_BOOKMARK + " = ? ";

    //done = ? AND bookmark = ?
    private static final String sDoneAndBookmark =
            InterviewEntry.COLUMN_DONE + " = ? AND " + InterviewEntry.COLUMN_BOOKMARK + " = ? ";


    public interface Callback {
        void onItemSelected(Uri topicUri);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TOPIC_LOADER, null, this);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getContext().getString(R.string.progress_title));
        progressDialog.setMessage(getContext().getString(R.string.progress_message));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        handler = new Handler();
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_topic);
        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        View emptyView = rootView.findViewById(R.id.recyclerview_topic_empty);
        mRecyclerView.setHasFixedSize(true);

        mTopicAdapter = new TopicAdapter(getActivity(), new TopicAdapterOnClickHandler() {
            @Override
            public void onClick(String topic, TopicAdapterViewHolder vh) {
                ((Callback) getActivity()).onItemSelected(InterviewEntry.buildTopic(topic));
                mPosition = vh.getAdapterPosition();
            }
        }, emptyView);

        mRecyclerView.setAdapter(mTopicAdapter);
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    public static HashMap<String, Integer> topicToDoneQues;
    public static HashMap<String, Integer> topicToBookmarkQues;
    public static HashMap<String, Integer> topicToTotalQues;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by topic name.
        String sortOrder = InterviewEntry.COLUMN_TOPIC + " ASC";

//        Log.d(LOG_TAG, "oncreateloader");

        String activityType = Utility.getActivityType(getActivity());
//        Log.d(LOG_TAG, activityType);
        switch (activityType) {
            case Utility.DONE:
                return new CursorLoader(
                        getActivity(),
                        InterviewEntry.CONTENT_URI,
                        TOPIC_COLUMNS,
                        sDoneAndBookmark,
                        new String[]{"1", "0"},
                        sortOrder);
            case Utility.BOOKMARK:
                return new CursorLoader(
                        getActivity(),
                        InterviewEntry.CONTENT_URI,
                        TOPIC_COLUMNS,
                        sDoneAndBookmark,
                        new String[]{"0", "1"},
                        sortOrder);
            case Utility.HOME:
            default:
                return new CursorLoader(
                        getActivity(),
                        InterviewEntry.CONTENT_URI,
                        TOPIC_COLUMNS,
                        sDoneAndBookmark,
                        new String[]{"0", "0"},
                        sortOrder);
        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor newData) {
        topicToDoneQues = new HashMap<>();
        topicToBookmarkQues = new HashMap<>();
        topicToTotalQues = new HashMap<>();
//        Log.d(LOG_TAG, "onloadfinished");
        Cursor cursor = getContext().getContentResolver().query(
                InterviewEntry.CONTENT_URI,
                TOPIC_COLUMNS,
                sDoneSelection,
                new String[]{"1"},
                null);

        if (null != cursor) {
            while (cursor.moveToNext()) {
                topicToDoneQues.put(cursor.getString(COL_TOPIC), cursor.getInt(COL_TOTAL));
            }
            cursor.close();
        }

        cursor = getContext().getContentResolver().query(
                InterviewEntry.CONTENT_URI,
                TOPIC_COLUMNS,
                sBookmarkSelection,
                new String[]{"1"},
                null);

        if (null != cursor) {
            while (cursor.moveToNext()) {
                topicToBookmarkQues.put(cursor.getString(COL_TOPIC), cursor.getInt(COL_TOTAL));
            }
            cursor.close();
        }

        cursor = getContext().getContentResolver().query(
                InterviewEntry.CONTENT_URI,
                TOPIC_COLUMNS,
                null,
                null,
                null);

        if (null != cursor) {
            while (cursor.moveToNext()) {
                topicToTotalQues.put(cursor.getString(COL_TOPIC), cursor.getInt(COL_TOTAL));
            }
            cursor.close();
        }

        mTopicAdapter.swapCursor(newData);
        if (mPosition != RecyclerView.NO_POSITION) {
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (mTopicAdapter.getItemCount() == 0) {
            String activityType = Utility.getActivityType(getActivity());
            TextView tv = (TextView) getView().findViewById(R.id.recyclerview_topic_empty);
            if (null != tv && !topicToTotalQues.isEmpty()) {
                switch (activityType) {
                    case Utility.DONE:
                        tv.setText(R.string.done_empty_message);
                        break;
                    case Utility.BOOKMARK:
                        tv.setText(R.string.bookmark_empty_message);
                        break;
                    case Utility.HOME:
                    default:
                        tv.setText(R.string.home_empty_message);
                        break;
                }
            } else {
                if (!Utility.isNetworkAvailable(getActivity())) {
                    progressDialog.setTitle(getContext().getString(R.string.progress_title_no_network));
                    progressDialog.setMessage(getContext().getString(R.string.progress_message_no_network));
                }
                progressDialog.show();
            }
        } else if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRecyclerView) {
            mRecyclerView.clearOnScrollListeners();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//        Log.d(LOG_TAG, "onloaderReset");
        mTopicAdapter.swapCursor(null);
    }

    void onActivityTypeChanged() {
        getLoaderManager().restartLoader(TOPIC_LOADER, null, this);
    }

}
