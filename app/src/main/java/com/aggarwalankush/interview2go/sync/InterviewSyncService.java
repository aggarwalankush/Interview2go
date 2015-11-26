package com.aggarwalankush.interview2go.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class InterviewSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static InterviewSyncAdapter sInterviewSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sInterviewSyncAdapter == null) {
                sInterviewSyncAdapter = new InterviewSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sInterviewSyncAdapter.getSyncAdapterBinder();
    }
}
