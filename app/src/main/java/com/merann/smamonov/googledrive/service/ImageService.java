package com.merann.smamonov.googledrive.service;

/**
 * Created by sergeym on 08.12.2015.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;

interface ImageLoaderListener {
    void onLoadComplete(Bitmap bitmap);
}

public class ImageService {
    private final static String LOG_TAG = "DriveServiceData";

    public static void loadFullImage(final InputStream inputStream,
                                     final ImageLoaderListener imageLoaderListener) {
        Log.d(LOG_TAG, "loadFullImage");

        AsyncTask task = new AsyncTask<Void, Void, Void>() {
            Bitmap mResult;

            @Override
            protected Void doInBackground(Void... params) {
                mResult = loadImage(inputStream);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                imageLoaderListener.onLoadComplete(mResult);
            }
        }.execute();
    }

    public static void loadIconImage(final InputStream inputStream,
                                      final ImageLoaderListener imageLoaderListener) {
        Log.d(LOG_TAG, "loadIconImage");
        AsyncTask task = new AsyncTask<Void, Void, Void>() {
            Bitmap mResult;

            @Override
            protected Void doInBackground(Void... params) {
                mResult = loadIcon(inputStream);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                imageLoaderListener.onLoadComplete(mResult);
            }
        }.execute();
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int width,
                                             int height) {
        Log.d(LOG_TAG, "calculateInSampleSize");
        final int original_height = options.outHeight;
        final int original_width = options.outWidth;

        int inSampleSize = 1;

        if (original_height > height || original_width > width) {

            final int halfHeight = original_height / 2;
            final int halfWidth = original_width / 2;

            while ((halfHeight / inSampleSize) > height
                    && (halfWidth / inSampleSize) > width) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static Bitmap loadIcon(InputStream inputStream) {
        Log.d(LOG_TAG, "loadIcon");
        Bitmap result = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(inputStream, null, options);
        options.inSampleSize = calculateInSampleSize(options, 60, 60);
        options.inJustDecodeBounds = false;

        try {
            result = BitmapFactory.decodeStream(inputStream, null, options);
            Log.d(LOG_TAG, "loadIcon: image icon was loaded, size:" + result.getByteCount());

        } catch (Throwable throwable) {
            Log.d(LOG_TAG, "loadIcon: unable to load file icon");
        }
        return result;
    }


    private static Bitmap loadImage(InputStream inputStream) {
        Bitmap result = null;
        while (result == null) {
            try {
                result = BitmapFactory.decodeStream(inputStream);
                Log.d(LOG_TAG, "loadImage: image was loaded, size:" + result.getByteCount());
                break;
            } catch (OutOfMemoryError error) {
                Log.d(LOG_TAG, "Unable to load image");
            }
        }
        return result;
    }

}

