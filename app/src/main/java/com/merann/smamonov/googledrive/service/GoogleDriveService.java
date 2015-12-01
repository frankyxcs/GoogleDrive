package com.merann.smamonov.googledrive.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.merann.smamonov.googledrive.R;


/**
 * Created by samam_000 on 01.12.2015.
 */
public class GoogleDriveService extends Service {

    private final String LOG_TAG = "GoogleDriveService";
    private GoogleApiClient mGoogleApiClient;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        mGoogleApiClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public void connect() {
        Log.d(LOG_TAG, "connect");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(LOG_TAG, "onConnected");
                        DriveFolder driveFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
                        Log.d(LOG_TAG, "Root folder is " + driveFolder.toString());
                        PendingResult<DriveApi.MetadataBufferResult> result =  driveFolder.listChildren(mGoogleApiClient);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(LOG_TAG, "onConnectionSuspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);
                        Intent intent = new Intent(getResources().getString(R.string.on_drive_connection_failed))
                                .putExtra(getResources().getString(R.string.on_drive_connection_failed_data), connectionResult);
                        sendBroadcast(intent);
                    }
                })
                .build();
        mGoogleApiClient.connect();
    }

    public boolean isConnecting()
    {
        return mGoogleApiClient != null && mGoogleApiClient.isConnecting();
    }
}
