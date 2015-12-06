package com.merann.smamonov.googledrive.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.merann.smamonov.googledrive.R;

import java.io.Serializable;

/**
 * Created by samam_000 on 05.12.2015.
 */
public class ConfigurationService extends BaseService {

    static class Configuration implements Serializable {
        int mSyncPeriod;
        String mFolderName;

        public String getFolderName() {
            return mFolderName;
        }

        public void setFolderName(String folderName) {
            this.mFolderName = folderName;
        }

        public int getSyncPeriod() {
            return mSyncPeriod;
        }

        public void setSyncPeriod(int syncPeriod) {
            this.mSyncPeriod = syncPeriod;
        }

        public Configuration(String folderName, int syncPeriod) {
            this.mFolderName = folderName;
            this.mSyncPeriod = syncPeriod;
        }
    }

    static public final String INTEND_STRING = "com.merann.smamonov.googledrive.ConfigurationService";
    static private final String LOG_TAG = "ConfigurationService";
    static private String PREFERENCE_FOLDER_NAME = "PREFERENCE_FOLDER_NAME";
    static private String PREFERENCE_DEFAULT_FOLDER_NAME = "Sergey";
    static private String PREFERENCE_SYNC_PERIOD = "PREFERENCE_SYNC_PERIOD";

    private Configuration mCurrentConfiguration;

    public ConfigurationService()
    {
        super(INTEND_STRING);
        Log.d(LOG_TAG, "ConfigurationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");

        addMessageHandler(Message.GET_CONFIGURATION_REQUEST, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "GET_CONFIGURATION_REQUEST");
                Configuration configuration = getConfiguration();
                sendMessage(createMessage(Message.GET_CONFIGURATION_RESPONSE)
                        .putExtra(ConfigurationService.Configuration.class.getName(),
                                configuration));

            }
        });

        addMessageHandler(Message.UPDATE_CONFIGURATION_REQUEST, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "UPDATE_CONFIGURATION_REQUEST");
                Configuration configuration = (Configuration) intent.getSerializableExtra(Configuration.class.getName());
                updateConfiguration(configuration);
                sendMessage(createMessage(Message.UPDATE_CONFIGURATION_RESPONSE)
                        .putExtra(ConfigurationService.Configuration.class.getName(),
                                configuration));
            }
        });
    }

    public Configuration getConfiguration() {
        Log.d(LOG_TAG, "getConfiguration");
        if (mCurrentConfiguration == null) {
            readConfiguration();
        }
        return mCurrentConfiguration;
    }

    public void updateConfiguration(Configuration configuration) {
        Log.d(LOG_TAG, "updateConfiguration");
        mCurrentConfiguration = configuration;
        saveConfiguration();
    }

    public void saveConfiguration() {
        Log.d(LOG_TAG, "saveConfiguration");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferecesEditor = preferences.edit();

        preferecesEditor.putString(PREFERENCE_FOLDER_NAME, mCurrentConfiguration.getFolderName());

        preferecesEditor.putInt(PREFERENCE_SYNC_PERIOD, mCurrentConfiguration.getSyncPeriod());
        preferecesEditor.commit();
    }

    private Configuration readConfiguration() {
        Log.d(LOG_TAG, "readConfiguration");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String folderName = preferences.getString(PREFERENCE_FOLDER_NAME, PREFERENCE_DEFAULT_FOLDER_NAME);

        final int[] periods = getBaseContext().getResources().getIntArray(R.array.sync_periods);
        int period = preferences.getInt(PREFERENCE_SYNC_PERIOD, periods[0]);

        mCurrentConfiguration = new Configuration(folderName, period);
        return mCurrentConfiguration;
    }
}
