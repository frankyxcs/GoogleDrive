package com.merann.smamonov.googledrive.service;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.merann.smamonov.googledrive.managers.RemoteStorageManager;

import java.io.File;

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

        addMessageHandler(Message.REMOTE_DRIVE_UPLOAD_FILE_REQUEST, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "REMOTE_DRIVE_UPLOAD_FILE_REQUEST");
                uploadFile(intent);
            }
        });

        addMessageHandler(Message.REMOTE_DRIVE_LOAD_FILES_REQUEST, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "REMOTE_DRIVE_LOAD_FILES_REQUEST");
                handleConnectionEstablished();
            }
        });

        RemoteStorageManager.getInstance().setOnNewFileListener(new RemoteStorageManager.RemoteStorageManagerListener() {
            @Override
            public void onNewFile(String fileName) {
                sendMessage(createMessage(Message.REMOTE_DRIVE_NEW_FILE_NOTIFY)
                        .putExtra(String.class.getName(), fileName));
            }

            @Override
            public void onFileUpload(String fileName, boolean isSuccess) {
                sendMessage(createMessage(Message.REMOTE_DRIVE_UPLOAD_FILE_RESPONSE)
                        .putExtra(Boolean.class.getName(), isSuccess));
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

        if (RemoteStorageManager.getInstance().getCurrentConfiguration() == null) {
            Log.d(LOG_TAG, "Connection requested while we have no configuration");
            RemoteStorageManager.getInstance().setIsConnectionRequested(true);
            getConfiguration();
        } else if (RemoteStorageManager.getInstance().getGoogleApiClient() != null) {
            if (RemoteStorageManager.getInstance().isConnected()) {
                onConnectionEstablished();
            } else {
                RemoteStorageManager.getInstance().getGoogleApiClient().connect();
            }
        } else {
            Log.d(LOG_TAG, "connect mGoogleApiClient doesn't exists");
            RemoteStorageManager.getInstance().setGoogleApiClient(new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Log.d(LOG_TAG, "onConnected");
                            DriveServiceProxy driveServiceProxy = new DriveServiceProxy(getBaseContext());
                            driveServiceProxy.handleConnectionEstablished();
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
            RemoteStorageManager.getInstance().getGoogleApiClient().connect();
        }
    }

    private void onConnectionEstablished() {
        Log.d(LOG_TAG, "onConnectionEstablished");
        sendMessage(createMessage(Message.REMOTE_DRIVE_CONNECT_NOTIFICATION));
    }

    private void handleConnectionEstablished() {
        Log.d(LOG_TAG, "handleConnectionEstablished");
        sendMessage(createMessage(Message.REMOTE_DRIVE_CONNECT_RESPONSE));
        RemoteStorageManager.getInstance().getFilesSync();
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

    private void setupConfiguration(Intent intent) {
        Log.d(LOG_TAG, "setupConfiguration");

        RemoteStorageManager.getInstance().setCurrentConfiguration((ConfigurationService.Configuration) intent
                .getSerializableExtra(ConfigurationService
                        .Configuration
                        .class
                        .getName()));

        Log.d(LOG_TAG, "setupConfiguration : configuration was updated: "
                + RemoteStorageManager.getInstance().getCurrentConfiguration().getFolderName()
                + RemoteStorageManager.getInstance().getCurrentConfiguration().getSyncPeriod());

        if (RemoteStorageManager.getInstance().isConnectionRequested()) {
            connect();
        }
    }

    private void getConfiguration() {
        ConfigurationServiceProxy configurationServiceProxy = new ConfigurationServiceProxy(this, null, null, null);
        configurationServiceProxy.getConfiguration();
    }

    private void uploadFile(Intent intent) {
        File file = (File) intent.getSerializableExtra(File.class.getName());
        Log.d(LOG_TAG, "uploadFile : "
                + file.getName());

        RemoteStorageManager.getInstance().uploadFileSync(file);
    }
}
