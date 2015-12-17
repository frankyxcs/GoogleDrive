package com.merann.smamonov.googledrive.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.merann.smamonov.googledrive.model.Configuration;
import com.merann.smamonov.googledrive.model.Image;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeym on 07.12.2015.
 */

public class RemoteStorageManager {

    public class RemoteDriveFile extends Image {
        Metadata mMetadata;

        public RemoteDriveFile(Metadata metadata) {
            super(metadata.getTitle());
            this.mMetadata = metadata;
        }

        public Metadata getMetadata() {
            return mMetadata;
        }

        public void setMetadata(Metadata mMetadata) {
            this.mMetadata = mMetadata;
        }

        public Image getImage() {
            return new Image(getMetadata().getTitle(),
                    getBitmap());
        }
    }

    public interface RemoteStorageManagerListener {

        void onFileUpload(File file, boolean isSuccess);

        void onConnectionFailed(ConnectionResult connectionResult);

        void onConnectionEstablished();

        void onConnectionSuspended();

        void onNewFileDetected(RemoteDriveFile file);
    }


    private final static String LOG_TAG = "RemoteStorageManager";

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    Configuration mCurrentConfiguration;
    private List<RemoteDriveFile> mFiles = new ArrayList<>();
    DriveFolder mDriveFolder;
    RemoteStorageManagerListener mRemoteStorageManagerListener;
    ChangeListener mChangeListener;

    public RemoteStorageManager(Context context,
                                RemoteStorageManagerListener remoteStorageManagerListener) {
        mContext = context;
        mRemoteStorageManagerListener = remoteStorageManagerListener;
        ConfigurationManager configurationManager = new ConfigurationManager(mContext);
        mCurrentConfiguration = configurationManager.getConfiguration();
    }

//    public void setIsConnectionRequested(boolean isConnectionRequested) {
//        this.mIsConnectionRequested = isConnectionRequested;
//    }

//    public ConfigurationService.Configuration getCurrentConfiguration() {
//        return mCurrentConfiguration;
//    }

    public boolean isConnected() {
        return mGoogleApiClient != null
                && mGoogleApiClient.isConnected();
    }

//    public void setCurrentConfiguration(ConfigurationService.Configuration mCurrentConfiguration) {
//        this.mCurrentConfiguration = mCurrentConfiguration;
//    }

    public void setOnNewFileListener(RemoteStorageManagerListener remoteStorageManagerListener) {
        this.mRemoteStorageManagerListener = remoteStorageManagerListener;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public Bitmap getImageByFilename(String fileName) {
        Bitmap result = null;
        for (RemoteDriveFile file : mFiles) {
            if (file.getMetadata().getTitle().equals(fileName)) {
                result = file.getBitmap();
            }
        }
        return result;
    }

    private PendingResult<DriveFolder.DriveFolderResult> prepareCreateFolderRequest(final String newFolderName) {

        Log.d(LOG_TAG, "prepareCreateFolderRequest: " + newFolderName);

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(newFolderName)
                .build();

        DriveFolder rootFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);

        return rootFolder.createFolder(mGoogleApiClient, changeSet);
    }

    private void handleCreateFolderResponse(DriveFolder.DriveFolderResult driveFolderResult,
                                            final String newFolderName) {

        Log.d(LOG_TAG, "handleCreateFolderResponse: " + newFolderName);

        if (driveFolderResult.getStatus().isSuccess()) {
            mDriveFolder = driveFolderResult.getDriveFolder();

            if (mDriveFolder != null) {
                Log.d(LOG_TAG, "createFolderSync: new folder"
                        + newFolderName
                        + " was created");
            }
        } else {
            Log.e(LOG_TAG, "createFolderSync: unable to create new folder"
                    + newFolderName
                    + " reason: "
                    + driveFolderResult.getStatus().getStatusMessage());
        }
    }

    void createFolderAsync(final String newFolderName) {
        Log.d(LOG_TAG, "createFolderAsync: " + newFolderName);

        prepareCreateFolderRequest(newFolderName)
                .setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
                    @Override
                    public void onResult(DriveFolder.DriveFolderResult driveFolderResult) {
                        handleCreateFolderResponse(driveFolderResult, newFolderName);

                        if (mDriveFolder != null) {
                            onFolderCreated();
                        }
                    }
                });
    }

    void createFolderSync(final String newFolderName) {
        Log.d(LOG_TAG, "createFolderSync: " + newFolderName);

        DriveFolder.DriveFolderResult driveFolderResult = prepareCreateFolderRequest(newFolderName).await();

        handleCreateFolderResponse(driveFolderResult, newFolderName);

        if (mDriveFolder != null) {
            onFolderCreated();
        }
    }

    private PendingResult<DriveApi.MetadataBufferResult> prepareGetImageFilesRequest() {
        Log.d(LOG_TAG, "prepareGetImageFilesRequest");
        Query query = new Query.Builder().addFilter(Filters.or(
                Filters.eq(SearchableField.MIME_TYPE,
                        "image/jpeg"),
                Filters.eq(SearchableField.MIME_TYPE,
                        "image/png")))
                .build();
        return mDriveFolder.queryChildren(mGoogleApiClient, query);
    }

    private void handleGetImageFilesResponse(DriveApi.MetadataBufferResult metadataBufferResult) {
        Log.d(LOG_TAG, "handleGetImageFilesResponse");

        MetadataBuffer metadataBuffer = metadataBufferResult.getMetadataBuffer();

        if (metadataBuffer.getCount() == 0) {
            Log.d(LOG_TAG, "handleGetImageFilesResponse: folder has no files");
        } else {
            for (int index = 0;
                 index < metadataBuffer.getCount();
                 index++) {
                Metadata metadata = metadataBuffer.get(index);
                printFileInfo(metadata);

                RemoteDriveFile file = new RemoteDriveFile(metadata);
                mFiles.add(file);
                mRemoteStorageManagerListener.onNewFileDetected(file);
            }
        }
    }

    public void getFileListSync() {
        Log.d(LOG_TAG, "getImageFilesSync");
        DriveApi.MetadataBufferResult metadataBufferResult = prepareGetImageFilesRequest().await();
        handleGetImageFilesResponse(metadataBufferResult);
//        downloadFilesSync();
    }

    public void getFileListAsync() {
        Log.d(LOG_TAG, "getImageFilesAsync");

        prepareGetImageFilesRequest()
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                        Log.d(LOG_TAG, "getImeageFiles::onResult:");
                        handleGetImageFilesResponse(metadataBufferResult);
//                        downloadFilesAsync();
                    }
                });
    }

    void loadFileMetagata(DriveFolder driveFolder) {
        Log.d(LOG_TAG, "loadFileMetagata");

        final PendingResult<DriveApi.MetadataBufferResult> result = driveFolder.listChildren(mGoogleApiClient);
        result.setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                Log.d(LOG_TAG, "loadFileMetagata::onResult: ");

                MetadataBuffer metadataBuffer = metadataBufferResult.getMetadataBuffer();

                if (metadataBuffer.getCount() == 0) {
                    Log.d(LOG_TAG, "loadFileMetagata::onResult: folder has no files");
                } else {

                    for (int index = 0;
                         index < metadataBuffer.getCount();
                         index++) {
                        Metadata metadata = metadataBuffer.get(index);
                        printFileInfo(metadata);
                        mFiles.add(new RemoteDriveFile(metadata));
                    }
                }
            }
        });
    }

    private void printFileInfo(Metadata metadata) {
        Log.d(LOG_TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        Log.d(LOG_TAG, "getAlternateLink:" + metadata.getAlternateLink());
        Log.d(LOG_TAG, "getContentAvailability:" + metadata.getContentAvailability());
        Log.d(LOG_TAG, "getCreatedDate:" + metadata.getCreatedDate());
        Log.d(LOG_TAG, "getDescription:" + metadata.getDescription());
        Log.d(LOG_TAG, "getDriveId:" + metadata.getDriveId());
        Log.d(LOG_TAG, "getEmbedLink:" + metadata.getEmbedLink());
        Log.d(LOG_TAG, "getFileExtension:" + metadata.getFileExtension());
        Log.d(LOG_TAG, "getFileSize:" + metadata.getFileSize());
        Log.d(LOG_TAG, "getLastViewedByMeDate:" + metadata.getLastViewedByMeDate());
        Log.d(LOG_TAG, "getMimeType:" + metadata.getMimeType());
        Log.d(LOG_TAG, "getModifiedByMeDate:" + metadata.getModifiedByMeDate());
        Log.d(LOG_TAG, "getModifiedDate:" + metadata.getModifiedDate());
        Log.d(LOG_TAG, "getOriginalFilename:" + metadata.getOriginalFilename());
        Log.d(LOG_TAG, "getQuotaBytesUsed:" + metadata.getQuotaBytesUsed());
        Log.d(LOG_TAG, "getSharedWithMeDate:" + metadata.getSharedWithMeDate());
        Log.d(LOG_TAG, "getTitle:" + metadata.getTitle());
        Log.d(LOG_TAG, "getWebContentLink:" + metadata.getWebContentLink());
        Log.d(LOG_TAG, "getWebViewLink:" + metadata.getWebViewLink());
        Log.d(LOG_TAG, "isEditable:" + metadata.isEditable());
        Log.d(LOG_TAG, "isExplicitlyTrashed:" + metadata.isExplicitlyTrashed());
        Log.d(LOG_TAG, "isFolder:" + metadata.isFolder());
        Log.d(LOG_TAG, "isInAppFolder:" + metadata.isInAppFolder());
        Log.d(LOG_TAG, "isPinnable:" + metadata.isPinnable());
        Log.d(LOG_TAG, "isPinned:" + metadata.isPinned());
        Log.d(LOG_TAG, "isRestricted:" + metadata.isRestricted());
        Log.d(LOG_TAG, "isShared:" + metadata.isShared());
        Log.d(LOG_TAG, "isStarred:" + metadata.isStarred());
        Log.d(LOG_TAG, "isTrashable:" + metadata.isTrashable());
        Log.d(LOG_TAG, "isTrashed:" + metadata.isTrashed());
        Log.d(LOG_TAG, "isViewed:" + metadata.isViewed());
        Log.d(LOG_TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    public void onFolderCreated() {
        Log.d(LOG_TAG, "onFolderCreated");
        mRemoteStorageManagerListener.onConnectionEstablished();
    }

    public void onFolderExisted() {
        Log.d(LOG_TAG, "onFolderExisted");
        mRemoteStorageManagerListener.onConnectionEstablished();
    }

    private void handleGetUserFolderResult(DriveApi.MetadataBufferResult metadataBufferResult) {
        MetadataBuffer metadataBuffer = metadataBufferResult.getMetadataBuffer();

        Log.d(LOG_TAG, "handleGetUserFolderResult:" + metadataBuffer.getCount() + " results");

        if (metadataBuffer.getCount() == 0) {
            Log.d(LOG_TAG, "handleGetUserFolderResult user folder doesn't exists");
            createFolderAsync(mCurrentConfiguration
                    .getFolderName());
        } else if (metadataBuffer.getCount() == 1) {
            Metadata metadata = metadataBuffer.get(0);
            printFileInfo(metadata);
            mDriveFolder = metadata.getDriveId().asDriveFolder();

            if (mDriveFolder != null) {
                printFileInfo(metadata);
            } else {
                Log.e(LOG_TAG, "handleGetUserFolderResult: unable to convert "
                        + metadata.getDriveId()
                        + " to DriveFolder");
                //TODO: remove all results here
            }
        } else {
            Log.e(LOG_TAG, "handleGetUserFolderResult: more than one user folder exist");
            for (Metadata metadata : metadataBuffer) {
                printFileInfo(metadata);
            }
        }
    }

    public void getUserFolderAsync() {
        prepareGetUserFolderRequest()
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                        handleGetUserFolderResult(metadataBufferResult);

                        if (mDriveFolder != null) {
                            onFolderExisted();
                        } else {
                            createFolderAsync(mCurrentConfiguration.getFolderName());
                        }
                    }
                });
    }


    public void getUserFolderSync() {
        DriveApi.MetadataBufferResult searchResult = prepareGetUserFolderRequest().await();
        handleGetUserFolderResult(searchResult);

        if (mDriveFolder != null) {
            onFolderExisted();
        } else {
            createFolderSync(mCurrentConfiguration.getFolderName());
        }
    }

    private PendingResult<DriveApi.MetadataBufferResult> prepareGetUserFolderRequest() {
        DriveFolder rootFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);

        Query query = new Query.Builder().addFilter(Filters.and(
                Filters.eq(SearchableField.TITLE,
                        mCurrentConfiguration.getFolderName()),
                Filters.eq(SearchableField.MIME_TYPE,
                        "application/vnd.google-apps.folder")))
                .build();

        return rootFolder.queryChildren(mGoogleApiClient, query);
    }

    private PendingResult<DriveApi.DriveContentsResult> prepareCreateRemoteFileRequest() {
        Log.d(LOG_TAG, "prepareCreateRemoteFileRequest");
        return Drive.DriveApi.newDriveContents(mGoogleApiClient);
    }

    private PendingResult<DriveFolder.DriveFileResult> prepapareRemoteFileContetnt(DriveApi.DriveContentsResult driveContentsResult,
                                                                                   final File file) {
        Log.d(LOG_TAG, "handleCreateRemoteFileResponse");

        final MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setTitle(file.getName())
                .build();

        DriveContents driveContents = driveContentsResult.getDriveContents();
        OutputStream outputStream = driveContents.getOutputStream();

        try {
            InputStream inputStream = new FileInputStream(file);
            IOUtils.copy(inputStream, outputStream);
            outputStream.close();
            inputStream.close();
        } catch (Throwable throwable) {
            Log.d(LOG_TAG, "throwable: " + throwable.getMessage());
        }

        return mDriveFolder.createFile(mGoogleApiClient,
                metadataChangeSet,
                driveContents);
    }

    private void handleUploadFileResult(DriveFolder.DriveFileResult driveFileResult,
                                        final File file) {
        Log.d(LOG_TAG, "handleUploadFileResult");

        boolean result = driveFileResult.getStatus().isSuccess();
        if (driveFileResult.getStatus().isSuccess()) {
            DriveFile driveFile = driveFileResult.getDriveFile();
            if (driveFile != null) {
                Log.d(LOG_TAG, "handleUploadFileResult: file "
                        + file.getName()
                        + " was successfully uploaded");
            }
        } else {
            Log.e(LOG_TAG, "handleUploadFileResult: unable to upload file "
                    + file.getName()
                    + ", message " +
                    driveFileResult.getStatus().getStatusMessage());
        }

        mRemoteStorageManagerListener.onFileUpload(file, result);
    }

    public void uploadFileSync(final File file) {
        Log.d(LOG_TAG, "uploadFileSync");
        DriveApi.DriveContentsResult driveContentsResult = prepareCreateRemoteFileRequest().await();
        DriveFolder.DriveFileResult driveFileResult = prepapareRemoteFileContetnt(driveContentsResult, file)
                .await();
        handleUploadFileResult(driveFileResult, file);
    }

    public void uploadFileAsync(final File file) {
        Log.d(LOG_TAG, "uploadFileAsync:" + file.getPath());
        prepareCreateRemoteFileRequest()
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                        Log.d(LOG_TAG, "uploadFileAsync:onResult " + file.getPath());
                        prepapareRemoteFileContetnt(driveContentsResult, file)
                                .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                                       @Override
                                                       public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                                                           handleUploadFileResult(driveFileResult, file);
                                                       }
                                                   }
                                );
                    }
                });
    }

    private void deleteUserFolder() {
        mDriveFolder.delete(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Log.d(LOG_TAG, "deleteUserFolder::onResult:" + status);
            }
        });
    }

    private void downloadFilesSync() {
        for (RemoteDriveFile file : mFiles) {
            downloadFileSync(file);
        }
    }

    private void downloadFilesAsync() {
        for (RemoteDriveFile file : mFiles) {
            downloadFileAsync(file);
        }
    }

    private void handleRemoteFileDownloadProgress(final RemoteDriveFile file,
                                                  long downloadedBytes,
                                                  long totalBytes) {
        Log.d(LOG_TAG, "handleRemoteFileDownloadProgress: " + file.getMetadata().getTitle()
                + ": "
                + downloadedBytes +
                "/"
                + totalBytes);
    }

    private PendingResult<DriveApi.DriveContentsResult> prepareOpenRemoteFileRequest(final RemoteDriveFile file) {
        Log.d(LOG_TAG, "prepareOpenRemoteFileRequest: " + file.getMetadata().getTitle());

        DriveId driveId = file.getMetadata().getDriveId();
        final DriveFile driveFile = driveId.asDriveFile();
        final String title = file.getMetadata().getTitle();

        return driveFile.open(mGoogleApiClient,
                DriveFile.MODE_READ_ONLY,
                new DriveFile.DownloadProgressListener() {
                    @Override
                    public void onProgress(long downloadedBytes, long totalBytes) {
                        handleRemoteFileDownloadProgress(file, downloadedBytes, totalBytes);
                    }
                });
    }

    private BitmapFactory.Options handleOpenRemoteFileResponseToGetImageOptions(final RemoteDriveFile file,
                                                                                DriveApi.DriveContentsResult driveContentsResult) {
        Log.d(LOG_TAG, "handleOpenRemoteFileResponse: " + file.getMetadata().getTitle());

        BitmapFactory.Options result = null;
        if (driveContentsResult.getStatus().isSuccess()) {
            DriveContents driveContents = driveContentsResult.getDriveContents();

            if (driveContents != null) {
                InputStream inputStream = driveContents.getInputStream();
                result = ImageService.getIconOptions(inputStream);
            }
        } else {
            Log.e(LOG_TAG, "handleOpenRemoteFileResponse: unable to open file "
                    + file.getMetadata().getTitle()
                    + "reason: "
                    + driveContentsResult.getStatus().getStatusMessage());
        }
        return result;
    }

    private Bitmap handleOpenRemoteFileResponseToLoadFile(final RemoteDriveFile file,
                                                          DriveApi.DriveContentsResult driveContentsResult,
                                                          BitmapFactory.Options bitmapOptions) {
        Bitmap result = null;
        if (driveContentsResult.getStatus().isSuccess()) {
            DriveContents driveContents = driveContentsResult.getDriveContents();

            if (driveContents != null) {
                InputStream inputStream = driveContents.getInputStream();
                result = ImageService.loadIcon(inputStream, bitmapOptions);
            }
        } else {
            Log.e(LOG_TAG, "handleOpenRemoteFileResponseToLoadFile: unable to open file "
                    + file.getMetadata().getTitle()
                    + "reason: "
                    + driveContentsResult.getStatus().getStatusMessage());
        }
        return result;
    }

    private void downloadFileSync(final RemoteDriveFile file) {
        Log.d(LOG_TAG, "downloadFileSync: " + file.getMetadata().getTitle());
        DriveApi.DriveContentsResult driveContentsResult = prepareOpenRemoteFileRequest(file).await();

        BitmapFactory.Options bitmapOptions = handleOpenRemoteFileResponseToGetImageOptions(file, driveContentsResult);

        if (bitmapOptions != null) {

            driveContentsResult = prepareOpenRemoteFileRequest(file).await();
            Bitmap bitmap = handleOpenRemoteFileResponseToLoadFile(file, driveContentsResult, bitmapOptions);

            if (bitmap != null) {
                onImageLoaded(file, bitmap);
            }
        }
    }

    private void downloadFileAsync(final RemoteDriveFile file) {
        prepareOpenRemoteFileRequest(file)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                        final BitmapFactory.Options bitmapOptions
                                = handleOpenRemoteFileResponseToGetImageOptions(file, driveContentsResult);

                        if (bitmapOptions != null) {
                            prepareOpenRemoteFileRequest(file)
                                    .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                                        @Override
                                        public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                                            Bitmap bitmap
                                                    = handleOpenRemoteFileResponseToLoadFile(file,
                                                    driveContentsResult,
                                                    bitmapOptions);

                                            if (bitmap != null) {
                                                //file.setBitmap(bitmap);
                                                onImageLoaded(file, bitmap);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void onImageLoaded(final RemoteDriveFile file,
                               Bitmap bitmap) {
        Log.d(LOG_TAG, "onImageLoaded:"
                + file.getMetadata().getTitle()
                + " size:"
                + bitmap.getByteCount());

        file.setBitmap(bitmap);

        mRemoteStorageManagerListener.onNewFileDetected(file);
    }

    public List<Image> getImagesList() {
        List<Image> result = new ArrayList<>();

        for (RemoteDriveFile remoteFileFile : mFiles) {
            result.add(new Image(remoteFileFile.getMetadata().getTitle(),
                    remoteFileFile.getBitmap()));
        }
        return result;
    }

    private PendingResult<Status> prepareSubscriptionToStateChangeRequest() {
        //todo: unsubscribe if not null
        mChangeListener = new ChangeListener() {
            @Override
            public void onChange(ChangeEvent changeEvent) {

            }
        };

        return mDriveFolder.addChangeListener(mGoogleApiClient, mChangeListener);
    }

    private void handleSubscriptionToStateChangeResponse(Status status) {
        if (status.isSuccess()) {

        } else {
            Log.e(LOG_TAG, "Unable to subscribe on folder" +
                    mCurrentConfiguration.getFolderName()
                    + "state change, reason:"
                    + status.getStatusMessage());
        }
    }

    private void subscribeToFolderStateChangeSync() {
        Status status = prepareSubscriptionToStateChangeRequest().await();
        handleSubscriptionToStateChangeResponse(status);
    }

    private void subscribeToFolderStateChangeAsync() {
        prepareSubscriptionToStateChangeRequest().setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                handleSubscriptionToStateChangeResponse(status);
            }
        });
    }

    private void unsubscribeFromFolderStateChangeSync() {
        Status status = prepareUnsubscriptionFromStateChangeRequest().await();
        handleUnsubscriptionFromStateChangeResponse(status);
    }

    private void unsubscribeFromFolderStateChangeAsync() {
        prepareUnsubscriptionFromStateChangeRequest().setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                handleUnsubscriptionFromStateChangeResponse(status);
            }
        });
    }

    private PendingResult<Status> prepareUnsubscriptionFromStateChangeRequest() {
        return mDriveFolder.removeChangeListener(mGoogleApiClient, mChangeListener);
    }

    private void handleUnsubscriptionFromStateChangeResponse(Status status) {
        if (status.isSuccess()) {

        } else {
            Log.e(LOG_TAG, "Unable to subscribe on folder" +
                    mCurrentConfiguration.getFolderName()
                    + "state change, reason:"
                    + status.getStatusMessage());
        }
    }

    public void connect() {
        Log.d(LOG_TAG, "connect");

        if (mGoogleApiClient != null) {
            if (isConnected()) {
                mRemoteStorageManagerListener.onConnectionEstablished();
            } else {
                mGoogleApiClient.connect();
            }
        } else {
            Log.d(LOG_TAG, "connect mGoogleApiClient doesn't exists");
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Log.d(LOG_TAG, "onConnected");
                            getUserFolderAsync();
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.d(LOG_TAG, "onConnectionSuspended");
                            mRemoteStorageManagerListener.onConnectionSuspended();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            Log.e(LOG_TAG, "onConnectionFailed");
                            mRemoteStorageManagerListener.onConnectionFailed(connectionResult);
                        }
                    })
                    .build();
            mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
    }

}
