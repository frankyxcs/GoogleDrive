package com.merann.smamonov.googledrive.service;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by sergeym on 07.12.2015.
 */
public class DriveServiceProxyForActivity extends DriveServiceProxy {

    Activity mActivityContext;
    static private final String LOG_TAG = "DriveServiceProxyForAct";
    static private final int GOOGLE_DRIVE_RESOLUTION_RESULT = 100;

    public DriveServiceProxyForActivity(Activity activity, OnConnectionStateChangeListener onConnectionStateChangeListener) {
        super(activity, onConnectionStateChangeListener);
        mActivityContext = activity;

        addMessageHandler(Message.REMOTE_DRIVE_AUTHENTICATION_PERFORM_REQUEST, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "REMOTE_DRIVE_AUTHENTICATION_PERFORM_REQUEST");
                handleAuthenticationRequest(intent);
            }
        });
    }

    private void handleAuthenticationRequest(Intent intent) {
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


}
