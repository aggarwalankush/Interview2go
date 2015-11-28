package com.aggarwalankush.interview2go.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class InterviewContract {
    public static final String CONTENT_AUTHORITY = "com.aggarwalankush.interview2go.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INTERVIEW = "interview";

    public static final class InterviewEntry implements BaseColumns {

        public static final String TABLE_NAME = "interview";

        public static final String COLUMN_TOPIC = "topic";
        public static final String COLUMN_QUESTION = "question";
        public static final String COLUMN_SOLUTION = "solution";
        public static final String COLUMN_DARK_SOLUTION = "dark_solution";
        public static final String COLUMN_OUTPUT = "output";
        public static final String COLUMN_BOOKMARK = "bookmark";
        public static final String COLUMN_DONE = "done";


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_INTERVIEW).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INTERVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INTERVIEW;

        public static Uri buildInterviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTopic(String topic) {
            return CONTENT_URI.buildUpon().appendPath(topic).build();
        }

        public static Uri buildTopicWithQuestion(String topic, String question) {
            return CONTENT_URI.buildUpon().appendPath(topic).appendPath(question).build();
        }

        public static String getTopicFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getQuestionFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

    }

}
