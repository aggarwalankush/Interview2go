package com.aggarwalankush.interview2go.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class InterviewAuthenticatorService extends Service {
    private InterviewAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new InterviewAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
