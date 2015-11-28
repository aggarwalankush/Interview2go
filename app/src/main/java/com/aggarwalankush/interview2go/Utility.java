package com.aggarwalankush.interview2go;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utility {

    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static int getImageResouce(String topic) {
        switch (topic.toLowerCase()) {
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

    public static String getTopicName(String topic) {
        switch (topic.toLowerCase()) {
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

}
