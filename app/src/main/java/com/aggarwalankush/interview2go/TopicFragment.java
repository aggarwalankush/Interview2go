package com.aggarwalankush.interview2go;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

    //topic = ?
    private static final String sTopicSelection = InterviewEntry.COLUMN_TOPIC + " = ? ";

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

        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(ViewHolder viewHolder, int direction) {
                        int itemPosition = viewHolder.getAdapterPosition();

                        String topic = "invalid topic";
                        if (viewHolder instanceof TopicAdapterViewHolder) {
                            TextView topicView = ((TopicAdapterViewHolder) viewHolder).mTopicView;
                            topic = Utility.getDatabaseTopicName(topicView.getText().toString());
                        }

                        String activityType = Utility.getActivityType(getActivity());
                        Log.d(LOG_TAG, activityType);

                        int bookmark = 1;
                        int done = 1;
                        String bookmark_message = "Bookmarked";
                        String done_message = "Marked Done";

                        switch (activityType) {
                            case Utility.BOOKMARK:
                                //if bookmark view, undo bookmark on left swipe
                                bookmark = 0;
                                bookmark_message = "Removed Bookmark";
                                break;
                            case Utility.DONE:
                                done = 0;
                                done_message = "Removed from Done";
                                break;
                        }

                        final int opp_bookmark = bookmark == 1 ? 0 : 1;
                        final int opp_done = done == 1 ? 0 : 1;

                        switch (direction) {
                            case ItemTouchHelper.LEFT: {
                                // bookmark item
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(InterviewEntry.COLUMN_BOOKMARK, bookmark);
                                contentValues.put(InterviewEntry.COLUMN_DONE, 0);
                                getContext().getContentResolver().update(
                                        InterviewEntry.CONTENT_URI,
                                        contentValues,
                                        sTopicSelection,
                                        new String[]{topic}
                                );
                                mTopicAdapter.notifyItemRemoved(itemPosition);

                                final String finalTopic = topic;
                                Snackbar.make(rootView, bookmark_message, Snackbar.LENGTH_LONG)
                                        .setAction("Undo", new OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                ContentValues contentValues = new ContentValues();
                                                contentValues.put(InterviewEntry.COLUMN_BOOKMARK, opp_bookmark);
                                                contentValues.put(InterviewEntry.COLUMN_DONE, opp_done);
                                                int rowUpdated = getContext().getContentResolver().update(
                                                        InterviewEntry.CONTENT_URI,
                                                        contentValues,
                                                        sTopicSelection,
                                                        new String[]{finalTopic}
                                                );
                                                Log.d(LOG_TAG, "rows updated " + rowUpdated);
                                                mTopicAdapter.notifyDataSetChanged();
                                            }
                                        }).show();
                                break;
                            }
                            case ItemTouchHelper.RIGHT: {
                                // done item
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(InterviewEntry.COLUMN_DONE, done);
                                contentValues.put(InterviewEntry.COLUMN_BOOKMARK, 0);
                                getContext().getContentResolver().update(
                                        InterviewEntry.CONTENT_URI,
                                        contentValues,
                                        sTopicSelection,
                                        new String[]{topic}
                                );
                                mTopicAdapter.notifyItemRemoved(itemPosition);

                                final String finalTopic = topic;
                                Snackbar.make(rootView, done_message, Snackbar.LENGTH_LONG)
                                        .setAction("Undo", new OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                ContentValues contentValues = new ContentValues();
                                                contentValues.put(InterviewEntry.COLUMN_DONE, opp_done);
                                                contentValues.put(InterviewEntry.COLUMN_BOOKMARK, opp_bookmark);
                                                int rowUpdated = getContext().getContentResolver().update(
                                                        InterviewEntry.CONTENT_URI,
                                                        contentValues,
                                                        sTopicSelection,
                                                        new String[]{finalTopic}
                                                );
                                                Log.d(LOG_TAG, "rows updated " + rowUpdated);
                                                mTopicAdapter.notifyDataSetChanged();
                                            }
                                        }).show();

                                break;
                            }
                        }


                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                            String activityType = Utility.getActivityType(getActivity());

                            int done_icon = R.drawable.ic_done;
                            int done_color = R.color.doneColor;
                            int bookmark_icon = R.drawable.ic_bookmark;
                            int bookmark_color = R.color.bookmarkColor;

                            switch (activityType) {
                                case Utility.BOOKMARK:
                                    bookmark_icon = R.drawable.ic_home;
                                    bookmark_color = R.color.colorPrimary;
                                    break;
                                case Utility.DONE:
                                    done_icon = R.drawable.ic_home;
                                    done_color = R.color.colorPrimary;
                                    break;
                            }


                            View itemView = viewHolder.itemView;
                            Bitmap icon;
                            Paint paint = new Paint();
                            int iconSize = (int) (Math.abs(dX) / 2);
                            iconSize = Math.min(iconSize, 70);
                            if (dX > 0) {
                                icon = BitmapFactory.decodeResource(
                                        getContext().getResources(), done_icon);
                                icon = Bitmap.createScaledBitmap(icon, iconSize > 0 ? iconSize : 1, iconSize > 0 ? iconSize : 1, false);
                                paint.setColor(ContextCompat.getColor(getContext(), done_color));

                                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                        (float) itemView.getBottom(), paint);
                                c.drawBitmap(icon,
                                        (float) itemView.getLeft() + dpToPx(16),
                                        (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2,
                                        paint);
                            } else {
                                icon = BitmapFactory.decodeResource(getContext().getResources(), bookmark_icon);
                                icon = Bitmap.createScaledBitmap(icon, iconSize > 0 ? iconSize : 1, iconSize > 0 ? iconSize : 1, false);
                                paint.setColor(ContextCompat.getColor(getContext(), bookmark_color));

                                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                        (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                                c.drawBitmap(icon,
                                        (float) itemView.getRight() - dpToPx(16) - icon.getWidth(),
                                        (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2,
                                        paint);
                            }
                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        }
                    }

                    public int dpToPx(int dp) {
                        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
                        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

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

        Log.d(LOG_TAG, "oncreateloader");

        String activityType = Utility.getActivityType(getActivity());
        Log.d(LOG_TAG, activityType);
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
        Log.d(LOG_TAG, "onloadfinished");
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
        Log.d(LOG_TAG, "onloaderReset");
        mTopicAdapter.swapCursor(null);
    }

    void onActivityTypeChanged() {
        getLoaderManager().restartLoader(TOPIC_LOADER, null, this);
    }

}
