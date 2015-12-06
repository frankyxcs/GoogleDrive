package com.merann.smamonov.googledrive.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.merann.smamonov.googledrive.R;
import com.merann.smamonov.googledrive.service.ConfigurationServiceProxy;

public class SettingsActivity extends AppCompatActivity {

    public final String LOG_TAG = "GoogleDriveSettingsAct";

    //    private int mSyncPeriod;
    private SeekBar mSyncSeekBar;
    private TextView mSyncPeriodText;
    private EditText mFolderEditText;
    private ConfigurationServiceProxy mConfigurationServiceProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /* set up of configuration service proxy */
        mConfigurationServiceProxy = new ConfigurationServiceProxy(this,
                new ConfigurationServiceProxy.GetConfigurationListener() {
                    @Override
                    public void onGetConfiguration(String folderName, int syncTime) {
                        Log.d(LOG_TAG, "onGetConfiguration");
                        updateSyncPeriodFromService(syncTime);
                        updateFolderNameFromService(folderName);
                    }
                },
                new ConfigurationServiceProxy.UpdateConfigurationListener() {
                    @Override
                    public void onUpdateConfiguration(String folderName, int syncTime) {
                        Log.d(LOG_TAG, "onUpdateConfiguration: folderName:" + folderName + " syncTime:" + syncTime);
                        Toast.makeText(SettingsActivity.this, "onUpdateConfiguration: folderName:" + folderName + " syncTime:" + syncTime, 100).show();
                    }
                },
                new ConfigurationServiceProxy.CacheCleanCacheListener() {
                    @Override
                    public void onCacheClean() {
                        Log.d(LOG_TAG, "onCacheClean");
                        Toast.makeText(SettingsActivity.this, "onCacheClean", 100).show();
                    }
                });

                /* set up of GUI elements */
        mSyncSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mFolderEditText = (EditText) findViewById(R.id.user_folder_name);

        final int[] periods = getResources().getIntArray(R.array.sync_periods);
        mSyncSeekBar.setMax(periods.length - 1);
        mSyncPeriodText = (TextView) findViewById(R.id.syncPeriodText);

        mSyncSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(LOG_TAG, "onProgressChanged progress:" + progress);
                updateSyncPeriodFromUI(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(LOG_TAG, "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(LOG_TAG, "onStopTrackingTouch");
            }
        });

        final Button applyConfigurationButton = (Button) findViewById(R.id.applyConfigurationButton);

        applyConfigurationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfigurationSettings();
            }
        });
    }

    private void updateSyncPeriodFromUI(int periodIndex) {
        Log.d(LOG_TAG, "updateSyncPeriodFromUI periodIndex:" + periodIndex);
        final int[] periods = getResources().getIntArray(R.array.sync_periods);

        int syncPeriod = periods[periodIndex];

        mSyncPeriodText.setText(syncPeriod + "min.");
    }

    private void updateSyncPeriodFromService(int syncPeriod) {
        Log.d(LOG_TAG, "updateSyncPeriodFromService syncPeriod:" + syncPeriod);
        final int[] periods = getResources().getIntArray(R.array.sync_periods);

        int period_index;
        for (period_index = 0;
             period_index < periods.length;
             period_index++) {
            if (periods[period_index] == syncPeriod) {
                break;
            }
        }

        mSyncSeekBar.setProgress(period_index);
        mSyncPeriodText.setText(syncPeriod + "min.");
    }

    private void updateFolderNameFromService(String folderName) {
        Log.d(LOG_TAG, "updateFolderNameFromService");
        mFolderEditText.setText(folderName);
    }


    private void readConfiguration() {
        mConfigurationServiceProxy.getConfiguration();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mConfigurationServiceProxy.bind();
        readConfiguration();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mConfigurationServiceProxy.unBind();
    }

    void saveConfigurationSettings() {
        final int[] periods = getResources().getIntArray(R.array.sync_periods);

        String folderName = mFolderEditText.getText().toString();
        int syncPeriod = periods[mSyncSeekBar.getProgress()];
        mConfigurationServiceProxy.setConfiguration(folderName,
                syncPeriod);
    }
}
