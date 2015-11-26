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

import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;

public class TopicFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = TopicFragment.class.getSimpleName();
    private TopicAdapter mTopicAdapter;

    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private static final int TOPIC_LOADER = 0;

    private static final String[] TOPIC_COLUMNS = {
            InterviewEntry._ID,
            InterviewEntry.COLUMN_TOPIC
    };
    static final int COL_ID = 0;
    static final int COL_TOPIC = 1;

    public interface Callback {
        void onItemSelected(Uri topicUri);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TOPIC_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_topic);

        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        View emptyView = rootView.findViewById(R.id.recyclerview_topic_empty);
        mRecyclerView.setHasFixedSize(true);

        mTopicAdapter = new TopicAdapter(getActivity(), new TopicAdapter.TopicAdapterOnClickHandler() {
            @Override
            public void onClick(String topic, TopicAdapter.TopicAdapterViewHolder vh) {
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


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by topic name.
        String sortOrder = InterviewEntry.COLUMN_TOPIC + " ASC";
        return new CursorLoader(
                getActivity(),
                InterviewEntry.CONTENT_URI,
                TOPIC_COLUMNS,
                null,
                null,
                sortOrder);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mTopicAdapter.swapCursor(cursor);
        if (mPosition != RecyclerView.NO_POSITION) {
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (mTopicAdapter.getItemCount() == 0) {
            TextView tv = (TextView) getView().findViewById(R.id.recyclerview_topic_empty);
            if (null != tv && !Utility.isNetworkAvailable(getActivity())) {
                tv.setText(R.string.empty_topic_list_no_network);
            }
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
        mTopicAdapter.swapCursor(null);
    }

}
