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
import com.merann.smamonov.googledrive.managers.StorageManager;
import com.merann.smamonov.googledrive.model.Image;
import com.merann.smamonov.googledrive.view.NotificationActivity;

import java.io.File;
import java.util.List;

/**
 * Created by samam_000 on 06.12.2015.
 */
public class DriveService extends BaseService {

    public static class DriveServiceBinder extends Binder {

        interface DriveServiceBinderListener {
            void onFileUploaded(File file, Boolean isSuccess);

            void onFileListChanged(List<Image> fileList);

            void onSynchronizationStarted();

            void onSynchronisationFinished();

            void onConnectedFailed(ConnectionResult connectionResult);
        }

        DriveService mDriveService;
        DriveServiceBinderListener mDriveServiceBinderListener;

        public DriveServiceBinder(DriveService driveService) {
            super();
            mDriveServiceBinderListener = null;
            mDriveService = driveService;
        }

        public List<Image> getImagesList() {
            return mDriveService.getImagesList();
        }

        public void doSync() {
            mDriveService.doSync();
        }

        public void uploadFile(File file) {
            mDriveService.uploadFile(file);
        }

        public void setListener(DriveServiceBinderListener listener) {
            mDriveServiceBinderListener = listener;
        }

        void notifyFileUploaded(File file, Boolean isSuccess) {
            if (mDriveServiceBinderListener != null) {
                mDriveServiceBinderListener.onFileUploaded(file, isSuccess);
            }
        }

        void notifyFileListChanged(List<Image> fileList) {
            if (mDriveServiceBinderListener != null) {
                mDriveServiceBinderListener.onFileListChanged(fileList);
            }
        }

        void notifySynchronizationStarted() {
            if (mDriveServiceBinderListener != null) {
                mDriveServiceBinderListener.onSynchronizationStarted();
            }
        }

        void notifySynchronisationFinished() {
            if (mDriveServiceBinderListener != null) {
                mDriveServiceBinderListener.onSynchronisationFinished();
            }
        }

        void notifyConnectedFailed(ConnectionResult connectionResult) {
            if (mDriveServiceBinderListener != null) {
                mDriveServiceBinderListener.onConnectedFailed(connectionResult);
            }
        }
    }

    static public final String INTEND_STRING = "com.merann.smamonov.googledrive.DriveService";
    static private final String LOG_TAG = "DriveService";

    private StorageManager mStorageManager;
    private DriveServiceBinder mBinder;

    public DriveService() {
        super(LOG_TAG, INTEND_STRING);

        mStorageManager = null;

        Log.d(LOG_TAG, "DriveService");
    }

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
        addMessageHandler(Message.REMOTE_DRIVE_START, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.d(LOG_TAG, "REMOTE_DRIVE_START");
            }
        });

        mStorageManager = new StorageManager(this,
                new StorageManager.StorageManagerListener() {
                    @Override
                    public void onFilesChanged() {
                        Log.d(LOG_TAG, "onFilesChanged");
                    }

                    @Override
                    public void onFileUpload(File file, boolean isSuccess) {
                        Log.d(LOG_TAG, "onFileUpload");

                    }

                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(LOG_TAG, "onConnectionFailed");
                        DriveService.this.onConnectionFailed(connectionResult);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mStorageManager = null;
    }

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

    private void sendNotification(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "sendNotification");

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

    }

    private void onFileUpload(File file, Boolean isSuccess) {
        Log.e(LOG_TAG, "onFileUpload");
        mBinder.notifyFileUploaded(file, isSuccess);
    }

    private void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "onConnectionFailed");

        if (mBinder != null) {
            if (connectionResult.hasResolution()) {
                mBinder.notifyConnectedFailed(connectionResult);
            } else {
                Log.e(LOG_TAG,
                        "onConnectionFailed: RemoteDrive connection was failed:"
                                + connectionResult.getErrorMessage());
            }
        } else {
            sendNotification(connectionResult);
        }
    }

//        sendMessage(createMessage(Message.REMOTE_DRIVE_AUTHENTICATION_PERFORM_REQUEST)
//                .putExtra(ConnectionResult.class.toString(),
//                        connectionResult));

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
    private void uploadFile(File file) {
        Log.d(LOG_TAG, "uploadFile : "
                + file.getName());

        mStorageManager.uploadFile(file);
    }

    private void doSync() {
        Log.d(LOG_TAG, "doSync");
        mStorageManager.doSync();
    }

    @Override
    public IBinder onBind(Intent intent) {
        mBinder = new DriveServiceBinder(this);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mBinder = null;
        return true;
    }

    public List<Image> getImagesList() {
        return mStorageManager.getImages();
    }
}
