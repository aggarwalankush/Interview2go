package com.aggarwalankush.interview2go.sync;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.aggarwalankush.interview2go.R;
import com.aggarwalankush.interview2go.TopicFragment;
import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TreeMap;
import java.util.Vector;

public class InterviewSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = InterviewSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the github, in seconds.
    // 60 * 24 * 60 * 60  = 60 days
    public static final int SYNC_INTERVAL = 60 * 24 * 60 * 60;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    //topic = ? AND question = ?
    private static final String sTopicAndQuestion =
            InterviewEntry.COLUMN_TOPIC + " = ? AND " + InterviewEntry.COLUMN_QUESTION + " = ? ";

    boolean updateProgressTitle = true;
    boolean emptyDB = false;

    Context context;

    public InterviewSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        int count = 0;
        TreeMap<String, TreeMap<String, String>> topicToQueToOutput;
        Vector<ContentValues> cVVector = new Vector<>();

        final String SERVER_BASE_URL = getContext().getString(R.string.server_base_url);
        final String SERVER_OUTPUT_FILE = getContext().getString(R.string.server_output_file);
        final String SERVER_LIGHT_SOLUTION = getContext().getString(R.string.server_light_solution);
        final String SERVER_DARK_SOLUTION = getContext().getString(R.string.server_dark_solution);

        // Output.json file handling
        Uri outputJsonUri = Uri.parse(SERVER_BASE_URL).buildUpon()
                .appendPath(SERVER_OUTPUT_FILE)
                .build();
        String outputJsonStr = getDataFromServer(outputJsonUri);
        String[][] topicQuesDetailOutput = jsonToArray(outputJsonStr);

        for (String[] array : topicQuesDetailOutput) {
            if (array.length < 4) {
                continue;
            }
            String topic = array[0];
            String question = array[1];
            String detail = array[2];
            String output = array[3];

            count++;
            // adding values to content values
            ContentValues contentValues = new ContentValues();
            contentValues.put(InterviewEntry.COLUMN_ROW_NO, count);
            contentValues.put(InterviewEntry.COLUMN_TOPIC, topic.toLowerCase());
            contentValues.put(InterviewEntry.COLUMN_QUESTION, question);
            contentValues.put(InterviewEntry.COLUMN_QUESTION_DETAIL, detail);
            contentValues.put(InterviewEntry.COLUMN_OUTPUT, output);
            cVVector.add(contentValues);

            // update progress bar
            updateProgressBar();
        }


        // insert into database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContext().getContentResolver().bulkInsert(InterviewEntry.CONTENT_URI, cvArray);
        }


        ////////

        for (String[] array : topicQuesDetailOutput) {
            if (array.length < 4) {
                continue;
            }
            String topic = array[0];
            String question = array[1];
            String solution = "Invalid Solution";
            String darkSolution = "Invalid Solution";
            int bookmark = getBookmark(topic, question);
            int done = getDone(topic, question);

            // light solution handling
            Uri lightSolutionUri = Uri.parse(SERVER_BASE_URL).buildUpon()
                    .appendPath(SERVER_LIGHT_SOLUTION)
                    .appendPath(topic)
                    .appendPath(question)
                    .build();
            String light_solution = getDataFromServer(lightSolutionUri);
            if (null != light_solution) {
                solution = light_solution;
            }

            // dark solution handling
            Uri darkSolutionUri = Uri.parse(SERVER_BASE_URL).buildUpon()
                    .appendPath(SERVER_DARK_SOLUTION)
                    .appendPath(topic)
                    .appendPath(question)
                    .build();
            String dark_solution = getDataFromServer(darkSolutionUri);
            if (null != dark_solution) {
                darkSolution = dark_solution;
            }


            ContentValues contentValues = new ContentValues();
            contentValues.put(InterviewEntry.COLUMN_SOLUTION, solution);
            contentValues.put(InterviewEntry.COLUMN_DARK_SOLUTION, darkSolution);
            contentValues.put(InterviewEntry.COLUMN_BOOKMARK, bookmark);
            contentValues.put(InterviewEntry.COLUMN_DONE, done);

            getContext().getContentResolver().update(
                    InterviewEntry.CONTENT_URI,
                    contentValues,
                    sTopicAndQuestion,
                    new String[]{topic.toLowerCase(), question}
            );
        }

        //////

    }


    public void onLocalPerformSync() {
        int count = 0;
        Vector<ContentValues> cVVector = new Vector<>();

        String outputJsonStr = getDataFromApp("Output.json");
        String[][] topicQuesDetailOutput = jsonToArray(outputJsonStr);

        for (String[] array : topicQuesDetailOutput) {
            if (array.length < 4) {
                continue;
            }
            String topic = array[0];
            String question = array[1];
            String detail = array[2];
            String output = array[3];

            String solution = "Invalid Solution";
            String darkSolution = "Invalid Solution";
            int bookmark = getBookmark(topic, question);
            int done = getDone(topic, question);

            String light_solution = getDataFromApp("LightModeHtml/" + topic + "/" + question);
            if (null != light_solution) {
                solution = light_solution;
            }

            String dark_solution = getDataFromApp("DarkModeHtml/" + topic + "/" + question);
            if (null != dark_solution) {
                darkSolution = dark_solution;
            }
            count++;
            // adding values to content values
            ContentValues contentValues = new ContentValues();
            contentValues.put(InterviewEntry.COLUMN_ROW_NO, count);
            contentValues.put(InterviewEntry.COLUMN_TOPIC, topic.toLowerCase());
            contentValues.put(InterviewEntry.COLUMN_QUESTION, question);
            contentValues.put(InterviewEntry.COLUMN_SOLUTION, solution);
            contentValues.put(InterviewEntry.COLUMN_QUESTION_DETAIL, detail);
            contentValues.put(InterviewEntry.COLUMN_DARK_SOLUTION, darkSolution);
            contentValues.put(InterviewEntry.COLUMN_OUTPUT, output);
            contentValues.put(InterviewEntry.COLUMN_BOOKMARK, bookmark);
            contentValues.put(InterviewEntry.COLUMN_DONE, done);
            cVVector.add(contentValues);

            // update progress bar
            updateProgressBar();
        }


        // insert into database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContext().getContentResolver().bulkInsert(InterviewEntry.CONTENT_URI, cvArray);
        }

    }


    public String[][] jsonToArray(String outputJsonStr) {
        String[][] topicQuesDetailOutput = new String[100][];
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = jsonParser.parse(outputJsonStr).getAsJsonArray();
        int i = 0;
        for (JsonElement element : jsonArray) {

            JsonObject jsonObject = element.getAsJsonObject();
            String topic = jsonObject.get("topic").getAsString();
            String question = jsonObject.get("question").getAsString();
            String detail = jsonObject.get("detail").getAsString();
            String output = jsonObject.get("solution").getAsString();
            Log.d(LOG_TAG, topic);
            topicQuesDetailOutput[i++] = new String[]{topic, question, detail, output};
        }
        return topicQuesDetailOutput;
    }


    public void updateProgressBar() {
        if (TopicFragment.handler != null && TopicFragment.progressDialog != null) {
            TopicFragment.handler.post(new Runnable() {
                @Override
                public void run() {
                    TopicFragment.progressDialog.incrementProgressBy(1);
                    if (updateProgressTitle) {
                        TopicFragment.progressDialog.setTitle(getContext().getString(R.string.progress_title));
                        TopicFragment.progressDialog.setMessage(getContext().getString(R.string.progress_message));
                        updateProgressTitle = false;
                    }
                }
            });
        }
    }

    public int getBookmark(String topic, String question) {
        int bookmark = 0;
        try {
//            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
//            queryBuilder.setTables(InterviewEntry.OLD_TABLE_NAME);
//            Cursor cursor = queryBuilder.query(new InterviewDbHelper(
//                            getContext()).getReadableDatabase(),
//                    new String[]{InterviewEntry.COLUMN_BOOKMARK, InterviewEntry.COLUMN_TOPIC, InterviewEntry.COLUMN_QUESTION},
//                    InterviewEntry.COLUMN_TOPIC + " = ? AND " + InterviewEntry.COLUMN_QUESTION + " = ? ",
//                    new String[]{topic.toLowerCase(), question}, null, null, null);
            Cursor cursor = getContext().getContentResolver().query(
                    InterviewEntry.buildTopicWithQuestion(topic.toLowerCase(), question),
                    new String[]{InterviewEntry.COLUMN_BOOKMARK},
                    null,
                    null,
                    null
            );
//            Log.d("hellocursor", DatabaseUtils.dumpCursorToString(cursor));
            if ((cursor != null) && (cursor.getCount() > 0)) {
                cursor.moveToFirst();
                bookmark = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            return bookmark;
        }
        return bookmark;
    }

    public int getDone(String topic, String question) {
        int done = 0;
        if (!isEmptyDB()) {
            Cursor cursor = getContext().getContentResolver().query(
                    InterviewEntry.buildTopicWithQuestion(topic.toLowerCase(), question),
                    new String[]{InterviewEntry.COLUMN_DONE},
                    null,
                    null,
                    null
            );
            if ((cursor != null) && (cursor.getCount() > 0)) {
                cursor.moveToFirst();
                done = cursor.getInt(0);
                cursor.close();
            }
        }
        return done;
    }

    public boolean haveDetailColumn() {
        try {
            String detail;
            if (!isEmptyDB()) {
                Cursor cursor = getContext().getContentResolver().query(
                        InterviewEntry.CONTENT_URI,
                        new String[]{InterviewEntry.COLUMN_QUESTION_DETAIL},
                        null,
                        null,
                        null
                );
                if ((cursor != null) && (cursor.getCount() > 0)) {
                    cursor.moveToFirst();
                    detail = cursor.getString(0);
                    cursor.close();
                    if (detail.equalsIgnoreCase("Click here to view Solution")) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public boolean isEmptyDB() {
        if (emptyDB) {
            return true;
        }
        Cursor cursor = getContext().getContentResolver().query(
                InterviewEntry.CONTENT_URI,
                new String[]{InterviewEntry._ID},
                null,
                null,
                null);

        if (null == cursor || cursor.getCount() == 0) {
            emptyDB = true;
        } else {
            cursor.close();
        }
        return emptyDB;
    }

    public String getDataFromServer(Uri builtUri) {
//        Log.d(LOG_TAG, "getting data from server");
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            if (builder.length() == 0) {
                return null;
            }

            return builder.toString();
        } catch (IOException e) {
//            Log.e(LOG_TAG, "IO Exception", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
//                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return null;
    }

    public String getDataFromApp(String filePath) {
//        Log.d(LOG_TAG, "getting data from app");
        String jsonOutputFile = readFileContents(filePath);
        if (jsonOutputFile.length() == 0) {
            return null;
        }
        return jsonOutputFile;
    }


    public String readFileContents(String filePath) {

        StringBuilder fileContent = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(filePath)));

            String mLine;
            while ((mLine = reader.readLine()) != null) {
                fileContent.append(mLine);
            }
        } catch (IOException e) {
//            Log.d("Error reading file", e.getMessage().toString());
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
        return fileContent.toString();

    }


    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static void syncLocally(Context context) {
        new InterviewSyncAdapter(context, true).onLocalPerformSync();
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        InterviewSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
//        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

}
