package com.merann.smamonov.googledrive.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.merann.smamonov.googledrive.R;
import com.merann.smamonov.googledrive.service.DriveServiceProxy;
import com.merann.smamonov.googledrive.service.DriveServiceProxyForActivity;

public class HomeActivity extends AppCompatActivity {

    public final String LOG_TAG = "HomeActivity";
    DriveServiceProxyForActivity mDriveServiceProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
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

        mDriveServiceProxy = new DriveServiceProxyForActivity(this, new DriveServiceProxy.OnConnectionStateChangeListener() {
            @Override
            public void onConnectionStateChange(boolean isConneted) {
                onServiceConnected(isConneted);
            }
        });
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart");
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(LOG_TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d(LOG_TAG, "onOptionsItemSelected: action_settings");
            showSettingsActivity();
            return true;
        } else if (id == R.id.action_sync) {
            Log.d(LOG_TAG, "onOptionsItemSelected: action_sync");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onServiceConnected(boolean isConnected) {
        Log.d(LOG_TAG, "onServiceConnected");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (isConnected) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.toolBarColorConnected));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.toolBarColorDisconnected));
        }
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();
        mDriveServiceProxy.bind();
        mDriveServiceProxy.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        if (!mDriveServiceProxy.onActivityResultHandler(requestCode, resultCode, data)) {
            Log.e(LOG_TAG, "unable to handle onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        }
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onStop");
        super.onPause();
        mDriveServiceProxy.unBind();
    }

    private void showSettingsActivity() {
        Log.d(LOG_TAG, "showSettingsActivity");
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }
}
