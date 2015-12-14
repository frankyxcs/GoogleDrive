package com.merann.smamonov.googledrive.managers;

import android.content.Context;

import java.io.File;

/**
 * Created by samam_000 on 15.12.2015.
 */
public class DiskCacheHelper {

    private final Object mDiskCacheLock = new Object();

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
    // but if not mounted, falls back on internal storage.
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cahePath = context.getExternalCacheDir().getPath();
        return new File(cahePath + File.separator + uniqueName);
    }


}
