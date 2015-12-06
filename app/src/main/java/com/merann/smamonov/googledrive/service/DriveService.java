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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by samam_000 on 06.12.2015.
 */
public class DriveService extends BaseService {

    static public final String INTEND_STRING = "com.merann.smamonov.googledrive.DriveService";
    static private final String LOG_TAG = "DriveService";
    private List<Metadata> mFiles = new ArrayList<>();
    String mFolderName;
    int mSyncPeriod;

    public DriveService() {
        super(INTEND_STRING);
        Log.d(LOG_TAG, "DriveService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");

        addMessageHandler(Message.AUTHENTICATION_PERFORM_RESPONSE, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "AUTHENTICATION_PERFORM_RESPONSE");
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

        addMessageHandler(Message.REMOTE_DRIVE_SETUP_CONFIGURATION_REQUEST, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "REMOTE_DRIVE_SETUP_CONFIGURATION_REQUEST");
                setupConfiguration(intent);
            }
        });

        mConfigurationServiceProxy = new ConfigurationServiceProxy(this,
                new ConfigurationServiceProxy.GetConfigurationListener() {
                    @Override
                    public void onGetConfiguration(String folderName, int syncTime) {
                        Log.d(LOG_TAG, "onGetConfiguration");
                        mSyncPeriod = syncTime;
                        mFolderName = folderName;
                    }
                },
                new ConfigurationServiceProxy.UpdateConfigurationListener() {
                    @Override
                    public void onUpdateConfiguration(String folderName, int syncTime) {
                        Log.d(LOG_TAG, "onUpdateConfiguration");
                    }
                },
                new ConfigurationServiceProxy.CacheCleanCacheListener() {
                    @Override
                    public void onCacheClean() {
                        Log.d(LOG_TAG, "onCacheClean");
                    }
                });

        mConfigurationServiceProxy.bind();
        mConfigurationServiceProxy.getConfiguration();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mConfigurationServiceProxy.unBind();
    }

    /* Business logic */
    private GoogleApiClient mGoogleApiClient;

    private void connect() {
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
                    .build();
            Log.d(LOG_TAG, "connect mGoogleApiClient.connect()");
            mGoogleApiClient.connect();
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
        sendMessage(createMessage(Message.AUTHENTICATION_PERFORM_REQUEST)
                .putExtra(ConnectionResult.class.toString(),
                        connectionResult));
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
                    mFiles.add(metadata);
                }
            }
        });
    }

    void createFolder(DriveFolder rootFolder, final String newFolderName) {
        Log.d(LOG_TAG, "createFolder newFolderName:" + newFolderName);

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(newFolderName)
                .build();

        rootFolder.createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
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

        ConfigurationService.Configuration configuration = (ConfigurationService.Configuration) intent
                .getSerializableExtra(ConfigurationService
                        .Configuration
                        .class
                        .getName());
        mSyncPeriod = configuration.getSyncPeriod();
        mFolderName = configuration.getFolderName();

        Log.d(LOG_TAG, "setupConfiguration : configuration was updated:" + mFolderName + mSyncPeriod);

        sendMessage(createMessage(Message.GET_CONFIGURATION_RESPONSE));

    }
}
