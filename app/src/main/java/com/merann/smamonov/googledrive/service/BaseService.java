package com.merann.smamonov.googledrive.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by samam_000 on 05.12.2015.
 */
public class BaseService extends IntentService implements IMessageReceiver, IMessageSender {

    static private String mLogTag;

    ServiceMessageHandler mServiceMessageHandler;

    public BaseService(String logTag, String intentString) {
        super(mLogTag);
        this.mLogTag = logTag;
        mServiceMessageHandler = new ServiceMessageHandler(this,
                mLogTag,
                intentString);
        Log.d(mLogTag, "BaseService");
    }

    @Override
    public void onCreate() {
        Log.d(mLogTag, "onCreate");
        super.onCreate();
        bind();
    }

    @Override
    public void onDestroy() {
        Log.d(mLogTag, "onDestroy");
        super.onDestroy();
        unBind();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(mLogTag, "onHandleIntent");
        mServiceMessageHandler.dispatchMessage(intent);
    }

    @Override
    public void bind() {
        Log.d(mLogTag, "bind");
        mServiceMessageHandler.bind();
    }

    @Override
    public void unBind() {
        Log.d(mLogTag, "unBind");
        mServiceMessageHandler.unBind();
    }

    @Override
    public void addMessageHandler(Message messageId, IMessageHandler handler) {
        mServiceMessageHandler.addMessageHandler(messageId, handler);
    }

    @Override
    public Intent createMessage(Message messageId) {
        Log.d(mLogTag, "createMessage");
        return mServiceMessageHandler.createMessage(messageId);
    }

    @Override
    public void sendMessage(Intent intent) {
        mServiceMessageHandler.sendMessage(intent);
    }

    @Override
    public void dispatchMessage(Intent intent) {
        Log.d(mLogTag, "dispatchMessage");
        mServiceMessageHandler.sendMessage(intent);
    }

    @Override
    public void handleSimpleIntent(Intent intent) {
        //do nothing
    }
}
