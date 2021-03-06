package com.aggarwalankush.interview2go.data;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterviewProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private InterviewDbHelper mOpenHelper;

    static final int INTERVIEW = 100;
    static final int INTERVIEW_WITH_TOPIC = 101;
    static final int INTERVIEW_WITH_TOPIC_AND_QUESTION = 102;
    static final int SEARCH_QUESTIONS = 103;
    static final int SEARCH_SUGGEST = 104;
    static final int SEARCH_WITH_ROW_NO = 105;

    private static final SQLiteQueryBuilder sQueryBuilder;

    static {
        sQueryBuilder = new SQLiteQueryBuilder();
        sQueryBuilder.setTables(InterviewEntry.TABLE_NAME);
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = InterviewContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, InterviewContract.PATH_INTERVIEW, INTERVIEW);
        matcher.addURI(authority, InterviewContract.PATH_INTERVIEW + "/#", SEARCH_WITH_ROW_NO);
        matcher.addURI(authority, InterviewContract.PATH_INTERVIEW + "/*", INTERVIEW_WITH_TOPIC);
        matcher.addURI(authority, InterviewContract.PATH_INTERVIEW + "/*/*", INTERVIEW_WITH_TOPIC_AND_QUESTION);
        matcher.addURI(authority, "search/" + InterviewContract.PATH_INTERVIEW + "/*", SEARCH_QUESTIONS);
        matcher.addURI(authority, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(authority, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new InterviewDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INTERVIEW:
                return InterviewEntry.CONTENT_TYPE;
            case INTERVIEW_WITH_TOPIC:
                return InterviewEntry.CONTENT_TYPE;
            case INTERVIEW_WITH_TOPIC_AND_QUESTION:
                return InterviewEntry.CONTENT_ITEM_TYPE;
            case SEARCH_QUESTIONS:
                return InterviewEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    //topic = ?
    private static final String sTopicSelection = InterviewEntry.COLUMN_TOPIC + " = ? ";

    //row_no = ?
    private static final String sRowSelection = InterviewEntry.COLUMN_ROW_NO + " = ? ";

    //topic = ? AND question = ?
    private static final String sTopicAndQuestionSelection =
            InterviewEntry.COLUMN_TOPIC + " = ? AND " + InterviewEntry.COLUMN_QUESTION + " = ? ";

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "interview/*/*"
            case INTERVIEW_WITH_TOPIC_AND_QUESTION: {
                String topic = InterviewEntry.getTopicFromUri(uri);
                String question = InterviewEntry.getQuestionFromUri(uri);

                if (null != selection) {
                    selection += " AND " + sTopicAndQuestionSelection;
                } else {
                    selection = sTopicAndQuestionSelection;
                }

                if (null != selectionArgs) {
                    int len = selectionArgs.length;
                    List<String> list = new ArrayList<>(Arrays.asList(selectionArgs));
                    list.add(topic);
                    list.add(question);
                    selectionArgs = list.toArray(new String[len + 1]);
                } else {
                    selectionArgs = new String[]{topic, question};
                }

                retCursor = sQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "interview/*"
            case INTERVIEW_WITH_TOPIC: {
                String topic = InterviewEntry.getTopicFromUri(uri);
                if (null != selection) {
                    selection += " AND " + sTopicSelection;
                } else {
                    selection = sTopicSelection;
                }

                if (null != selectionArgs) {
                    int len = selectionArgs.length;
                    List<String> list = new ArrayList<>(Arrays.asList(selectionArgs));
                    list.add(topic);
                    selectionArgs = list.toArray(new String[len + 1]);
                } else {
                    selectionArgs = new String[]{topic};
                }

                retCursor = sQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "interview"
            case INTERVIEW: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        true,
                        InterviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        InterviewEntry.COLUMN_TOPIC,
                        null,
                        sortOrder,
                        null
                );
                break;
            }
            case SEARCH_WITH_ROW_NO: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        InterviewEntry.TABLE_NAME,
                        projection,
                        sRowSelection,
                        new String[]{String.valueOf(InterviewEntry.getRowNoFromUri(uri))},
                        null,
                        null,
                        null,
                        null
                );

                break;
            }
            case SEARCH_QUESTIONS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        InterviewEntry.TABLE_NAME,
                        new String[]{InterviewEntry._ID, InterviewEntry.COLUMN_TOPIC, InterviewEntry.COLUMN_QUESTION},
                        InterviewEntry.COLUMN_QUESTION + " LIKE ? ",
                        new String[]{selectionArgs[0] + "%"},
                        null,
                        null,
                        null);
                break;
            }
            case SEARCH_SUGGEST: {
                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables(InterviewEntry.TABLE_NAME);
                Map<String, String> projectionMap = new HashMap<>();
                projectionMap.put(InterviewEntry.COLUMN_QUESTION, InterviewEntry.COLUMN_QUESTION + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
                projectionMap.put(InterviewEntry._ID, InterviewEntry._ID);
                projectionMap.put(InterviewEntry.COLUMN_ROW_NO, InterviewEntry.COLUMN_ROW_NO + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA);
                projectionMap.put(InterviewEntry.COLUMN_TOPIC, InterviewEntry.COLUMN_TOPIC);
                builder.setProjectionMap(projectionMap);

                if (selectionArgs != null && selectionArgs.length > 0 && selectionArgs[0].length() > 0) {

                    retCursor = builder.query(mOpenHelper.getReadableDatabase(),
                            new String[]{InterviewEntry._ID, InterviewEntry.COLUMN_ROW_NO, InterviewEntry.COLUMN_TOPIC, InterviewEntry.COLUMN_QUESTION},
                            InterviewEntry.COLUMN_QUESTION + " LIKE ? ",
                            new String[]{selectionArgs[0] + "%"},
                            null,
                            null,
                            null);
                } else {
                    retCursor = builder.query(mOpenHelper.getReadableDatabase(),
                            new String[]{InterviewEntry._ID, InterviewEntry.COLUMN_ROW_NO, InterviewEntry.COLUMN_TOPIC, InterviewEntry.COLUMN_QUESTION},
                            null,
                            null,
                            null,
                            null,
                            null);
                }


                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (null != getContext()) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return retCursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
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
        if (null != getContext()) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
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

        if (rowsDeleted != 0 && null != getContext()) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
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
        if (rowsUpdated != 0 && null != getContext()) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
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
                if (null != getContext()) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }


}
