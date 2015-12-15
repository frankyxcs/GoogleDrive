package com.merann.smamonov.googledrive.managers;

import android.content.Context;

import com.merann.smamonov.googledrive.model.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by samam_000 on 16.12.2015.
 */
public class StorageManager {

    public interface StorageManagerListener {
        void onFilesChanged();
    }

    private final static String LOG_TAG = "StorageManager";

    private HashMap<String, Image> mImages;
    private Context mContext;
    private StorageManagerListener mStorageManagerListener;

    private DiskCacheHelper mDiskCacheHelper;
    private DataBaseHelper mDataBaseHelper;

    public StorageManager(Context context,
                          StorageManagerListener storageManagerListener) {

        mContext = context;
        mStorageManagerListener = storageManagerListener;
        mDiskCacheHelper = new DiskCacheHelper(mContext);
        mDataBaseHelper = new DataBaseHelper(mContext);

        loadLocalImages();
    }

    public void loadLocalImages() {
        mImages = new HashMap();

        List<Image> localImages = mDataBaseHelper.getImagesFromDb();

        for (Image image : localImages) {

        }
    }

    public void addRemoteImage(Image remoteImage) {

        if (mImages.containsKey(remoteImage.getFileName())) {
            if (remoteImage.getBitmap() != null
                    && mDiskCacheHelper.getCachedIcon(remoteImage.getFileName()) == null) {
                // save icon to disk cache
                mDiskCacheHelper.saveIconToFile(remoteImage.getFileName(),
                        remoteImage.getBitmap());

                // notify about new file/icon
                mStorageManagerListener.onFilesChanged();
            }
        } else {
            // add remote image to database and load save icon in on disk cache
            mDataBaseHelper.add(remoteImage);
            if (remoteImage.getBitmap() != null) {
                mDiskCacheHelper.saveIconToFile(remoteImage.getFileName(),
                        remoteImage.getBitmap());
            }

            // update files map
            mImages.put(remoteImage.getFileName(), remoteImage);

            // notify about new file/icon
            mStorageManagerListener.onFilesChanged();
        }
    }

    public List<Image> getImages() {
        return new ArrayList<Image>(mImages.values());
    }

}
