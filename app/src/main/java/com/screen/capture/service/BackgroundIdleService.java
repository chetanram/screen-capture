package com.screen.capture.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.screen.capture.util.Common;
import com.screen.capture.util.Constants;
import com.screen.capture.util.PrefUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by chetan on 12/7/17.
 */

public class BackgroundIdleService extends Service implements LinearLayout.OnTouchListener, KeyguardManager.OnKeyguardExitResult {

    public static final String EXTRA_RESULT_CODE = "resultCode";
    public static final String EXTRA_RESULT_INTENT = "resultIntent";
    private Handler mHandler;
    private Runnable mRunnable;
    private final int mTimerDelay = 10000;//inactivity delay in milliseconds
    private LinearLayout mTouchLayout;//the transparent view
    public static int resultCode;
    public static Intent resultData;
    private MediaProjectionManager mgr;
    private WindowManager windowManager;
    final private HandlerThread handlerThread =
            new HandlerThread(getClass().getSimpleName(),
                    android.os.Process.THREAD_PRIORITY_BACKGROUND);
    private Handler handler;
    private MediaProjection projection;
    private VirtualDisplay vdisplay;
    private ImageTransmogrifier it;
    static final int VIRT_DISPLAY_FLAGS =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

    Handler getHandler() {
        return (handler);
    }

    String fileName = "";
    private KeyguardManager keyguardManager;
    private KeyguardManager.KeyguardLock lock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*mTouchLayout = new LinearLayout(this);
        mTouchLayout.setDrawingCacheEnabled(true);
        mTouchLayout.buildDrawingCache(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mTouchLayout.setLayoutParams(lp);

        // set on touch listener
        mTouchLayout.setOnTouchListener(this);
        */
        // fetch window manager object
        mgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        keyguardManager = ((KeyguardManager) getSystemService(KEYGUARD_SERVICE));
        // set layout parameter of window manager
/*
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
        );
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        windowManager.addView(mTouchLayout, mParams);*/
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

    }

/*
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceTask = new Intent(getApplicationContext(), this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceTask, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + mTimerDelay,
                restartPendingIntent);
        Log.d("IdleDetectorService", "On Removed");
        startService(rootIntent);
        super.onTaskRemoved(rootIntent);
    }
*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d("IdleDetectorService", "On StartCommand");
        if (intent != null) {
            resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 111);
            resultData = intent.getParcelableExtra(EXTRA_RESULT_INTENT);
        } else {

            resultCode = -1;
            resultData = null;

        }
        initTimer();
        return START_STICKY;
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
//        Log.d("IdleDetectorService", "Touch detected. Resetting timer");
        PrefUtils.saveLong(getApplicationContext(), Constants.PREF_LONG_INACTIVE, System.currentTimeMillis());
//        initTimer();
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCapture();
        mHandler.removeCallbacks(mRunnable);
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (windowManager != null && mTouchLayout != null) {
            windowManager.removeView(mTouchLayout);
        }
        Intent i = new Intent(this, RestartServiceReceiver.class);
        i.setAction(Constants.INTENT_ACTION_IDLE);
//        i.putExtra(BackgroundIdleService.EXTRA_RESULT_CODE, resultCode);
//        i.putExtra(BackgroundIdleService.EXTRA_RESULT_INTENT, resultData);
        sendBroadcast(i);
    }

    /**
     * (Re)sets the timer to send the inactivity broadcast
     */
    private void initTimer() {
        // Start timer and timer task
        if (mRunnable == null) {

            mRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d("chetan", "service running");
//                    Common.showToast(getApplicationContext(),"service running");
                    try {
                        boolean isInForeground = new ForegroundCheckTask().execute(getApplicationContext()).get();

                        if (resultData != null) {
                            fileName = "img_" + System.currentTimeMillis() + ".png";
                            startCapture();
                        } else {
                            Intent launchIntent = getApplication()
                                    .getPackageManager()
                                    .getLaunchIntentForPackage(getApplicationContext().getPackageName());
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                            getApplication().startActivity(launchIntent);
                        }
//                        stopSelf();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mHandler.postDelayed(this, mTimerDelay);
                }

            };
        }

        if (mHandler == null) {
            mHandler = new Handler();
        }

        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, mTimerDelay);
    }

    private void stopCapture() {
        if (projection != null) {
            projection.stop();
            vdisplay.release();
            projection = null;
        }
    }

    private void startCapture() {
        projection = mgr.getMediaProjection(resultCode, resultData);
        it = new ImageTransmogrifier(this);

        MediaProjection.Callback cb = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                vdisplay.release();
            }
        };

        vdisplay = projection.createVirtualDisplay("andshooter",
                it.getWidth(), it.getHeight(),
                getResources().getDisplayMetrics().densityDpi,
                VIRT_DISPLAY_FLAGS, it.getSurface(), null, handler);
        projection.registerCallback(cb, handler);
    }

    void processImage(final byte[] png) {
        new Thread() {
            @Override
            public void run() {
                File output = new File(Environment.getExternalStorageDirectory() + "/chetan",
                        fileName);

                try {
                    FileOutputStream fos = new FileOutputStream(output);

                    fos.write(png);
                    fos.flush();
                    fos.getFD().sync();
                    fos.close();

                    MediaScannerConnection.scanFile(BackgroundIdleService.this,
                            new String[]{output.getAbsolutePath()},
                            new String[]{"image/png"},
                            null);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Exception writing out screenshot", e);
                }
                stopCapture();
            }
        }.start();
        lock();
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public void lock() {
        if (keyguardManager.isKeyguardSecure()) {

        } else {
            Common.showToast(getApplicationContext(), "Please Setup Device Screen Lock Security");
        }
        if (!keyguardManager.isKeyguardLocked()) {
            lock = keyguardManager.newKeyguardLock("CustomLock");
            lock.disableKeyguard();
            keyguardManager.exitKeyguardSecurely(this);
        }
    }

    @Override
    public void onKeyguardExitResult(boolean success) {
        if (success) {
            lock.reenableKeyguard();
        } else {
            lock.reenableKeyguard();
            lock = keyguardManager.newKeyguardLock("CustomLock");
            lock.disableKeyguard();
            keyguardManager.exitKeyguardSecurely(this);
        }
    }

    private class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0];
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = null;

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                appProcesses = activityManager.getRunningAppProcesses();
            } else {
                //for devices with Android 5+ use alternative methods
//                appProcesses = Process.getRunningAppProcessInfo(getApplication());
                appProcesses = activityManager.getRunningAppProcesses();
            }

            if (appProcesses == null) {
                return false;
            }

            final String packageName = context.getPackageName();

            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                        appProcess.processName.equals(packageName)) {
                    return true;
                }
            }

            return false;
        }
    }

}
