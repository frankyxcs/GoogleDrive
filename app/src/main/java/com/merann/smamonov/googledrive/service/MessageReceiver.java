package com.merann.smamonov.googledrive.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by samam_000 on 05.12.2015.
 */
public abstract class MessageReceiver implements IMessageReceiver {

    protected Context mContext;
    private BroadcastReceiver mBroadcastReceiver;
    private String mLogTag;
    private String mIntentString;

    HashMap<Message, IMessageHandler> mCommandHandlers = new HashMap();

    public MessageReceiver(Context context, final String logTag, final String intentString)
    {
        this.mLogTag = logTag;
        mContext = context;
        mIntentString = intentString;
        Log.d(mLogTag, "MessageListener");
    }

    @Override
    public void bind()
    {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(mLogTag, "onReceive");
                dispatchMessage(intent);
            }
        };


        Log.d(mLogTag, "bind mIntentString:" + mIntentString);
        IntentFilter onDriveNotificationIntendFilter = new IntentFilter(mIntentString);
        mContext.registerReceiver(mBroadcastReceiver, onDriveNotificationIntendFilter);
    }

    @Override
    public void unBind()
    {
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void addMessageHandler(Message messageId, IMessageHandler handler) {
        mCommandHandlers.put(messageId, handler);
    }

    @Override
    public void dispatchMessage(Intent intent) {
        Log.d(mLogTag, "dispatchIntent");

        if (intent != null) {
            Message messageId = (Message)intent.getSerializableExtra(Message.class.getName());

            if (messageId != null) {
                if (mCommandHandlers.containsKey(messageId)) {
                    mCommandHandlers.get(messageId).onIntent(intent);
                }
            }
            else
            {
                Log.e(mLogTag, "Intent has no messageId filed");
            }
        }
    }

    @Override
    public void sendMessage(Intent intent)
    {
        mContext.sendBroadcast(intent);
    }

    @Override
    public Intent createMessage(Message messageId) {
        Log.d(mLogTag, "createMessage:" + mIntentString);
        return new Intent(getIntendString())
                .putExtra(Message.class.getName(), messageId);
    }

    public String getIntendString() {
        return mIntentString;
    }
}
