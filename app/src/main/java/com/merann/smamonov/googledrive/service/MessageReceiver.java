package com.merann.smamonov.googledrive.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.merann.smamonov.googledrive.R;

import java.util.HashMap;

/**
 * Created by samam_000 on 05.12.2015.
 */
public abstract class MessageReceiver implements IMessageReceiver {

    protected Context mContext;
    private BroadcastReceiver mBroadcastReceiver;
    private String LOG_TAG;
    private String mIntentString;

    HashMap<Message, IMessageHandler> mCommandHandlers = new HashMap();

    public MessageReceiver(Context context, final String logTag, final String intentString)
    {
        this.LOG_TAG = logTag;
        mContext = context;
        mIntentString = intentString;
        Log.d(LOG_TAG, "MessageListener");
    }

    @Override
    public void bind()
    {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(LOG_TAG, "onReceive");
                dispatchMessage(intent);
            }
        };


        Log.d(LOG_TAG, "bind mIntentString:" + mIntentString);
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
        Log.d(LOG_TAG, "addHandler: messageId[" + messageId + "]");
        mCommandHandlers.put(messageId, handler);
    }

    @Override
    public void dispatchMessage(Intent intent) {
        Log.d(LOG_TAG, "dispatchIntent");

        if (intent != null) {
            Message messageId = (Message)intent.getSerializableExtra(Message.class.getName());

            if (mCommandHandlers.containsKey(messageId)) {
                mCommandHandlers.get(messageId).onIntent(intent);
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
        Log.d(LOG_TAG, "createMessage:" + mIntentString);
        return new Intent(getIntendString())
                .putExtra(Message.class.getName(), messageId);
    }

    public String getIntendString() {
        return mIntentString;
    }
}
