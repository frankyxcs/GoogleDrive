package com.merann.smamonov.googledrive.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by samam_000 on 05.12.2015.
 */
public class ConfigurationServiceProxy extends ProxyMessageHandler {

    public interface GetConfigurationListener {
        void onGetConfiguration(String folderName, int syncTime);
    }

    public interface UpdateConfigurationListener {
        void onUpdateConfiguration(String folderName, int syncTime);
    }

    public interface CacheCleanCacheListener {
        void onCacheClean();
    }

    static private final String LOG_TAG = "ConfServiceProxy";

    public ConfigurationServiceProxy(final Context context,
                                     final GetConfigurationListener getConfigurationListener,
                                     final UpdateConfigurationListener updateConfigurationListener,
                                     final CacheCleanCacheListener cacheCleanCacheListener) {

        super(context, LOG_TAG, ConfigurationService.INTEND_STRING);

        addMessageHandler(Message.GET_CONFIGURATION_RESPONSE, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "GET_CONFIGURATION_RESPONSE");
                ConfigurationService.Configuration configuration = (ConfigurationService.Configuration) intent
                        .getSerializableExtra(ConfigurationService
                                .Configuration
                                .class
                                .getName());
                getConfigurationListener.onGetConfiguration(configuration.getFolderName(),
                        configuration.getSyncPeriod());
            }
        });
        addMessageHandler(Message.UPDATE_CONFIGURATION_RESPONSE, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "UPDATE_CONFIGURATION_RESPONSE");
                ConfigurationService.Configuration configuration = (ConfigurationService.Configuration) intent
                        .getSerializableExtra(ConfigurationService
                                .Configuration
                                .class
                                .getName());
                updateConfigurationListener.onUpdateConfiguration(configuration.getFolderName(),
                        configuration.getSyncPeriod());
            }
        });
    }

    public void getConfiguration() {
        Log.d(LOG_TAG, "getConfiguration");
        sendMessage(createMessage(Message.GET_CONFIGURATION_REQUEST));
    }

    public void setConfiguration(String folderName, int syncTime) {
        Log.d(LOG_TAG, "setConfiguration");
        ConfigurationService.Configuration configuration = new ConfigurationService.Configuration(folderName, syncTime);
        sendMessage(createMessage(Message.UPDATE_CONFIGURATION_REQUEST)
                .putExtra(ConfigurationService
                                .Configuration
                                .class
                                .getName(),
                        configuration));
    }

    public void bind() {
        Log.d(LOG_TAG, "bind");
        super.bind();
    }

    public void unBind() {
        Log.d(LOG_TAG, "unBind");
        super.unBind();
    }
}
