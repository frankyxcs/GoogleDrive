package com.merann.smamonov.googledrive.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by samam_000 on 05.12.2015.
 */
public abstract class ProxyMessageHandler extends MessageReceiver implements IMessageSender {

    static private String LOG_TAG;

    public ProxyMessageHandler(Context context, final String logTag, final String intentString) {
        super(context, logTag, intentString);
        this.LOG_TAG = logTag;
        Log.d(LOG_TAG, "ProxyMessageHandler");
    }

    @Override
    public void sendMessage(Intent intent) {
        Log.d(LOG_TAG, "sendMessage");
        mContext.startService(intent);
    }
}
