package com.merann.smamonov.googledrive.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.merann.smamonov.googledrive.R;
import com.merann.smamonov.googledrive.model.Configuration;


/**
 * Created by samam_000 on 15.12.2015.
 */
public class ConfigurationManager {
    static private final String LOG_TAG = "ConfigurationManager";

    static private String PREFERENCE_FOLDER_NAME = "PREFERENCE_FOLDER_NAME";
    static private String PREFERENCE_DEFAULT_FOLDER_NAME = "Sergey";
    static private String PREFERENCE_SYNC_PERIOD = "PREFERENCE_SYNC_PERIOD";

    Context mContext;

    public ConfigurationManager(Context context) {
        Log.d(LOG_TAG, "ConfigurationManager");
        mContext = context;
    }

    public Configuration getConfiguration() {
        Log.d(LOG_TAG, "getConfiguration");
        return readConfiguration();
    }

    public void updateConfiguration(Configuration configuration) {
        Log.d(LOG_TAG, "updateConfiguration");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(PREFERENCE_FOLDER_NAME, configuration.getFolderName());
        edit.putInt(PREFERENCE_SYNC_PERIOD, configuration.getSyncPeriod());
        edit.commit();
    }

    private Configuration readConfiguration() {
        Log.d(LOG_TAG, "readConfiguration");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String folderName = preferences.getString(PREFERENCE_FOLDER_NAME, PREFERENCE_DEFAULT_FOLDER_NAME);

        final int[] periods = mContext.getResources().getIntArray(R.array.sync_periods);
        int period = preferences.getInt(PREFERENCE_SYNC_PERIOD, periods[0]);

        return new Configuration(folderName, period);
    }
}
