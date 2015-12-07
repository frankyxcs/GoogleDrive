package com.merann.smamonov.googledrive.service;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeym on 07.12.2015.
 */
public class DriveServiceData {

    private GoogleApiClient mGoogleApiClient;
    ConfigurationService.Configuration mCurrentConfiguration;
    private List<Metadata> mFiles = new ArrayList<>();
    boolean mIsConnectionRequested;

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

    public List<Metadata> getFiles() {
        return mFiles;
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
}
