package com.merann.smamonov.googledrive.managers;

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

    static public final String MEDIA_STORAGE = "/mnt/extSdCard/DCIM/Camera";

    private HashMap<String, Image> mFiles = new HashMap<>();
    private Queue<Image> mImagesToBeLoaded = new LinkedList<Image>();
    private Thread mBitmapLoaderTask;
    private BitmapLoadedListener mBitmapLoadedListener;
    private String mSearchFolder;


    public LocalStorageManager(String searchFolder, BitmapLoadedListener bitmapLoadedListener) {
        mBitmapLoadedListener = bitmapLoadedListener;
        mSearchFolder = searchFolder;
    }

    public List<Image> getImagesList() {

        Log.d(LOG_TAG, "getImagesList");

        //TODO: get the folder by global name
        File picture_folder = new File(mSearchFolder);

        List<Image> result = new LinkedList();

        if (picture_folder != null) {
            File[] files = picture_folder.listFiles();

            Log.e(LOG_TAG, "files sie:" + files.length);

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
                        while (!mImagesToBeLoaded.isEmpty()) {
                            Image image = mImagesToBeLoaded.poll();
                            File file = getFileByFileName(image.getFileName());
                            image.setBitmap(ImageService.loadIcon(file));

                            Log.d(LOG_TAG, image.getFileName() + " bitmap was updated" );

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
        Log.d(LOG_TAG, "getFileByFileName");

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
}
