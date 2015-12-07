package com.merann.smamonov.googledrive.service;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;

/**
 * Created by samam_000 on 06.12.2015.
 */
public class DriveService extends BaseService {

    static public final String INTEND_STRING = "com.merann.smamonov.googledrive.DriveService";
    static private final String LOG_TAG = "DriveService";

    public DriveService() {
        super(LOG_TAG, INTEND_STRING);
        Log.d(LOG_TAG, "DriveService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");

        addMessageHandler(Message.REMOTE_DRIVE_AUTHENTICATION_PERFORM_RESPONSE, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "REMOTE_DRIVE_AUTHENTICATION_PERFORM_RESPONSE");
                connect();
            }
        });
        addMessageHandler(Message.REMOTE_DRIVE_CONNECT_REQUEST, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "REMOTE_DRIVE_CONNECT_REQUEST");
                connect();
            }
        });

        addMessageHandler(Message.REMOTE_DRIVE_CONFIGURATION_UPDATE_NOTIFICATION, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "REMOTE_DRIVE_CONFIGURATION_UPDATE_NOTIFICATION");
                setupConfiguration(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /* Business logic */


    private void connect() {
        Log.d(LOG_TAG, "connect");

        if (DriveServiceData.getInstance().getCurrentConfiguration() == null) {
            Log.d(LOG_TAG, "Connection requested while we have no configuration");
            DriveServiceData.getInstance().setIsConnectionRequested(true);
            getConfiguration();
        } else if (DriveServiceData.getInstance().getGoogleApiClient() != null) {
            Log.d(LOG_TAG, "connect isConnecting:"
                    + DriveServiceData.getInstance().getGoogleApiClient().isConnecting()
                    + " isConnected:"
                    + DriveServiceData.getInstance().getGoogleApiClient().isConnected());

            DriveServiceData.getInstance().getGoogleApiClient().connect();
        } else {
            Log.d(LOG_TAG, "connect mGoogleApiClient doesn't exists");
            DriveServiceData.getInstance().setGoogleApiClient(new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Log.d(LOG_TAG, "onConnected");
                            onConnectionEstablished();
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.d(LOG_TAG, "onConnectionSuspended");
                            onConnectionLost();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            Log.e(LOG_TAG, "onConnectionFailed");
                            onConnectingFailed(connectionResult);
                        }
                    })
                    .build());
            Log.d(LOG_TAG, "connect mGoogleApiClient.connect()");
            DriveServiceData.getInstance().getGoogleApiClient().connect();
        }
    }

    private void onConnectionEstablished() {
        Log.d(LOG_TAG, "onConnectionEstablished");
        sendMessage(createMessage(Message.REMOTE_DRIVE_CONNECT_RESPONSE));
        loadFileMetagata();
    }

    private void onConnectionLost() {
        Log.d(LOG_TAG, "onConnectionLost");
        sendMessage(createMessage(Message.REMOTE_DRIVE_DISCONNECT_NOTIFICATION));
    }

    private void onConnectingFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "onConnectingFailed");
        sendMessage(createMessage(Message.REMOTE_DRIVE_AUTHENTICATION_PERFORM_REQUEST)
                .putExtra(ConnectionResult.class.toString(),
                        connectionResult));
    }

    void loadFileMetagata() {
        Log.d(LOG_TAG, "loadFileMetagata");
        DriveFolder driveFolder = Drive.DriveApi.getRootFolder(DriveServiceData.getInstance().getGoogleApiClient());
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

        final PendingResult<DriveApi.MetadataBufferResult> result = driveFolder.listChildren(DriveServiceData.getInstance().getGoogleApiClient());
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
                    DriveServiceData.getInstance().getFiles().add(metadata);
                }
            }
        });
    }

    void createFolder(DriveFolder rootFolder, final String newFolderName) {
        Log.d(LOG_TAG, "createFolder newFolderName:" + newFolderName);

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(newFolderName)
                .build();

        rootFolder.createFolder(DriveServiceData.getInstance().getGoogleApiClient(), changeSet)
                .setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(DriveFolder.DriveFolderResult driveFolderResult) {
                Log.d(LOG_TAG, "createFolder::onResult");
                DriveFolder driveFolder = driveFolderResult.getDriveFolder();

                if (driveFolder != null) {
                    Log.d(LOG_TAG, "created folder id:" + driveFolder.getDriveId());
                } else {
                    Log.d(LOG_TAG, "unable to create folder " + newFolderName);
                }
            }
        });
    }

    private void setupConfiguration(Intent intent) {
        Log.d(LOG_TAG, "setupConfiguration");

        DriveServiceData.getInstance().setCurrentConfiguration((ConfigurationService.Configuration) intent
                .getSerializableExtra(ConfigurationService
                        .Configuration
                        .class
                        .getName()));


        Log.d(LOG_TAG, "setupConfiguration : configuration was updated: "
                + DriveServiceData.getInstance().getCurrentConfiguration().getFolderName()
                + DriveServiceData.getInstance().getCurrentConfiguration().getSyncPeriod());

        if (DriveServiceData.getInstance().isConnectionRequested()) {
            connect();
        }
    }

    private void getConfiguration() {
        ConfigurationServiceProxy configurationServiceProxy = new ConfigurationServiceProxy(this, null, null, null);
        configurationServiceProxy.getConfiguration();
    }
}
