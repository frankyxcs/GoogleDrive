package com.merann.smamonov.googledrive.service;

import android.content.Context;
import android.util.Log;

import com.merann.smamonov.googledrive.model.Configuration;

/**
 * Created by sergeym on 03.12.2015.
 */
public class DriveServiceProxy extends ProxyMessageHandler {

    static private final String LOG_TAG = "DriveServiceProxy";

    public DriveServiceProxy(Context context) {
        super(context, LOG_TAG, DriveService.INTEND_STRING);
        Log.d(LOG_TAG, "DriveServiceProxy");
        mContext = context;
    }

    public void bind() {
        Log.d(LOG_TAG, "bind");
        super.bind();
    }

    public void unBind() {
        Log.d(LOG_TAG, "unBind");
        super.unBind();
    }

    public void start()
    {
        Log.d(LOG_TAG, "start");
        sendMessage(createMessage(Message.REMOTE_DRIVE_START));
    }
}
