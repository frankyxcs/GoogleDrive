package com.merann.smamonov.googledrive.service;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by sergeym on 03.12.2015.
 */
public class DriveServiceProxy extends ProxyMessageHandler {

    public interface OnConnectionStateChangeListener {
        void onConnectionStateChange(boolean isConneted);
    }

    interface OnNewFileDetectedListener {
        void onNewFileDetectedListener();
    }

    private Activity mContext;
    OnConnectionStateChangeListener mOnConnectionStateChangeListener;

    static private final String INTEND_STRING = "com.merann.smamonov.googledrive.DriveService";
    static private final String LOG_TAG = "DriveServiceProxy";
    static private final int GOOGLE_DRIVE_RESOLUTION_RESULT = 100;

    ConfigurationServiceProxy mConfigurationServiceProxy;

    public DriveServiceProxy(Activity context, OnConnectionStateChangeListener onConnectionStateChangeListener) {
        super(context, LOG_TAG, DriveService.INTEND_STRING);
        mContext = context;
        this.mOnConnectionStateChangeListener = onConnectionStateChangeListener;
        Log.d(LOG_TAG, "DriveServiceProxy");

        addMessageHandler(Message.AUTHENTICATION_PERFORM_REQUEST, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "AUTHENTICATION_PERFORM_REQUEST");
                handleAuthenticationRequest(intent);
            }
        });

        addMessageHandler(Message.REMOTE_DRIVE_DISCONNECT_NOTIFICATION, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "REMOTE_DRIVE_DISCONNECT_NOTIFICATION");
                mOnConnectionStateChangeListener.onConnectionStateChange(false);
            }
        });

        addMessageHandler(Message.REMOTE_DRIVE_CONNECT_NOTIFICATION, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "REMOTE_DRIVE_CONNECT_NOTIFICATION");
                mOnConnectionStateChangeListener.onConnectionStateChange(true);
            }
        });

        mConfigurationServiceProxy = new ConfigurationServiceProxy(mContext,
                new ConfigurationServiceProxy.GetConfigurationListener() {
                    @Override
                    public void onGetConfiguration(String folderName, int syncTime) {

                    }
                }
                , null, null);
    }

    public void bind() {
        Log.d(LOG_TAG, "bind");
        super.bind();
        mConfigurationServiceProxy.bind();
    }

    public void unBind() {
        Log.d(LOG_TAG, "unBind");
        super.unBind();
        mConfigurationServiceProxy.unBind();
    }

    /* business logic */
    private void handleAuthenticationRequest(Intent intent) {
        Log.d(LOG_TAG, "handleAuthenticationRequest");
        final ConnectionResult connectionResult = intent.getParcelableExtra(ConnectionResult.class.toString());
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(mContext,
                        GOOGLE_DRIVE_RESOLUTION_RESULT);
            } catch (IntentSender.SendIntentException e) {
                Log.d(LOG_TAG, "AUTHENTICATION_PERFORM_REQUEST e" + e.getMessage());
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), mContext, 0).show();
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
                    sendMessage(createMessage(Message.AUTHENTICATION_PERFORM_RESPONSE));
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

    public void connect() {
        Log.d(LOG_TAG, "connect");
        sendMessage(createMessage(Message.REMOTE_DRIVE_CONNECT_REQUEST));
    }
}
