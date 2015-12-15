package com.merann.smamonov.googledrive.model;

import java.io.Serializable;

/**
 * Created by samam_000 on 15.12.2015.
 */
public class Configuration implements Serializable {
    int mSyncPeriod;
    String mFolderName;

    public String getFolderName() {
        return mFolderName;
    }

    public void setFolderName(String folderName) {
        this.mFolderName = folderName;
    }

    public int getSyncPeriod() {
        return mSyncPeriod;
    }

    public void setSyncPeriod(int syncPeriod) {
        this.mSyncPeriod = syncPeriod;
    }

    public Configuration(String folderName, int syncPeriod) {
        this.mFolderName = folderName;
        this.mSyncPeriod = syncPeriod;
    }
}