package com.merann.smamonov.googledrive.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.merann.smamonov.googledrive.R;


/**
 * Created by samam_000 on 01.12.2015.
 */
public class GoogleDriveService extends Service {

    static private final String LOG_TAG = "GoogleDrive";
    private GoogleApiClient mGoogleApiClient;

    static public final String COMMAND_PARAMETER_NAME = "COMMAND";
    static public final String COMMAND_CONNECT = "CONNECT";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return new Binder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        //connect();
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

        if(intent == null)
        {
            return super.onStartCommand(intent, flags, startId);
        }

        String command_name = intent.getStringExtra(COMMAND_PARAMETER_NAME);

        if (command_name != null) {
            switch (command_name) {
                case COMMAND_CONNECT:
                    connect();
                    break;
                default:
                    Log.e(LOG_TAG, "onStartCommand: unknown command " + command_name);
                    break;
            }
        } else {
            Log.e(LOG_TAG, "onStartCommand: unable to get command");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void connect() {
        Log.d(LOG_TAG, "connect");

        if (mGoogleApiClient != null) {

            Log.d(LOG_TAG, "connect mGoogleApiClient exists");
            Log.d(LOG_TAG, "connect isConnecting:" + mGoogleApiClient.isConnecting() + " isConnected:" + mGoogleApiClient.isConnected());
            mGoogleApiClient.connect();
        } else {
            Log.d(LOG_TAG, "connect mGoogleApiClient doesn't exists");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Log.d(LOG_TAG, "onConnected");
                            loadFileMetagata();
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.d(LOG_TAG, "onConnectionSuspended");
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            Log.e(LOG_TAG, "onConnectionFailed:" + connectionResult.toString());
                            Intent intent = new Intent(getResources().getString(R.string.on_drive_connection_failed))
                                    .putExtra(getResources().getString(R.string.on_drive_connection_failed_data), connectionResult);
                            sendBroadcast(intent);
                        }
                    })
                    .build();
            Log.d(LOG_TAG, "connect mGoogleApiClient.connect()");
            mGoogleApiClient.connect();
        }
    }


    void loadFileMetagata() {
        Log.d(LOG_TAG, "loadFileMetagata");
        DriveFolder driveFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
        Log.d(LOG_TAG, "Root folder is " + driveFolder.getDriveId());

//        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
//                .setTitle("My_test_folder")
//                .build();
//
//        driveFolder.createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
//            @Override
//            public void onResult(DriveFolder.DriveFolderResult driveFolderResult) {
//                Log.d(LOG_TAG, "loadFileMetagata::oncreateFolderResult");
//                DriveFolder driveFolder1 = driveFolderResult.getDriveFolder();
//                Log.d(LOG_TAG, "new folder is created:" + driveFolder1.getDriveId());
//            }
//        });

        final PendingResult<DriveApi.MetadataBufferResult> result = driveFolder.listChildren(mGoogleApiClient);
        result.setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                Log.d(LOG_TAG, "loadFileMetagata::onResult: ");

                MetadataBuffer metadataBuffer = metadataBufferResult.getMetadataBuffer();

                for (int index = 0;
                     index < metadataBuffer.getCount();
                     index++) {
                    Metadata metadata = metadataBuffer.get(index);
                    Log.d(LOG_TAG, "loadFileMetagata::onSuccess: " + index + ": " + metadata.getTitle());
                }
            }
        });
    }
}
