package com.merann.smamonov.googledrive.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;

/**
 * Created by samam_000 on 15.12.2015.
 */
public class DiskCacheHelper {
    private final static String LOG_TAG = "DiskCacheHelper";
    private final static String CacheDirectoryForIcons = "IconsCache";
    private final static String CacheDirectoryForImages = "ImagesCache";

    private File mIconCacheFolder;
    private File mImageCacheFolder;

    DiskCacheHelper(Context context) {
        CacheDirectoryForIcons(context);
        CacheDirectoryForImages(context);
    }

    private static File getDiskCacheDir(Context context, String subfolder) {
        final File cacheDirectory = context.getExternalCacheDir();
        File result = null;

        if (cacheDirectory.isDirectory()) {
            File[] files = cacheDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });

            for (File file : files) {
                if (file.getName().equals(subfolder)) {
                    result = file;
                    break;
                }
            }

            if (result == null) {
                result = new File(cacheDirectory.getPath() + "/" + subfolder);
                if (result.mkdir()) {
                    Log.d(LOG_TAG, "Cache subfolder "
                            + subfolder
                            + " was created");
                } else {
                    Log.e(LOG_TAG, "Unable to create subfolder "
                            + subfolder);
                }
            }
        } else {
            Log.e(LOG_TAG, "Unable to get cache folder");
        }
        return result;
    }

    private File CacheDirectoryForIcons(Context context) {
        mIconCacheFolder = getDiskCacheDir(context, CacheDirectoryForIcons);
        return mIconCacheFolder;
    }

    private File CacheDirectoryForImages(Context context) {
        mImageCacheFolder = getDiskCacheDir(context, CacheDirectoryForImages);
        return mImageCacheFolder;
    }

    public File getCachedIcon(String filename) {
        return getFileFromFolder(mIconCacheFolder, filename);
    }

    public File getCachedImage(String filename) {
        return getFileFromFolder(mImageCacheFolder, filename);
    }

    private static File getFileFromFolder(File folder, final String filename) {
        File[] results = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return filename.equals(pathname.getName());
            }
        });

        return results.length == 0 ? null : results[0];
    }

    private static File createNewFile(File folder, String filename) {
        File result = new File(folder, filename);
        try {
            result.createNewFile();
            Log.d(LOG_TAG, "New file was created: "
                    + filename);
        } catch (Throwable throwable) {
            Log.e(LOG_TAG, "Unable to create new file: "
                    + throwable.getMessage());
        }

        return result;
    }

    private File createNewIconFile(String filename) {
        return createNewFile(mIconCacheFolder, filename);
    }

    private File createNewImageFile(String filename) {
        return createNewFile(mImageCacheFolder, filename);
    }


    private static void saveBitmapToFile(File file, Bitmap bitmap) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (Throwable throwable) {
            Log.e(LOG_TAG, "Unable to save bitmap: "
                    + throwable.getMessage());
        }
    }

    public void saveIconToFile(String filename, Bitmap bitmap) {
        File newFile = getCachedIcon(filename);

        if (filename == null) {
            newFile = createNewIconFile(filename);
        }
        saveBitmapToFile(newFile, bitmap);
    }

    public void saveImageToFile(String filename, Bitmap bitmap) {

        File newFile = getCachedImage(filename);

        if (filename == null) {
            newFile = createNewImageFile(filename);
        }
        saveBitmapToFile(newFile, bitmap);
    }
}
