package com.aggarwalankush.interview2go;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
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
            InterviewEntry.COLUMN_TOPIC,
            InterviewEntry.COLUMN_QUESTION
    };

    public static final int COL_ID = 0;
    public static final int COL_TOPIC = 1;
    public static final int COL_QUESTION = 2;

    //topic = ? AND question = ?
    private static final String sTopicAndQuestion =
            InterviewEntry.COLUMN_TOPIC + " = ? AND " + InterviewEntry.COLUMN_QUESTION + " = ? ";

    //done = ? AND bookmark = ?
    private static final String sDoneAndBookmark =
            InterviewEntry.COLUMN_DONE + " = ? AND " + InterviewEntry.COLUMN_BOOKMARK + " = ? ";

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

        final View rootView = inflater.inflate(R.layout.fragment_question, container, false);
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
                        String question = "invalid quesion";
                        if (viewHolder instanceof QuestionAdapterViewHolder) {
                            TextView questionView = ((QuestionAdapterViewHolder) viewHolder).mQuestionView;
                            question = questionView.getText().toString();
                            if (mUri != null) {
                                topic = InterviewEntry.getTopicFromUri(mUri);
                            }
                        }

                        String activityType = Utility.getActivityType(getActivity());
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
                                        sTopicAndQuestion,
                                        new String[]{topic, question}
                                );
                                mQuestionAdapter.notifyItemRemoved(itemPosition);

                                final String finalTopic = topic;
                                final String finalQuestion = question;
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
                                                        sTopicAndQuestion,
                                                        new String[]{finalTopic, finalQuestion}
                                                );
                                                Log.d(LOG_TAG, "rows updated " + rowUpdated);
                                                mQuestionAdapter.notifyDataSetChanged();
                                            }
                                        }).setDuration(5000).show();
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
                                        sTopicAndQuestion,
                                        new String[]{topic, question}
                                );
                                mQuestionAdapter.notifyItemRemoved(itemPosition);

                                final String finalTopic = topic;
                                final String finalQuestion = question;
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
                                                        sTopicAndQuestion,
                                                        new String[]{finalTopic, finalQuestion}
                                                );
                                                Log.d(LOG_TAG, "rows updated " + rowUpdated);
                                                mQuestionAdapter.notifyDataSetChanged();
                                            }
                                        }).setDuration(5000).show();

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
            String activityType = Utility.getActivityType(getActivity());
            Log.d(LOG_TAG, activityType);
            switch (activityType) {
                case Utility.DONE:
                    return new CursorLoader(
                            getActivity(),
                            mUri,
                            QUESTION_COLUMNS,
                            sDoneAndBookmark,
                            new String[]{"1", "0"},
                            sortOrder);
                case Utility.BOOKMARK:
                    return new CursorLoader(
                            getActivity(),
                            mUri,
                            QUESTION_COLUMNS,
                            sDoneAndBookmark,
                            new String[]{"0", "1"},
                            sortOrder);
                case Utility.HOME:
                default:
                    return new CursorLoader(
                            getActivity(),
                            mUri,
                            QUESTION_COLUMNS,
                            sDoneAndBookmark,
                            new String[]{"0", "0"},
                            sortOrder);
            }

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