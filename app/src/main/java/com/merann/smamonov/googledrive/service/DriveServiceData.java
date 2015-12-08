package com.merann.smamonov.googledrive.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

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
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

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
public class DriveServiceData {

    private class RemoteFileFile {
        Metadata mMetadata;
        Bitmap mBitmap;

        public Metadata getMetadata() {
            return mMetadata;
        }

        public void setMetadata(Metadata mMetadata) {
            this.mMetadata = mMetadata;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public void setBitmap(Bitmap mBitmap) {
            this.mBitmap = mBitmap;
        }

        public RemoteFileFile(Metadata metadata) {
            this.mMetadata = metadata;
        }
    }

    private final static String LOG_TAG = "DriveServiceData";

    private GoogleApiClient mGoogleApiClient;
    ConfigurationService.Configuration mCurrentConfiguration;
    private List<RemoteFileFile> mFiles = new ArrayList<>();
    boolean mIsConnectionRequested;
    DriveFolder mDriveFolder;

    private static DriveServiceData ourInstance = new DriveServiceData();

    public static DriveServiceData getInstance() {
        return ourInstance;
    }

    private DriveServiceData() {
    }

    public boolean isConnectionRequested() {
        return mIsConnectionRequested;
    }

    public void setIsConnectionRequested(boolean isConnectionRequested) {
        this.mIsConnectionRequested = isConnectionRequested;
    }

    public ConfigurationService.Configuration getCurrentConfiguration() {
        return mCurrentConfiguration;
    }

    public void setCurrentConfiguration(ConfigurationService.Configuration mCurrentConfiguration) {
        this.mCurrentConfiguration = mCurrentConfiguration;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public static DriveServiceData getOurInstance() {
        return ourInstance;
    }

    public static void setOurInstance(DriveServiceData ourInstance) {
        DriveServiceData.ourInstance = ourInstance;
    }

    void createFolderAsync(final String newFolderName) {
        Log.d(LOG_TAG, "createFolder new folder to be created:" + newFolderName);

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(newFolderName)
                .build();

        DriveFolder rootFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);

        rootFolder.createFolder(mGoogleApiClient,
                changeSet)
                .setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
                    @Override
                    public void onResult(DriveFolder.DriveFolderResult driveFolderResult) {
                        mDriveFolder = driveFolderResult.getDriveFolder();
                        if (mDriveFolder != null) {
                            Log.d(LOG_TAG, "new user folder:" +
                                    newFolderName
                                    + " was created");
                            onFolderCreated();
                        }
                    }
                });
    }

    void createFolderSync(final String newFolderName, DriveFolder parentFolder) {
        Log.d(LOG_TAG, "createFolderSync newFolderName:" + newFolderName);

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(newFolderName)
                .build();

        DriveFolder.DriveFolderResult driveFolderResult = parentFolder.createFolder(mGoogleApiClient,
                changeSet).await();

        mDriveFolder = driveFolderResult.getDriveFolder();

        if (mDriveFolder != null) {
            Log.d(LOG_TAG, "createFolder created folder:" + mDriveFolder.getDriveId());
        }
    }

    private void getImeageFiles(DriveFolder driveFolder) {
        Log.d(LOG_TAG, "getImeageFiles");

        Query query = new Query.Builder().addFilter(Filters.or(
                Filters.eq(SearchableField.MIME_TYPE,
                        "image/jpeg"),
                Filters.eq(SearchableField.MIME_TYPE,
                        "image/png")))
                .build();
        driveFolder.queryChildren(mGoogleApiClient, query)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                        Log.d(LOG_TAG, "getImeageFiles::onResult:");

                        MetadataBuffer metadataBuffer = metadataBufferResult.getMetadataBuffer();

                        if (metadataBuffer.getCount() == 0) {
                            Log.d(LOG_TAG, "getImeageFiles::onResult: folder has no files");
                        } else {
                            for (int index = 0;
                                 index < metadataBuffer.getCount();
                                 index++) {
                                Metadata metadata = metadataBuffer.get(index);
                                printFileInfo(metadata);
                                mFiles.add(new RemoteFileFile(metadata));
                            }
                        }
                        downloadFiles();
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
                        mFiles.add(new RemoteFileFile(metadata));
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

    public void getFilesAsync() {
        // get root folder
        DriveFolder rootFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);

        Query query = new Query.Builder().addFilter(Filters.and(Filters.eq(
                        SearchableField.TITLE,
                        mCurrentConfiguration.getFolderName()),
                Filters.eq(SearchableField.MIME_TYPE,
                        "application/vnd.google-apps.folder")))
                .build();

        rootFolder.queryChildren(mGoogleApiClient, query)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                        MetadataBuffer metadataBuffer = metadataBufferResult.getMetadataBuffer();

                        Log.d(LOG_TAG, "searchForUserFolder::onResult: " + metadataBuffer.getCount() + " results");

                        if (metadataBuffer.getCount() == 0) {
                            Log.d(LOG_TAG, "searchForUserFolder::onResult used folder doesn't exists");
                            createFolderAsync(mCurrentConfiguration
                                    .getFolderName());
                        } else {
                            for (int index = 0;
                                 index < metadataBuffer.getCount();
                                 index++) {
                                Metadata metadata = metadataBuffer.get(index);
                                if (metadata.isFolder()) {

                                    printFileInfo(metadata);

                                    mDriveFolder = metadata.getDriveId().asDriveFolder();
                                    if (mDriveFolder != null) {
                                        Log.d(LOG_TAG, "searchForUserFolder::onSuccess: "
                                                + index
                                                + ": "
                                                + mDriveFolder.getDriveId());
                                        onFolderExisted();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
    }

    public void onFolderCreated() {
        getImeageFiles(mDriveFolder);
    }

    public void onFolderExisted() {
        getImeageFiles(mDriveFolder);
    }

    public void getFilesSync() {
        // get root folder
        DriveFolder rootFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);

        Query query = new Query.Builder().addFilter(Filters.and(
                Filters.eq(SearchableField.TITLE,
                        mCurrentConfiguration.getFolderName()),
                Filters.eq(SearchableField.MIME_TYPE,
                        "application/vnd.google-apps.folder")))
                .build();

        DriveApi.MetadataBufferResult searchResult = rootFolder
                .queryChildren(mGoogleApiClient, query)
                .await();
        MetadataBuffer metadataBuffer = searchResult.getMetadataBuffer();

        if (metadataBuffer.getCount() == 0) {
            createFolderSync(mCurrentConfiguration.getFolderName(), rootFolder);
        } else {
            for (int index = 0;
                 index < metadataBuffer.getCount();
                 index++) {
                Metadata metadata = metadataBuffer.get(index);
                Log.d(LOG_TAG, "searchForUserFolder::onSuccess: " + index + ": " + metadata.getTitle());
                if (metadata.isFolder()) {
                    mDriveFolder = metadata.getDriveId().asDriveFolder();
                    break;
                }
            }
        }
    }

    public void uploadFile(final File file) {

        final MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setTitle(file.getName())
                .build();

        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
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
                        //driveContents.commit();

                        mDriveFolder.createFile(mGoogleApiClient,
                                metadataChangeSet,
                                driveContents)
                                .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                                       @Override
                                                       public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                                                           DriveFile driveFile = driveFileResult.getDriveFile();
                                                           if (driveFile != null) {
                                                               Log.d(LOG_TAG, "uploadFile: "
                                                                       + file.getName()
                                                                       + " was successfull");
                                                           }
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

    private void downloadFiles() {
        for (RemoteFileFile file : mFiles) {
            downloadFile(file);
        }
    }

    private void downloadFile(final RemoteFileFile file) {

        DriveId driveId = file.getMetadata().getDriveId();
        final DriveFile driveFile = driveId.asDriveFile();
        final String title = file.getMetadata().getTitle();

        driveFile.open(mGoogleApiClient,
                DriveFile.MODE_READ_ONLY,
                new DriveFile.DownloadProgressListener() {
                    @Override
                    public void onProgress(long l, long l1) {
                        Log.d(LOG_TAG, "downloadFile::onProgress: "
                                + title
                                + " "
                                + l +
                                "/"
                                + l1);
                    }
                }).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                Log.d(LOG_TAG, "downloadFile::onResult: " + title);

                if (driveContentsResult.getStatus().isSuccess()) {

                    Log.d(LOG_TAG, "downloadFile::onResult:"
                            + driveContentsResult);

                    DriveContents driveContents = driveContentsResult.getDriveContents();
                    if (driveContents != null) {
                        InputStream inputStream = driveContents.getInputStream();
                        final BitmapFactory.Options bitmabOptions = ImageService.getIconOptions(inputStream);

                        driveFile.open(mGoogleApiClient,
                                DriveFile.MODE_READ_ONLY,
                                null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                            @Override
                            public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                                if (driveContentsResult.getStatus().isSuccess()) {

                                    Log.d(LOG_TAG, "downloadFile::onResult:"
                                            + driveContentsResult);

                                    DriveContents driveContents = driveContentsResult.getDriveContents();
                                    InputStream inputStream = driveContents.getInputStream();
                                    Bitmap bitmap = ImageService.loadIcon(inputStream, bitmabOptions);
                                    onImageLoaded(file, bitmap);
                                }
                            }
                        });
                    } else {
                        Log.e(LOG_TAG, "downloadFile::onResult: unable to open file:"
                                + file.getMetadata().getTitle());
                    }
                }
            }
        });
    }


    private void onImageLoaded(final RemoteFileFile file,
                               Bitmap bitmap) {
        Log.d(LOG_TAG, "onImageLoaded:"
                + file.getMetadata().getTitle()
                + " size:"
                + bitmap.getByteCount());
        file.setBitmap(bitmap);
    }
}
