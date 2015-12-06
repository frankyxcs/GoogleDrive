package com.merann.smamonov.googledrive.service;

import android.content.Intent;

/**
 * Created by samam_000 on 05.12.2015.
 */
public interface IMessageReceiver {
    void bind();

    void unBind();

    void addMessageHandler(Message messageId, IMessageHandler handler);

    Intent createMessage(Message messageId);

//    String getIntendString();

    void sendMessage(Intent intent);

    void dispatchMessage(Intent intent);
}
