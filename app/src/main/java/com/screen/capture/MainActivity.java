package com.screen.capture;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.screen.capture.service.BackgroundIdleService;
import com.screen.capture.service.RestartServiceReceiver;
import com.screen.capture.util.Common;
import com.screen.capture.util.Constants;
import com.screen.capture.util.PrefUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int CLEANUP_JOB_ID = 43;
    private Button btn_capture;
    private MediaProjectionManager mediaProjectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 111);
            } else {
                checkDrawOverlayPermission();
            }
        } else {
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 111);
        }

    }

    public void checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps */
        if (!Settings.canDrawOverlays(MainActivity.this)) {
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            /** request permission via start activity for result */
            startActivityForResult(intent, 101);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /** check if received result code
         is equal our requested code for draw permission  */
        if (requestCode == 101) {

            if (Settings.canDrawOverlays(this)) {
                startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 111);
            }
        } else if (requestCode == 111) {

            Intent i = new Intent(this, RestartServiceReceiver.class);
            i.setAction(Constants.INTENT_ACTION_IDLE);
            i.putExtra(BackgroundIdleService.EXTRA_RESULT_CODE, resultCode);
            i.putExtra(BackgroundIdleService.EXTRA_RESULT_INTENT, data);
            sendBroadcast(i);
            finish();
        }

    }
}
