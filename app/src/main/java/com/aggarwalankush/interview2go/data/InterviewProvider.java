package com.aggarwalankush.interview2go.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;

public class InterviewProvider extends ContentProvider {

    private static final String LOG_TAG = InterviewProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private InterviewDbHelper mOpenHelper;

    static final int INTERVIEW = 100;
    static final int INTERVIEW_WITH_TOPIC = 101;
    static final int INTERVIEW_WITH_TOPIC_AND_QUESTION = 102;

    private static final SQLiteQueryBuilder sQueryBuilder;

    static {
        sQueryBuilder = new SQLiteQueryBuilder();
        sQueryBuilder.setTables(InterviewEntry.TABLE_NAME);
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = InterviewContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, InterviewContract.PATH_INTERVIEW, INTERVIEW);
        matcher.addURI(authority, InterviewContract.PATH_INTERVIEW + "/*", INTERVIEW_WITH_TOPIC);
        matcher.addURI(authority, InterviewContract.PATH_INTERVIEW + "/*/*", INTERVIEW_WITH_TOPIC_AND_QUESTION);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new InterviewDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INTERVIEW:
                return InterviewEntry.CONTENT_TYPE;
            case INTERVIEW_WITH_TOPIC:
                return InterviewEntry.CONTENT_TYPE;
            case INTERVIEW_WITH_TOPIC_AND_QUESTION:
                return InterviewEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    //topic = ?
    private static final String sTopicSelection = InterviewEntry.COLUMN_TOPIC + " = ? ";

    //topic = ? AND question = ?
    private static final String sTopicAndQuestionSelection =
            InterviewEntry.COLUMN_TOPIC + " = ? AND " + InterviewEntry.COLUMN_QUESTION + " = ? ";

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "interview/*/*"
            case INTERVIEW_WITH_TOPIC_AND_QUESTION: {
                Log.d(LOG_TAG, "Interview with topic and question called");

                String topic = InterviewEntry.getTopicFromUri(uri);
                String question = InterviewEntry.getQuestionFromUri(uri);
                retCursor = sQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sTopicAndQuestionSelection,
                        new String[]{topic, question},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "interview/*"
            case INTERVIEW_WITH_TOPIC: {
                Log.d(LOG_TAG, "Interview with topic called");

                String topic = InterviewEntry.getTopicFromUri(uri);
                retCursor = sQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sTopicSelection,
                        new String[]{topic},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "interview"
            case INTERVIEW: {
                Log.d(LOG_TAG, "Interview called");
                retCursor = mOpenHelper.getReadableDatabase().query(
                        true,
                        InterviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        null
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        long _id;

        switch (match) {
            case INTERVIEW:
                _id = db.insert(InterviewEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = InterviewEntry.buildInterviewUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case INTERVIEW:
                rowsDeleted = db.delete(InterviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case INTERVIEW:
                rowsUpdated = db.update(InterviewEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INTERVIEW:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(InterviewEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
