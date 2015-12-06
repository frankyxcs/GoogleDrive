package com.merann.smamonov.googledrive.service;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by samam_000 on 05.12.2015.
 */
public class BaseService extends IntentService implements IMessageReceiver, IMessageSender {

    static private final String LOG_TAG = "BaseService";

    ServiceMessageHandler mServiceMessageHandler;

    public BaseService(String intentString) {
        super(LOG_TAG);
        mServiceMessageHandler = new ServiceMessageHandler(this, LOG_TAG, intentString);
        Log.d(LOG_TAG, "BaseService");
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate();
        bind();
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
        unBind();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent");
        mServiceMessageHandler.dispatchMessage(intent);
    }

    @Override
    public void bind() {
        Log.d(LOG_TAG, "bind");
        mServiceMessageHandler.bind();
    }

    @Override
    public void unBind() {
        Log.d(LOG_TAG, "unBind");
        mServiceMessageHandler.unBind();
    }

    @Override
    public void addMessageHandler(Message messageId, IMessageHandler handler) {
        mServiceMessageHandler.addMessageHandler(messageId, handler);
    }

    @Override
    public Intent createMessage(Message messageId) {
        Log.d(LOG_TAG, "createMessage");
        return mServiceMessageHandler.createMessage(messageId);
    }

    @Override
    public void sendMessage(Intent intent) {
        Log.d(LOG_TAG, "sendMessage");
        mServiceMessageHandler.sendMessage(intent);
    }

    @Override
    public void dispatchMessage(Intent intent) {
        Log.d(LOG_TAG, "dispatchMessage");
        mServiceMessageHandler.sendMessage(intent);
    }
}
