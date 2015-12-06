package com.merann.smamonov.googledrive.service;

import android.content.Context;
import android.content.Intent;

/**
 * Created by samam_000 on 05.12.2015.
 */
public class ServiceMessageHandler extends MessageReceiver implements IMessageSender {
    Context mContext;
    static private String LOG_TAG;

    public ServiceMessageHandler(Context context, final String logTag, final String intendString)
    {
        super(context, logTag, intendString);
        this.mContext = context;
        this.LOG_TAG = logTag;;
    }

    @Override
    public void sendMessage(Intent intent) {
        mContext.sendBroadcast(intent);
    }
}
