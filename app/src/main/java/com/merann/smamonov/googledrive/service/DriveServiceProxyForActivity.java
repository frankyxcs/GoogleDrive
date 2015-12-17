package com.merann.smamonov.googledrive.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.merann.smamonov.googledrive.model.Image;

import java.io.File;
import java.util.List;

/**
 * Created by sergeym on 07.12.2015.
 */
public class DriveServiceProxyForActivity extends DriveServiceProxy {

    public interface DriveServiceProxyListener {

        void onConnectionStateChange(boolean isConneted);

        void onNewFileNotification();

        void onFileUploadNotification(String fileName, Boolean isSuccess);
    }

    Activity mActivityContext;
    static private final String LOG_TAG = "DriveServiceProxyForAct";
    static private final int GOOGLE_DRIVE_RESOLUTION_RESULT = 100;
    DriveServiceProxyListener mDriveServiceProxyListener;
    DriveService.DriveServiceBinder mDriveServiceBinder;
    ServiceConnection mServiceConnection;


    public DriveServiceProxyForActivity(Activity activity, DriveServiceProxyListener driveServiceProxyListener) {
        super(activity);
        Log.d(LOG_TAG, "DriveServiceProxy");

        mActivityContext = activity;
        mDriveServiceProxyListener = driveServiceProxyListener;

//        mServiceConnection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                Log.d(LOG_TAG, "onServiceConnected");
//                mDriveServiceBinder = (DriveService.DriveServiceBinder) service;
//
//                mDriveServiceBinder.setListener(new DriveService.DriveServiceBinder.DriveServiceBinderListener() {
//                    @Override
//                    public void onFileUploaded(File file, Boolean isSuccess) {
//                        Log.d(LOG_TAG, "onFileUploaded");
//                        mDriveServiceProxyListener.onFileUploadNotification(file.getPath(), isSuccess);
//                    }
//
//                    @Override
//                    public void onFileListChanged() {
//                        Log.d(LOG_TAG, "onFileListChanged");
//                        mDriveServiceProxyListener.onNewFileNotification();
//                    }
//
//                    @Override
//                    public void onSynchronizationStarted() {
//                        Log.d(LOG_TAG, "onSynchronizationStarted");
//                    }
//
//                    @Override
//                    public void onSynchronisationFinished() {
//                        Log.d(LOG_TAG, "onSynchronisationFinished");
//                    }
//
//                    @Override
//                    public void onConnectedFailed(ConnectionResult connectionResult) {
//                        Log.d(LOG_TAG, "onConnectedFailed");
//                        handleConnectionResolutionRequest(connectionResult);
//                    }
//                });
//
//                mDriveServiceProxyListener.onNewFileNotification();
//
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//                Log.d(LOG_TAG, "onServiceConnected");
//            }
//        };
//
//        Intent bindIntent = new Intent(DriveService.INTEND_STRING);
//
//        mActivityContext.bindService(bindIntent,
//                mServiceConnection,
//                Context.BIND_AUTO_CREATE);
    }

    public void handleAuthenticationRequest(Intent intent) {
        Log.d(LOG_TAG, "handleAuthenticationRequest");
        final ConnectionResult connectionResult = intent.getParcelableExtra(ConnectionResult.class.toString());
        handleConnectionResolutionRequest(connectionResult);
    }

    public void handleConnectionResolutionRequest(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(mActivityContext,
                        GOOGLE_DRIVE_RESOLUTION_RESULT);
            } catch (IntentSender.SendIntentException e) {
                Log.d(LOG_TAG, "REMOTE_DRIVE_CONNECTION_RESOLUTION_REQUEST e" + e.getMessage());
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), mActivityContext, 0).show();
        }
    }

    public boolean onActivityResultHandler(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResultHandler");
        boolean result = false;

        Log.d(LOG_TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);

        switch (requestCode) {
            case GOOGLE_DRIVE_RESOLUTION_RESULT: {
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(LOG_TAG, "onActivityResult GOOGLE_DRIVE_RESOLUTION_RESULT result is RESULT_OK, starting service");
                    mDriveServiceBinder.handleRemoteDriveProblemSolved();
                } else {
                    Log.e(LOG_TAG, "onActivityResult GOOGLE_DRIVE_RESOLUTION_RESULT error resultCode " + resultCode);
                }
                result = true;
                break;
            }

            default:
                break;
        }
        return result;
    }

    public void uploadFile(File file) {
        Log.d(LOG_TAG, "uploadFile: " + file.getName());

        mDriveServiceBinder.uploadFile(file);
    }

    public void doSync() {
        mDriveServiceBinder.doSync();
    }

    public void bind() {
        Log.d(LOG_TAG, "bind");
        super.bind();

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(LOG_TAG, "onServiceConnected");
                mDriveServiceBinder = (DriveService.DriveServiceBinder) service;

                mDriveServiceBinder.setListener(new DriveService.DriveServiceBinder.DriveServiceBinderListener() {
                    @Override
                    public void onFileUploaded(File file, Boolean isSuccess) {
                        Log.d(LOG_TAG, "onFileUploaded");
                        mDriveServiceProxyListener.onFileUploadNotification(file.getPath(), isSuccess);
                    }

                    @Override
                    public void onFileListChanged() {
                        Log.d(LOG_TAG, "onFileListChanged");
                        mDriveServiceProxyListener.onNewFileNotification();
                    }

                    @Override
                    public void onSynchronizationStarted() {
                        Log.d(LOG_TAG, "onSynchronizationStarted");
                    }

                    @Override
                    public void onSynchronisationFinished() {
                        Log.d(LOG_TAG, "onSynchronisationFinished");
                    }

                    @Override
                    public void onConnectedFailed(ConnectionResult connectionResult) {
                        Log.d(LOG_TAG, "onConnectedFailed");
                        handleConnectionResolutionRequest(connectionResult);
                    }
                });

                mDriveServiceProxyListener.onNewFileNotification();

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "onServiceConnected");
            }
        };

        Intent bindIntent = new Intent(DriveService.INTEND_STRING);

        mActivityContext.bindService(bindIntent,
                mServiceConnection,
                Context.BIND_AUTO_CREATE);

    }

    public void unBind() {
        Log.d(LOG_TAG, "unBind");
        super.unBind();
        mActivityContext.unbindService(mServiceConnection);
    }

    public List<Image> getImagesList() {
        return mDriveServiceBinder.getImagesList();
    }

    public void updateConfiguration()
    {
        Log.d(LOG_TAG, "updateConfiguration");
        mDriveServiceBinder.updateConfiguration();
    }
}
