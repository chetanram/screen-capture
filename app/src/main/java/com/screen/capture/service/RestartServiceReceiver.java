package com.screen.capture.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import com.screen.capture.util.Constants;

/**
 * Created by chetan on 12/7/17.
 */

public class RestartServiceReceiver extends BroadcastReceiver {
    private static final String TAG = "RestartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        int resultCode = intent.getIntExtra(BackgroundIdleService.EXTRA_RESULT_CODE, 111);
        Intent resultData = intent.getParcelableExtra(BackgroundIdleService.EXTRA_RESULT_INTENT);
        Intent i = new Intent(context.getApplicationContext(), BackgroundIdleService.class);
        i.setAction(Constants.INTENT_ACTION_IDLE);
        i.putExtra(BackgroundIdleService.EXTRA_RESULT_CODE, resultCode);
        i.putExtra(BackgroundIdleService.EXTRA_RESULT_INTENT, resultData);
        context.startService(i);

    }
}
