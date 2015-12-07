package com.merann.smamonov.googledrive.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by samam_000 on 05.12.2015.
 */
public class ServiceMessageHandler extends MessageReceiver implements IMessageSender {
    Context mContext;
    static private String mLogTag;

    public ServiceMessageHandler(Context context, final String logTag, final String intendString)
    {
        super(context, logTag, intendString);
        this.mContext = context;
        this.mLogTag = logTag;;
    }

    @Override
    public void sendMessage(Intent intent) {
        Log.d(mLogTag, "sendMessage: " + intent.getSerializableExtra(Message.class.getName()));
        mContext.sendBroadcast(intent);
    }
}
