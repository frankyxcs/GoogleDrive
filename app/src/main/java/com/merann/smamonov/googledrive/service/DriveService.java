package com.merann.smamonov.googledrive.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.merann.smamonov.googledrive.managers.ConfigurationManager;
import com.merann.smamonov.googledrive.managers.StorageManager;
import com.merann.smamonov.googledrive.model.Configuration;
import com.merann.smamonov.googledrive.model.Image;
import com.merann.smamonov.googledrive.view.NotificationActivity;

import java.io.File;
import java.util.List;

/**
 * Created by samam_000 on 06.12.2015.
 */
public class DriveService extends BaseService {

    static public class DriveServiceBinder extends Binder {

        interface DriveServiceBinderListener {
            void onFileUploaded(File file, Boolean isSuccess);

            void onFileListChanged();

            void onSynchronizationStarted();

            void onSynchronisationFinished();

            void onConnectedFailed(ConnectionResult connectionResult);
        }

        static private final String LOG_TAG = "DriveServiceBinder";
        DriveService mDriveService;
        DriveServiceBinderListener mDriveServiceBinderListener;


        public DriveServiceBinder(DriveService driveService) {
            super();
            Log.d(LOG_TAG, "DriveServiceBinder");
            mDriveServiceBinderListener = null;
            mDriveService = driveService;
        }

        public List<Image> getImagesList() {
            Log.d(LOG_TAG, "getImagesList");
            return mDriveService.getImagesList();
        }

        public void doSync() {
            Log.d(LOG_TAG, "doSync");
            mDriveService.doSync();
        }

        public void uploadFile(File file) {
            Log.d(LOG_TAG, "uploadFile: " + file.getPath());
            mDriveService.uploadFile(file);
        }

        public void handleRemoteDriveProblemSolved() {
            Log.d(LOG_TAG, "handleRemoteDriveProblemSolved");
            mDriveService.handleRemoteDriveProblemSolved();
        }

        public void setListener(DriveServiceBinderListener listener) {
            Log.d(LOG_TAG, "setListener");
            mDriveServiceBinderListener = listener;
        }

        void notifyFileUploaded(File file, Boolean isSuccess) {
            Log.d(LOG_TAG, "notifyFileUploaded");
            if (mDriveServiceBinderListener != null) {
                mDriveServiceBinderListener.onFileUploaded(file, isSuccess);
            }
        }

        void notifyFileListChanged() {
            Log.d(LOG_TAG, "notifyFileListChanged");
            if (mDriveServiceBinderListener != null) {
                mDriveServiceBinderListener.onFileListChanged();
            }
        }

        void notifySynchronizationStarted() {
            Log.d(LOG_TAG, "notifySynchronizationStarted");
            if (mDriveServiceBinderListener != null) {
                mDriveServiceBinderListener.onSynchronizationStarted();
            }
        }

        void notifySynchronisationFinished() {
            Log.d(LOG_TAG, "notifySynchronisationFinished");
            if (mDriveServiceBinderListener != null) {
                mDriveServiceBinderListener.onSynchronisationFinished();
            }
        }

        void notifyConnectedFailed(ConnectionResult connectionResult) {
            Log.d(LOG_TAG, "notifyConnectedFailed");
            if (mDriveServiceBinderListener != null) {
                mDriveServiceBinderListener.onConnectedFailed(connectionResult);
            }
        }

        public void updateConfiguration() {
            mDriveService.updateConfiguration();
        }
    }

    static public final String INTEND_STRING = "com.merann.smamonov.googledrive.DriveService";
    static private final int PERIODIC_START_REQUEST_CODE = 1;
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

        addMessageHandler(Message.REMOTE_DRIVE_START, new IMessageHandler() {
            @Override
            public void onIntent(Intent intent) {
                Log.e(LOG_TAG, "REMOTE_DRIVE_START");
                setRepeating();
                doSync();
            }
        });

        mStorageManager = new StorageManager(this,
                new StorageManager.StorageManagerListener() {
                    @Override
                    public void onFilesChanged() {
                        Log.d(LOG_TAG, "onFilesChanged");
                        if (mBinder != null) {
                            mBinder.notifyFileListChanged();
                        }
                    }

                    @Override
                    public void onFileUpload(File file, boolean isSuccess) {
                        Log.d(LOG_TAG, "onFileUpload");
                        mBinder.notifyFileUploaded(file, isSuccess);
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
        Log.d(LOG_TAG, "onDestroy");
        mStorageManager = null;
    }

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

    private void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "onConnectionFailed");

        if (mBinder != null) {
            mBinder.notifyConnectedFailed(connectionResult);
        } else {
            sendNotification(connectionResult);
        }
    }

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
        Log.d(LOG_TAG, "onBind");
        mBinder = new DriveServiceBinder(this);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnbind");
        mBinder = null;
        return true;
    }

    public List<Image> getImagesList() {
        Log.d(LOG_TAG, "getImagesList");
        return mStorageManager.getImages();
    }

    public void handleRemoteDriveProblemSolved() {
        Log.d(LOG_TAG, "handleRemoteDriveProblemSolved");
        mStorageManager.handleRemoteDriveProblemSolved();
    }

    private void setRepeating() {
        Log.d(LOG_TAG, "setRepeating");
        ConfigurationManager configurationManager = new ConfigurationManager(this);
        Configuration configuration = configurationManager.getConfiguration();

        Intent intent = new Intent(getApplicationContext(),
                DriveService.class)
                .putExtra(Message.class.getName(),
                        Message.REMOTE_DRIVE_START);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                PERIODIC_START_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                configuration.getSyncPeriod() * 1000,
                pendingIntent);
    }

    void updateConfiguration() {
        Log.d(LOG_TAG, "updateConfiguration");
        setRepeating();
    }
}
