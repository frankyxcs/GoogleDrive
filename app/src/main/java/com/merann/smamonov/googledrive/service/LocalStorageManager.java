package com.merann.smamonov.googledrive.service;

import com.merann.smamonov.googledrive.model.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by samam_000 on 10.12.2015.
 */
public class LocalStorageManager {
    private static LocalStorageManager ourInstance = new LocalStorageManager();

    public static LocalStorageManager getInstance() {
        return ourInstance;
    }

    private LocalStorageManager() {
    }

    public List<Image> getImagesList() {
        //TODO: get the folder by global name
        List<Image> result = new ArrayList<>();
        File picture_folder = new File("/mnt/extSdCard/DCIM/Camera");
        if (picture_folder != null) {
            for (File file : picture_folder.listFiles())
            {
                result.add(new Image(file.getName()));
            }
        }
        return result;
    }

    public File getFileByFileName(String fileName)
    {
        File result = null;
        File picture_folder = new File("/mnt/extSdCard/DCIM/Camera");
        if (picture_folder != null) {
            for (File file : picture_folder.listFiles())
            {
                if (file.getName().equals(fileName))
                {
                    result = file;
                    break;
                }
            }
        }
        return result;
    }
}
