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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.merann.smamonov.googledrive.R;
import com.merann.smamonov.googledrive.service.GoogleDriveService;

public class HomeActivity extends AppCompatActivity {

    BroadcastReceiver onDriveConnectionFailedBroadcastReceiver;

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

        onDriveConnectionFailedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final ConnectionResult connectionResult = intent.getParcelableExtra(getResources().getString(R.string.on_drive_connection_failed_data));
                if (connectionResult.hasResolution()) {

                    try {
                        connectionResult.startResolutionForResult(HomeActivity.this, 7);
                    } catch (IntentSender.SendIntentException e) {

                    }
                } else {
                    GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), HomeActivity.this, 0).show();
                }
            }
        };
        IntentFilter onDriveConnectionFailedIntendFilter = new IntentFilter(getResources().getString(R.string.on_drive_connection_failed));
        registerReceiver(onDriveConnectionFailedBroadcastReceiver, onDriveConnectionFailedIntendFilter);
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

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, GoogleDriveService.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.err.println("onActivityResult requestCode" + requestCode + " resultCode" + resultCode);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(onDriveConnectionFailedBroadcastReceiver);
    }
}
