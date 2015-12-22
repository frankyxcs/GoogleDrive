package com.merann.smamonov.googledrive.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.merann.smamonov.googledrive.service.DriveServiceProxy;

/**
 * Created by samam_000 on 16.12.2015.
 */
public class DriveBroadcastReceiver extends BroadcastReceiver {

    static private final String LOG_TAG = "DriveBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");
        Log.e(LOG_TAG, "onReceive android.intent.action.BOOT_COMPLETED");
        DriveServiceProxy driveServiceProxy = new DriveServiceProxy(context);
        driveServiceProxy.start();
    }
}
