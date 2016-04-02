package com.aggarwalankush.interview2go.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;

public class InterviewDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "interview.db";

    public InterviewDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_INTERVIEW_TABLE =
                "CREATE TABLE " + InterviewEntry.TABLE_NAME + " ("
                        + InterviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + InterviewEntry.COLUMN_ROW_NO + " INTEGER NOT NULL,"
                        + InterviewEntry.COLUMN_TOPIC + " TEXT NOT NULL, "
                        + InterviewEntry.COLUMN_QUESTION + " TEXT NOT NULL, "
                        + InterviewEntry.COLUMN_SOLUTION + " TEXT NOT NULL DEFAULT '', "
                        + InterviewEntry.COLUMN_QUESTION_DETAIL + " TEXT DEFAULT 'Click here to view Solution', "
                        + InterviewEntry.COLUMN_DARK_SOLUTION + " TEXT NOT NULL DEFAULT '', "
                        + InterviewEntry.COLUMN_OUTPUT + " TEXT NOT NULL, "
                        + InterviewEntry.COLUMN_BOOKMARK + " INTEGER DEFAULT 0, "
                        + InterviewEntry.COLUMN_DONE + " INTEGER DEFAULT 0, "
                        + " UNIQUE ("
                        + InterviewEntry.COLUMN_TOPIC + ", " + InterviewEntry.COLUMN_QUESTION
                        + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_INTERVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (newVersion) {
            case 2:
                db.execSQL("ALTER TABLE " + InterviewEntry.TABLE_NAME + " ADD COLUMN " + InterviewEntry.COLUMN_QUESTION_DETAIL + " TEXT DEFAULT 'Click here to view Solution'");
                break;
            case 1:
            default:
                db.execSQL("DROP TABLE IF EXISTS " + InterviewEntry.TABLE_NAME);
                onCreate(db);
        }
    }

}
