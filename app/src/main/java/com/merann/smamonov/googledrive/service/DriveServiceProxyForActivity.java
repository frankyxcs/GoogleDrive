package com.merann.smamonov.googledrive.service;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.File;

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

    public DriveServiceProxyForActivity(Activity activity, DriveServiceProxyListener driveServiceProxyListener) {
        super(activity);

        Log.d(LOG_TAG, "DriveServiceProxy");

        mActivityContext = activity;
        this.mDriveServiceProxyListener = driveServiceProxyListener;
//
//        addMessageHandler(Message.REMOTE_DRIVE_DISCONNECT_NOTIFICATION, new IMessageHandler() {
//            @Override
//            public void onIntent(Intent intent) {
//                Log.d(LOG_TAG, "REMOTE_DRIVE_DISCONNECT_NOTIFICATION");
//                mDriveServiceProxyListener.onConnectionStateChange(false);
//            }
//        });
//
//        addMessageHandler(Message.REMOTE_DRIVE_CONNECT_NOTIFICATION, new IMessageHandler() {
//            @Override
//            public void onIntent(Intent intent) {
//                Log.d(LOG_TAG, "REMOTE_DRIVE_CONNECT_NOTIFICATION");
//                mDriveServiceProxyListener.onConnectionStateChange(true);
//            }
//        });
//
        addMessageHandler(Message.REMOTE_DRIVE_AUTHENTICATION_PERFORM_REQUEST, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "REMOTE_DRIVE_AUTHENTICATION_PERFORM_REQUEST");
                handleAuthenticationRequest(intent);
            }
        });

        addMessageHandler(Message.REMOTE_DRIVE_NEW_FILE_NOTIFY, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "REMOTE_DRIVE_NEW_FILE_NOTIFY");
                mDriveServiceProxyListener.onNewFileNotification();
            }
        });
//
//        addMessageHandler(Message.REMOTE_DRIVE_UPLOAD_FILE_RESPONSE, new IMessageHandler() {
//            @Override
//            public void onIntent(Intent intent) {
//                Log.d(LOG_TAG, "REMOTE_DRIVE_UPLOAD_FILE_RESPONSE");
//                String fileName = (String)intent.getSerializableExtra(String.class.getName());
//                Boolean isSuccess = (Boolean)intent.getSerializableExtra(Boolean.class.getName());
//                mDriveServiceProxyListener.onFileUploadNotification(fileName, isSuccess);
//
//            }
//        });
    }

    public void handleAuthenticationRequest(Intent intent) {
        Log.d(LOG_TAG, "handleAuthenticationRequest");
        final ConnectionResult connectionResult = intent.getParcelableExtra(ConnectionResult.class.toString());
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(mActivityContext,
                        GOOGLE_DRIVE_RESOLUTION_RESULT);
            } catch (IntentSender.SendIntentException e) {
                Log.d(LOG_TAG, "REMOTE_DRIVE_AUTHENTICATION_PERFORM_REQUEST e" + e.getMessage());
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
                    sendMessage(createMessage(Message.REMOTE_DRIVE_AUTHENTICATION_PERFORM_RESPONSE));
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
        sendMessage(createMessage(Message.REMOTE_DRIVE_UPLOAD_FILE_REQUEST)
                .putExtra(File.class.getName(), file));
    }
}
