package com.merann.smamonov.googledrive.model;

import android.graphics.Bitmap;

/**
 * Created by samam_000 on 10.12.2015.
 */
public class Image {
    String mFileName;
    Bitmap mBitmap;


    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public Image(String fileName) {
        this.mFileName = fileName;
    }
}
