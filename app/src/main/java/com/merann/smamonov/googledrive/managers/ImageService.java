package com.merann.smamonov.googledrive.managers;

/**
 * Created by sergeym on 08.12.2015.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

interface ImageLoaderListener {
    void onLoadComplete(Bitmap bitmap);
}

public class ImageService {
    private final static String LOG_TAG = "RemoteStorageManager";

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
/*
    public static void loadIconImage(final InputStream inputStream,
                                     final ImageLoaderListener imageLoaderListener) {
        Log.d(LOG_TAG, "loadIconImage");
        AsyncTask task = new AsyncTask<Void, Void, Void>() {
            Bitmap mResult;

            @Override
            protected Void doInBackground(Void... params) {
                //mResult = loadIcon(inputStream);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                imageLoaderListener.onLoadComplete(mResult);
            }
        }.execute();
    }

    public static void loadIconImageSync(final InputStream inputStream,
                                         final ImageLoaderListener imageLoaderListener) {
        Log.d(LOG_TAG, "loadIconImage");
        //Bitmap result = loadIcon(inputStream);
        //imageLoaderListener.onLoadComplete(result);
    }
*/

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

    public static BitmapFactory.Options getIconOptions(InputStream inputStream) {
        BitmapFactory.Options result = new BitmapFactory.Options();
        result.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(inputStream, null, result);
        result.inSampleSize = calculateInSampleSize(result, 60, 60);
        result.inJustDecodeBounds = false;

        return result;
    }


    public static Bitmap loadIcon(InputStream inputStream, BitmapFactory.Options bitmapOptions) {
        Log.d(LOG_TAG, "loadIcon");
        Bitmap result = null;

        try {
            result = BitmapFactory.decodeStream(inputStream, null, bitmapOptions);
        } catch (Throwable throwable) {
            Log.d(LOG_TAG, "loadIcon: unable to load file icon");
        }

        Log.d(LOG_TAG, "loadIcon: image icon was loaded, size:" + result.getByteCount());

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

    public static Bitmap loadIcon(File file) {
        Log.d(LOG_TAG, "loadIcon");
        Bitmap result = null;

        try {
            InputStream inputStream = new FileInputStream(file);
            BitmapFactory.Options bitmapOptions = getIconOptions(inputStream);
            inputStream.close();
            inputStream = new FileInputStream(file);
            result = BitmapFactory.decodeStream(inputStream, null, bitmapOptions);
            inputStream.close();
        } catch (Throwable throwable) {
            Log.d(LOG_TAG, "loadIcon: unable to load file icon");
        }

        Log.d(LOG_TAG, "loadIcon: image icon was loaded, size:" + result.getByteCount());

        return result;
    }

}

