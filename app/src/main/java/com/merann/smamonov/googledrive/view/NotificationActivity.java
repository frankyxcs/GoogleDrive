package com.merann.smamonov.googledrive.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.merann.smamonov.googledrive.R;
import com.merann.smamonov.googledrive.service.DriveServiceProxyForActivity;

public class NotificationActivity extends AppCompatActivity {

    public final String LOG_TAG = "NotificationActivity";

    DriveServiceProxyForActivity mDriveServiceProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        mDriveServiceProxy = new DriveServiceProxyForActivity(this, null);

        Intent intent = getIntent();
        mDriveServiceProxy.handleAuthenticationRequest(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        if (!mDriveServiceProxy.onActivityResultHandler(requestCode, resultCode, data)) {
            Log.e(LOG_TAG, "unable to handle onActivityResult requestCode:"
                    + requestCode
                    + " resultCode:"
                    + resultCode);
        }
        finish();
    }
}

