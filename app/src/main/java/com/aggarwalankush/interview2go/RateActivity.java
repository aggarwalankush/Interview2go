package com.aggarwalankush.interview2go;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class RateActivity {

    private static void openAppInPlayStore(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + context.getPackageName())));
        } catch (android.content.ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

    private static void openFeedback(Context paramContext) {
        Intent localIntent = new Intent(Intent.ACTION_SENDTO);
        localIntent.setData(Uri.parse("mailto:" + "ankushagg93@gmail.com"));
        localIntent.putExtra(Intent.EXTRA_CC, "");
        String str;
        try {
            str = paramContext.getPackageManager().getPackageInfo(paramContext.getPackageName(), 0).versionName;
            localIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for Your Android App");
            localIntent.putExtra(Intent.EXTRA_TEXT, "\n\n----------------------------------\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + str + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER);
            paramContext.startActivity(Intent.createChooser(localIntent, "Choose an Email client :"));
        } catch (Exception e) {
            Log.d("OpenFeedback", e.getMessage());
        }
    }

    public static void showRateAppDialog(Context context) {
        createAppRatingDialog(context, context.getString(R.string.rate_app_title), context.getString(R.string.rate_app_message)).show();
    }

    private static AlertDialog createAppRatingDialog(final Context context, String rateAppTitle, String rateAppMessage) {
        return new AlertDialog.Builder(context).setPositiveButton(context.getString(R.string.dialog_app_rate), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                openAppInPlayStore(context);
            }
        }).setNegativeButton(context.getString(R.string.dialog_your_feedback), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                openFeedback(context);
            }
        }).setNeutralButton(context.getString(R.string.dialog_ask_later), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                paramAnonymousDialogInterface.dismiss();
            }
        }).setMessage(rateAppMessage).setTitle(rateAppTitle).create();
    }
}

