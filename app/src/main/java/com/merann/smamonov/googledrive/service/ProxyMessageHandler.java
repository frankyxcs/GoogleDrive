package com.merann.smamonov.googledrive.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by samam_000 on 05.12.2015.
 */
public abstract class ProxyMessageHandler extends MessageReceiver implements IMessageSender {

    static private String mLogTag;

    public ProxyMessageHandler(Context context, final String logTag, final String intentString) {
        super(context, logTag, intentString);
        this.mLogTag = logTag;
        Log.d(mLogTag, "ProxyMessageHandler");
    }

    @Override
    public void sendMessage(Intent intent) {
        Log.d(mLogTag, "sendMessage: " + intent.getSerializableExtra(Message.class.getName()));
        mContext.startService(intent);
    }
}
