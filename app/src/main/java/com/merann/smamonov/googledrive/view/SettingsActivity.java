package com.merann.smamonov.googledrive.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.merann.smamonov.googledrive.R;

public class SettingsActivity extends AppCompatActivity {

    public final String LOG_TAG = "GoogleDriveSettingsAct";

    private int mSyncPeriod;
    private SeekBar mSyncSeekBar;
    private TextView syncPeriodText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        readConfiguration();

        mSyncSeekBar = (SeekBar) findViewById(R.id.seekBar);

        final int[] periods = getResources().getIntArray(R.array.sync_periods);
        mSyncSeekBar.setMax(periods.length - 1);
        syncPeriodText = (TextView) findViewById(R.id.syncPeriodText);

        updateSyncPeriod();

        mSyncSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(LOG_TAG, "onProgressChanged progress:" + progress);
                updateSyncPeriod();
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
    }

    private void updateSyncPeriod()
    {
        final int[] periods = getResources().getIntArray(R.array.sync_periods);
        mSyncPeriod = periods[mSyncSeekBar.getProgress()];
        syncPeriodText.setText(mSyncPeriod + "min.");
    }

    private void readConfiguration()
    {

    }

}
