package com.aggarwalankush.interview2go;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class Utility {

    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }


    public static int getFontSize(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        switch (prefs.getString(context.getString(R.string.pref_font_key), context.getString(R.string.pref_font_small))) {
            case "small":
                return android.R.style.TextAppearance_DeviceDefault_Small;
            case "large":
                return android.R.style.TextAppearance_DeviceDefault_Large;
            case "normal":
            default:
                return android.R.style.TextAppearance_DeviceDefault_Medium;

        }
    }

    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_enable_darkMode_key),
                Boolean.parseBoolean(context.getString(R.string.pref_enable_darkMode_default)));
    }

    public static int getImageResouce(String topic) {
        switch (topic) {
            case "arrays":
                return R.drawable.ic_arrays;
            case "bitwiseandmath":
                return R.drawable.ic_bitwiseandmath;
            case "graphs":
                return R.drawable.ic_graphs;
            case "linkedlists":
                return R.drawable.ic_linkedlists;
            case "recursiondp":
                return R.drawable.ic_recursiondp;
            case "sorting":
                return R.drawable.ic_sorting;
            case "stacksqueues":
                return R.drawable.ic_stacksqueues;
            case "strings":
                return R.drawable.ic_strings;
            case "trees":
                return R.drawable.ic_trees;
            case "xbonus":
                return R.drawable.ic_bonus;
        }
        return R.drawable.ic_default;
    }

    public static String getDisplayTopicName(String topic) {
        switch (topic) {
            case "arrays":
                return "Arrays";
            case "bitwiseandmath":
                return "Bitwise and Maths";
            case "graphs":
                return "Graphs";
            case "linkedlists":
                return "Linked Lists";
            case "recursiondp":
                return "Recursion and Dynamic Programming";
            case "sorting":
                return "Sorting";
            case "stacksqueues":
                return "Stacks and Queues";
            case "strings":
                return "Strings";
            case "trees":
                return "Trees";
            case "xbonus":
                return "Bonus Questions";
        }
        return "Random";
    }

    public static String getDatabaseTopicName(String topic) {
        switch (topic) {
            case "Arrays":
                return "arrays";
            case "Bitwise and Maths":
                return "bitwiseandmath";
            case "Graphs":
                return "graphs";
            case "Linked Lists":
                return "linkedlists";
            case "Recursion and Dynamic Programming":
                return "recursiondp";
            case "Sorting":
                return "sorting";
            case "Stacks and Queues":
                return "stacksqueues";
            case "Strings":
                return "strings";
            case "Trees":
                return "trees";
            case "Bonus Questions":
                return "xbonus";
        }
        return "Random";
    }

}
