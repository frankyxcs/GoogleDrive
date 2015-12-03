package com.merann.smamonov.googledrive.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.merann.smamonov.googledrive.R;
import com.merann.smamonov.googledrive.service.GoogleDriveService;

public class HomeActivity extends AppCompatActivity {

    public final String LOG_TAG = "GoogleDrive";

    BroadcastReceiver driveBroadcastReceiverHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        driveBroadcastReceiverHandler = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleIntendFromDriveService(intent);
            }
        };

        IntentFilter onDriveNotificationIntendFilter = new IntentFilter(getResources().getString(R.string.drive_intend_notification));
        registerReceiver(driveBroadcastReceiverHandler, onDriveNotificationIntendFilter);
    }

    private void handleIntendFromDriveService(Intent intent) {

        int messageType = intent.getIntExtra(getResources().getString(R.string.drive_intend_message_type), 0);

        if (messageType == 0) {
            Log.d(LOG_TAG, "Unable to handle notification from GoogleDriveService");
        } else {

            switch (messageType) {
                case GoogleDriveService.AUTHENTICATION_PERFORM_REQUEST: {
                    onServiceConnected(false);
                    final ConnectionResult connectionResult = intent.getParcelableExtra(getResources().getString(R.string.on_drive_connection_failed_data));
                    if (connectionResult.hasResolution()) {
                        try {
                            connectionResult.startResolutionForResult(HomeActivity.this, GoogleDriveService.AUTHENTICATION_PERFORM_REQUEST);
                        } catch (IntentSender.SendIntentException e) {
                            Log.d(LOG_TAG, "AUTHENTICATION_PERFORM_REQUEST e" + e.getMessage());
                        }
                    } else {
                        GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), HomeActivity.this, 0).show();
                    }
                    break;
                }

                case GoogleDriveService.GOOGLE_DRIVE_CONNECTED: {
                    onServiceConnected(true);
                    break;
                }

                case GoogleDriveService.GOOGLE_DRIVE_DISCONNECTED: {
                    onServiceConnected(false);
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onServiceConnected(boolean isConnected) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (isConnected) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.toolBarColorConnected));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.toolBarColorDisconnected));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent startServiceIntend = new Intent(this, GoogleDriveService.class);
        startServiceIntend.putExtra(GoogleDriveService.COMMAND_PARAMETER_NAME, GoogleDriveService.COMMAND_CONNECT);
        startService(startServiceIntend);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);

        switch (requestCode) {
            case GoogleDriveService.AUTHENTICATION_PERFORM_REQUEST: {
                if (resultCode == RESULT_OK) {
                    Log.d(LOG_TAG, "onActivityResult AUTHENTICATION_PERFORM_REQUEST result is RESULT_OK, starting service");
                    Intent startServiceIntend = new Intent(this, GoogleDriveService.class);
                    startServiceIntend.putExtra(GoogleDriveService.COMMAND_PARAMETER_NAME, GoogleDriveService.COMMAND_CONNECT);
                    startService(startServiceIntend);
                } else {
                    Log.e(LOG_TAG, "onActivityResult AUTHENTICATION_PERFORM_REQUEST error resultCode " + resultCode);
                }
                break;
            }

            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(driveBroadcastReceiverHandler);
    }
}
