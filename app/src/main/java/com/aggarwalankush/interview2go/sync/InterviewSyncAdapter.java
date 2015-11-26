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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.aggarwalankush.interview2go.R;
import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

public class InterviewSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = InterviewSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the github, in seconds.
    // 5 * 24 * 60 * 60  = 5 days
    public static final int SYNC_INTERVAL = 5 * 24 * 60 * 60;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    public InterviewSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        TreeMap<String, ArrayList<String>> topicToQuestions = new TreeMap<>();

        try {
            final String SERVER_BASE_URL = getContext().getString(R.string.server_base_url);
            String server_informer_file = getContext().getString(R.string.server_informer_file);

            Uri builtUri = Uri.parse(SERVER_BASE_URL).buildUpon().appendPath(server_informer_file).build();
            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            if (builder.length() == 0) {
                return;
            }
            String informerJsonStr = builder.toString();
            Log.d(LOG_TAG, informerJsonStr);

            JsonObject jsonObject = new JsonParser().parse(informerJsonStr).getAsJsonObject();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();

            for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String topic = entry.getKey();
                String jsonElem = entry.getValue().getAsString();
                ArrayList<String> list = new Gson().fromJson(jsonElem, type);
                topicToQuestions.put(topic, list);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Error ", e);
        } catch (JsonParseException e) {
            Log.e(LOG_TAG, "Json Parse error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        for (Entry<String, ArrayList<String>> entry : topicToQuestions.entrySet()) {
            String topic = entry.getKey();
            ArrayList<String> questions = entry.getValue();
            for (String question : questions) {
                try {
                    final String SERVER_BASE_URL = getContext().getString(R.string.server_base_url);
                    Uri builtUri = Uri.parse(SERVER_BASE_URL).buildUpon().appendPath(topic).appendPath(question).build();
                    URL url = new URL(builtUri.toString());

                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder builder = new StringBuilder();
                    if (inputStream == null) {
                        // Nothing to do.
                        return;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    int count=0;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line+"\n");
                        count++;
                    }

                    if (builder.length() == 0) {
                        return;
                    }


                    String solution = builder.toString();
                    Log.d(LOG_TAG, solution);
                    Log.d(LOG_TAG, count + "");

                    ContentValues contentValues = new ContentValues();

                    contentValues.put(InterviewEntry.COLUMN_TOPIC, topic);
                    contentValues.put(InterviewEntry.COLUMN_QUESTION, question);
                    contentValues.put(InterviewEntry.COLUMN_SOLUTION, solution);
                    contentValues.put(InterviewEntry.COLUMN_OUTPUT, " ");

                    Log.d(LOG_TAG,getContext().getContentResolver().insert(InterviewEntry.CONTENT_URI, contentValues)+"");

                    break;
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IO Error ", e);
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }
            }
            break;
        }

    }


    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
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
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


}
