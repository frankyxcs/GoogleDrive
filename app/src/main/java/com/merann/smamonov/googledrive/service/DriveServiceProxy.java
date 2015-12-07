package com.merann.smamonov.googledrive.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

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

    OnConnectionStateChangeListener mOnConnectionStateChangeListener;

    static private final String LOG_TAG = "DriveServiceProxy";

    public DriveServiceProxy(Context context, OnConnectionStateChangeListener onConnectionStateChangeListener) {
        super(context, LOG_TAG, DriveService.INTEND_STRING);
        mContext = context;
        this.mOnConnectionStateChangeListener = onConnectionStateChangeListener;
        Log.d(LOG_TAG, "DriveServiceProxy");

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
    }

    public void bind() {
        Log.d(LOG_TAG, "bind");
        super.bind();
    }

    public void unBind() {
        Log.d(LOG_TAG, "unBind");
        super.unBind();
    }

    public void connect() {
        Log.d(LOG_TAG, "connect");
        sendMessage(createMessage(Message.REMOTE_DRIVE_CONNECT_REQUEST));
    }

    /* business logic */
    public void handleConfigurationUpdate(ConfigurationService.Configuration newConfiguration) {
        Log.d(LOG_TAG, "handleConfigurationUpdate");
        sendMessage(createMessage(Message.REMOTE_DRIVE_CONFIGURATION_UPDATE_NOTIFICATION)
                .putExtra(ConfigurationService.Configuration.class.getName(), newConfiguration));
    }
}
