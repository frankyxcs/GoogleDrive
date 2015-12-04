package com.merann.smamonov.googledrive.service;

/**
 * Created by sergeym on 04.12.2015.
 */
public class ConfigurationService {
    final class Configuration {
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
    }

    private Configuration mCurrentConfiguration;

    public Configuration readConfiguration() {
        Configuration result = null;
        if (mCurrentConfiguration == null) {
            //read configuration from the properties
        } else {
            result = mCurrentConfiguration;
        }
        return result;
    }

    public void updateConfiguration() {

    }
}
