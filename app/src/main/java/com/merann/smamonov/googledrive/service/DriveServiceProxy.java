package com.merann.smamonov.googledrive.service;

import android.content.Context;
import android.util.Log;

/**
 * Created by sergeym on 03.12.2015.
 */
public class DriveServiceProxy extends ProxyMessageHandler {

    static private final String LOG_TAG = "DriveServiceProxy";

    public DriveServiceProxy(Context context) {
        super(context, LOG_TAG, DriveService.INTEND_STRING);
        Log.d(LOG_TAG, "DriveServiceProxy");
        mContext = context;
        Log.d(LOG_TAG, "DriveServiceProxy");
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

    public void handleConnectionEstablished() {
        Log.d(LOG_TAG, "handleConnectionEstablished");
        sendMessage(createMessage(Message.REMOTE_DRIVE_LOAD_FILES_REQUEST));
    }

    public void handleNewFile() {
        Log.d(LOG_TAG, "handleNewFile");
        sendMessage(createMessage(Message.REMOTE_DRIVE_NEW_FILE_NOTIFY));
    }
}
