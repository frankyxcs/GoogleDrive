package com.merann.smamonov.googledrive.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.merann.smamonov.googledrive.managers.RemoteStorageManager;
import com.merann.smamonov.googledrive.managers.StorageManager;
import com.merann.smamonov.googledrive.model.Image;
import com.merann.smamonov.googledrive.view.NotificationActivity;

import java.util.List;

/**
 * Created by samam_000 on 06.12.2015.
 */
public class DriveService extends BaseService {

    public class DriveServiceBinder extends Binder {
        DriveService mDriveService;

        public DriveServiceBinder(DriveService driveService) {
            super();
            mDriveService = driveService;
        }

        public List<Image> getImagesList() {
            return mDriveService.getImagesList();
        }

        public void doSync() {
            mDriveService.doSync();
        }
    }

    static public final String INTEND_STRING = "com.merann.smamonov.googledrive.DriveService";
    static private final String LOG_TAG = "DriveService";

    public DriveService() {
        super(LOG_TAG, INTEND_STRING);
        Log.d(LOG_TAG, "DriveService");
    }

    StorageManager mStorageManager;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "onCreate");

//        addMessageHandler(Message.REMOTE_DRIVE_AUTHENTICATION_PERFORM_RESPONSE, new IMessageHandler() {
//            @Override
//            public void onIntent(Intent intent) {
//                Log.d(LOG_TAG, "REMOTE_DRIVE_AUTHENTICATION_PERFORM_RESPONSE");
//                connect();
//            }
//        });
//        addMessageHandler(Message.REMOTE_DRIVE_CONNECT_REQUEST, new IMessageHandler() {
//            @Override
//            public void onIntent(Intent intent) {
//                Log.d(LOG_TAG, "REMOTE_DRIVE_CONNECT_REQUEST");
//                connect();
//            }
//        });
//
//        addMessageHandler(Message.REMOTE_DRIVE_CONFIGURATION_UPDATE_NOTIFICATION, new IMessageHandler() {
//            @Override
//            public void onIntent(Intent intent) {
//                Log.d(LOG_TAG, "REMOTE_DRIVE_CONFIGURATION_UPDATE_NOTIFICATION");
//                setupConfiguration(intent);
//            }
//        });
//
//        addMessageHandler(Message.REMOTE_DRIVE_UPLOAD_FILE_REQUEST, new IMessageHandler() {
//            @Override
//            public void onIntent(Intent intent) {
//                Log.d(LOG_TAG, "REMOTE_DRIVE_UPLOAD_FILE_REQUEST");
//                uploadFile(intent);
//            }
//        });
//
//        addMessageHandler(Message.REMOTE_DRIVE_LOAD_FILES_REQUEST, new IMessageHandler() {
//            @Override
//            public void onIntent(Intent intent) {
//                Log.d(LOG_TAG, "REMOTE_DRIVE_LOAD_FILES_REQUEST");
//                handleConnectionEstablished();
//            }
//        });
//
//
//        addMessageHandler(Message.REMOTE_DRIVE_DO_SYNC, new IMessageHandler() {
//            @Override
//            public void onIntent(Intent intent) {
//                Log.d(LOG_TAG, "REMOTE_DRIVE_DO_SYNC");
//                doSync();
//            }
//        });

        mStorageManager = new StorageManager(this,
                new StorageManager.StorageManagerListener() {
                    @Override
                    public void onFilesChanged() {
                        sendMessage(createMessage(Message.REMOTE_DRIVE_NEW_FILE_NOTIFY));
                    }
                });

//        RemoteStorageManager.getInstance().setOnNewFileListener(new RemoteStorageManager.RemoteStorageManagerListener() {
//            @Override
//            public void onNewFile(String fileName) {
//                sendMessage(createMessage(Message.REMOTE_DRIVE_NEW_FILE_NOTIFY)
//                        .putExtra(String.class.getName(), fileName));
//            }
//
//            @Override
//            public void onFileUpload(String fileName, boolean isSuccess) {
//                sendMessage(createMessage(Message.REMOTE_DRIVE_UPLOAD_FILE_RESPONSE)
//                        .putExtra(Boolean.class.getName(), isSuccess));
//            }
//        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mStorageManager = null;
    }

//    /* Business logic */
//    private void connect() {
//        Log.d(LOG_TAG, "connect");
//
//        if (RemoteStorageManager.getInstance().getCurrentConfiguration() == null) {
//            Log.d(LOG_TAG, "Connection requested while we have no configuration");
//            RemoteStorageManager.getInstance().setIsConnectionRequested(true);
//            getConfiguration();
//        } else if (RemoteStorageManager.getInstance().getGoogleApiClient() != null) {
//            if (RemoteStorageManager.getInstance().isConnected()) {
//                onConnectionEstablished();
//            } else {
//                RemoteStorageManager.getInstance().getGoogleApiClient().connect();
//            }
//        } else {
//            Log.d(LOG_TAG, "connect mGoogleApiClient doesn't exists");
//            RemoteStorageManager.getInstance().setGoogleApiClient(new GoogleApiClient.Builder(this)
//                    .addApi(Drive.API)
//                    .addScope(Drive.SCOPE_FILE)
//                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                        @Override
//                        public void onConnected(Bundle bundle) {
//                            Log.d(LOG_TAG, "onConnected");
//                            DriveServiceProxy driveServiceProxy = new DriveServiceProxy(getBaseContext());
//                            driveServiceProxy.handleConnectionEstablished();
//                            onConnectionEstablished();
//                        }
//
//                        @Override
//                        public void onConnectionSuspended(int i) {
//                            Log.d(LOG_TAG, "onConnectionSuspended");
//                            onConnectionLost();
//                        }
//                    })
//                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
//                        @Override
//                        public void onConnectionFailed(ConnectionResult connectionResult) {
//                            Log.e(LOG_TAG, "onConnectionFailed");
//                            DriveService.this.onConnectionFailed(connectionResult);
//                        }
//                    })
//                    .build());
//            Log.d(LOG_TAG, "connect mGoogleApiClient.connect()");
//            RemoteStorageManager.getInstance().getGoogleApiClient().connect();
//        }
//    }

    private void onConnectionEstablished() {
        Log.d(LOG_TAG, "onConnectionEstablished");
        sendMessage(createMessage(Message.REMOTE_DRIVE_CONNECT_NOTIFICATION));


    }

//    private void handleConnectionEstablished() {
//        Log.d(LOG_TAG, "handleConnectionEstablished");
//        sendMessage(createMessage(Message.REMOTE_DRIVE_CONNECT_RESPONSE));
//        RemoteStorageManager.getInstance().getFilesSync();
//    }
//
//    private void onConnectionLost() {
//        Log.d(LOG_TAG, "onConnectionLost");
//        sendMessage(createMessage(Message.REMOTE_DRIVE_DISCONNECT_NOTIFICATION));
//    }

    private void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "onConnectionFailed");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder notificationBuilder = new Notification.Builder(this);

        Intent notificationIntent = new Intent(this, NotificationActivity.class);

        notificationIntent.setAction("Resolve problem")
                .putExtra(ConnectionResult.class.toString(),
                        connectionResult);

        String title = "title";
        String content = "content";

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                1,
                notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT |
                        PendingIntent.FLAG_ONE_SHOT);

        notificationBuilder
                .setContentIntent(pendingIntent)
                .setSmallIcon(android.R.drawable.ic_input_delete)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(content)
                .setWhen(0)
                .setVibrate(new long[]{150, 150, 150, 150, 75, 75, 150, 150, 150, 150, 450})
                .setAutoCancel(true);

        Notification notification = notificationBuilder.build();

        notificationManager.notify(1, notification);

//        sendMessage(createMessage(Message.REMOTE_DRIVE_AUTHENTICATION_PERFORM_REQUEST)
//                .putExtra(ConnectionResult.class.toString(),
//                        connectionResult));
    }

//    private void setupConfiguration(Intent intent) {
//        Log.d(LOG_TAG, "setupConfiguration");
//
//        RemoteStorageManager.getInstance().setCurrentConfiguration((ConfigurationService.Configuration) intent
//                .getSerializableExtra(ConfigurationService
//                        .Configuration
//                        .class
//                        .getName()));
//
//        Log.d(LOG_TAG, "setupConfiguration : configuration was updated: "
//                + RemoteStorageManager.getInstance().getCurrentConfiguration().getFolderName()
//                + RemoteStorageManager.getInstance().getCurrentConfiguration().getSyncPeriod());
//
//        if (RemoteStorageManager.getInstance().isConnectionRequested()) {
//            connect();
//        }
//    }

//    private void getConfiguration() {
//        ConfigurationServiceProxy configurationServiceProxy = new ConfigurationServiceProxy(this, null, null, null);
//        configurationServiceProxy.getConfiguration();
//    }
//
//    private void uploadFile(Intent intent) {
//        File file = (File) intent.getSerializableExtra(File.class.getName());
//        Log.d(LOG_TAG, "uploadFile : "
//                + file.getName());
//
//        RemoteStorageManager.getInstance().uploadFileSync(file);
//    }

    private void doSync() {
        Log.d(LOG_TAG, "doSync");
        RemoteStorageManager remoteStorageManager = new RemoteStorageManager(this, mStorageManager);
        remoteStorageManager.connect(new RemoteStorageManager.RemoteStorageManagerListener() {

            @Override
            public void onFileUpload(String fileName, boolean isSuccess) {

            }

            @Override
            public void onConnectionFailed(final ConnectionResult connectionResult) {
                DriveService.this.onConnectionFailed(connectionResult);
            }

            @Override
            public void onConnectionEstablished() {
                Log.d(LOG_TAG, "onConnected");
                DriveServiceProxy driveServiceProxy = new DriveServiceProxy(getBaseContext());
                driveServiceProxy.handleConnectionEstablished();
                DriveService.this.onConnectionEstablished();
            }

            @Override
            public void onConnectionSuspended() {
                Log.d(LOG_TAG, "onConnectionSuspended");
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new DriveServiceBinder(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    public List<Image> getImagesList() {
        return mStorageManager.getImages();
    }
}
