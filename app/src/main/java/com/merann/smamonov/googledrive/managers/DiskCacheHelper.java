package com.merann.smamonov.googledrive.managers;

import android.content.Context;
import android.util.Log;

import org.apache.commons.io.filefilter.DirectoryFileFilter;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by samam_000 on 15.12.2015.
 */
public class DiskCacheHelper {

    private final static String LOG_TAG = "DiskCacheHelper";
    private final static String CacheDirectoryForIcons = "IconsCache";
    private final static String CacheDirectoryForImages = "ImagesCache";

    private static File getDiskCacheDir(Context context, String subfolder) {

        final File cacheDirectory = context.getCacheDir();
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

            if(result == null)
            {
                result = new File(cacheDirectory.getPath() + "/" + cacheDirectory);
                if (result.mkdir())
                {
                    Log.d(LOG_TAG, "Cache subfolder "
                            + cacheDirectory
                            + " was created");
                }
                else
                {
                    Log.e(LOG_TAG, "Unable to create subfolder "
                    + cacheDirectory);
                }
            }
        } else {
            Log.e(LOG_TAG, "Unable to get cache folder");
        }

        return result;
    }

    private static File CacheDirectoryForIcons(Context context) {
        return getDiskCacheDir(context, CacheDirectoryForIcons);
    }

    private static File CacheDirectoryForImages(Context context) {
        return getDiskCacheDir(context, CacheDirectoryForImages);
    }



}
