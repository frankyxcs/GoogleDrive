package com.merann.smamonov.googledrive.managers;

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

    public interface BitmapLoadedListener {
        void onBitmapLoaded(String fileName);
    }

    private static LocalStorageManager ourInstance = new LocalStorageManager();
    private HashMap<String, Image> mFiles = new HashMap<>();
    private Queue<Image> mImagesToBeLoaded = new LinkedList<Image>();
    private Thread mBitmapLoaderTask;
    private BitmapLoadedListener mBitmapLoadedListener;

    public static LocalStorageManager getInstance() {
        return ourInstance;
    }

    private LocalStorageManager() {
    }

    public List<Image> getImagesList(BitmapLoadedListener bitmapLoadedListener) {

        mBitmapLoadedListener = bitmapLoadedListener;
        //TODO: get the folder by global name
        File picture_folder = new File("/mnt/extSdCard/DCIM/Camera");
        if (picture_folder != null) {
            for (File file : picture_folder.listFiles()) {
                if (!mFiles.containsKey(file.getName())) {
                    Image image = new Image(file.getName());
                    mFiles.put(file.getName(), image);
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
                            mBitmapLoadedListener.onBitmapLoaded(image.getFileName());
                        }
                    }
                });
                mBitmapLoaderTask.start();

            }
        }

        List<Image> result = new LinkedList<>(mFiles.values());
        return result;
    }

    public File getFileByFileName(String fileName) {
        File result = null;
        File picture_folder = new File("/mnt/extSdCard/DCIM/Camera");
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
