package com.merann.smamonov.googledrive.service;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
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

    private final static String LOG_TAG = "DriveServiceData";

    private GoogleApiClient mGoogleApiClient;
    ConfigurationService.Configuration mCurrentConfiguration;
    private List<Metadata> mFiles = new ArrayList<>();
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

    public void setFiles(List<Metadata> mFiles) {
        this.mFiles = mFiles;
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
        Log.d(LOG_TAG, "createFolder newFolderName:" + newFolderName);

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
                            Log.d(LOG_TAG, "createFolder created folder:" + mDriveFolder.getDriveId());
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
                        Log.d(LOG_TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        Log.d(LOG_TAG, "getTitle:" + metadata.getTitle());
                        Log.d(LOG_TAG, "getDescription:" + metadata.getDescription());
                        Log.d(LOG_TAG, "getMimeType:" + metadata.getMimeType());
                        Log.d(LOG_TAG, "getWebContentLink:" + metadata.getWebContentLink());
                        Log.d(LOG_TAG, "getFileSize:" + metadata.getFileSize());
                        mFiles.add(metadata);
                        Log.d(LOG_TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    }
                }
            }
        });
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
                        Log.d(LOG_TAG, "searchForUserFolder::onResult");

                        MetadataBuffer metadataBuffer = metadataBufferResult.getMetadataBuffer();

                        if (metadataBuffer.getCount() == 0) {
                            createFolderAsync(mCurrentConfiguration
                                    .getFolderName());
                        } else {
                            for (int index = 0;
                                 index < metadataBuffer.getCount();
                                 index++) {
                                Metadata metadata = metadataBuffer.get(index);
                                if (metadata.isFolder()) {
                                    Log.d(LOG_TAG, "searchForUserFolder::onSuccess: "
                                            + index
                                            + ": "
                                            + metadata.getDriveId());

                                    mDriveFolder = metadata.getDriveId().asDriveFolder();
                                    Log.d(LOG_TAG, "searchForUserFolder::onSuccess: "
                                            + index
                                            + ": "
                                            + mDriveFolder.getDriveId());
                                    break;
                                }
                            }
                        }
                        onFolderExisted();
                    }
                });
    }

    public void onFolderCreated() {
        loadFileMetagata(mDriveFolder);
    }

    public void onFolderExisted() {
        loadFileMetagata(mDriveFolder);
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
}
