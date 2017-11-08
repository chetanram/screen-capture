package com.screen.capture;

import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.screen.capture.util.Constants;
import com.screen.capture.util.PrefUtils;

/**
 * Created by chetan on 16/10/17.
 */

public class MyApplication extends MultiDexApplication {
    private static MyApplication myApplication;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        context = this;
//        new GetNotification().execute();
        PrefUtils.saveLong(context, Constants.PREF_LONG_INACTIVE,System.currentTimeMillis());
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    @Override
    public void onTerminate() {
        super.onTerminate();

        Log.d("IdleDetectorService","App terminate");

    }

    public static MyApplication getInstance() {
        return myApplication;
    }


}
