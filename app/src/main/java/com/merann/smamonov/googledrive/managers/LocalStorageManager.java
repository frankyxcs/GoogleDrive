package com.merann.smamonov.googledrive.managers;

import android.content.Context;
import android.util.Log;

import com.merann.smamonov.googledrive.model.Image;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by samam_000 on 10.12.2015.
 */
public class LocalStorageManager {

    public final String LOG_TAG = "LocalStorageManager";

    public interface BitmapLoadedListener {
        void onBitmapLoaded(String fileName);
    }

    public enum IMAGE_FOLDER {
        IMAGE_FOLDER_ICON_CACHE,
        IMAGE_FOLDER_IMAGE_CACHE,
        IMAGE_FOLDER_CAMERA
    }

    static private final String MEDIA_STORAGE = "/mnt/extSdCard/DCIM/Camera";

    private static String getImageFolderPath(Context context, IMAGE_FOLDER folder) {
        String result = null;

        switch (folder) {
            case IMAGE_FOLDER_ICON_CACHE: {
                DiskCacheHelper diskCacheHelper = new DiskCacheHelper(context);
                result = diskCacheHelper.getIconFolderPath();
                break;
            }
            case IMAGE_FOLDER_IMAGE_CACHE: {
                DiskCacheHelper diskCacheHelper = new DiskCacheHelper(context);
                result = diskCacheHelper.getImageFolderPath();
                break;
            }
            case IMAGE_FOLDER_CAMERA:
                result = MEDIA_STORAGE;
                break;
        }
        return result;
    }

    private HashMap<String, Image> mFiles = new HashMap<>();
    private Queue<Image> mImagesToBeLoaded = new LinkedList<Image>();
    private Thread mBitmapLoaderTask;
    private BitmapLoadedListener mBitmapLoadedListener;
    private String mSearchFolder;
    private Boolean isStopped;

    public LocalStorageManager(Context context,
                               IMAGE_FOLDER folderType,
                               BitmapLoadedListener bitmapLoadedListener) {
        mBitmapLoadedListener = bitmapLoadedListener;
        mSearchFolder = getImageFolderPath(context, folderType);
        isStopped = false;
    }

    public List<Image> getImagesList() {

        Log.d(LOG_TAG, "getImagesList");

        //TODO: get the folder by global name
        File picture_folder = new File(mSearchFolder);

        List<Image> result = new LinkedList();

        if (picture_folder != null) {
            File[] files = picture_folder.listFiles();

            for (File file : picture_folder.listFiles()) {
                if (!mFiles.containsKey(file.getName())) {
                    Image image = new Image(file.getName());
                    mFiles.put(file.getName(), image);

                    result.add(image);
                    mImagesToBeLoaded.add(image);
                }
            }

            if (mBitmapLoaderTask == null
                    && mImagesToBeLoaded.isEmpty() != true) {
                mBitmapLoaderTask = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while (!isStopped &&
                                !mImagesToBeLoaded.isEmpty()) {
                            Image image = mImagesToBeLoaded.poll();
                            File file = getFileByFileName(image.getFileName());
                            image.setBitmap(ImageService.loadIcon(file));

                            Log.d(LOG_TAG, image.getFileName() + " bitmap was updated");

                            mBitmapLoadedListener.onBitmapLoaded(image.getFileName());
                        }
                    }
                });
                mBitmapLoaderTask.start();
            }
        }

        return result;
    }

    public File getFileByFileName(String fileName) {
        Log.d(LOG_TAG,
                "getFileByFileName: "
                        + fileName);

        File result = null;
        File picture_folder = new File(mSearchFolder);
        if (picture_folder != null) {
            for (File file : picture_folder.listFiles()) {
                if (file.getName().equals(fileName)) {
                    result = file;
                    break;
                }
            }
        }
        return result;
    }

    public void stopLoading() {
        Log.d(LOG_TAG, "stopLoading");
        isStopped = true;
    }
}
