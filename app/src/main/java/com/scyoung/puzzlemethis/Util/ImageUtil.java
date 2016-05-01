package com.scyoung.puzzlemethis.Util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by scyoung on 4/18/16.
 */
public class ImageUtil {

    public static File writeBitmapToInternalStorage(Context context,
                                                    String outputFilename,
                                                    Bitmap bitmap) {
        FileOutputStream out;
        File buttonResourceFile = null;
        try {
            buttonResourceFile = new File(context.getFilesDir(), outputFilename);
            out = new FileOutputStream(buttonResourceFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return buttonResourceFile;
    }

    public static Bitmap getScaledBitmapFromStorage(Uri uri, int reqWidth, int reqHeight) {
        Bitmap ret = null;
        try {
            File f = new File(uri.getPath());
            if (f.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(f.getAbsolutePath(), options);
                Log.d("ImageUtil", "getScaledBitmap original image height is: " + options.outHeight);
                Log.d("ImageUtil", "getScaledBitmap original image width is: " + options.outWidth);

                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;
                ret = BitmapFactory.decodeFile(f.getAbsolutePath(), options);

                Log.d("ImageUtil", "getScaledBitmap scaled image height is: " + ret.getHeight());
                Log.d("ImageUtil", "getScaledBitmap scaled image width is: " + ret.getWidth());
                Log.d("ImageUtil", "image in bytes: " + ret.getByteCount());
                ret = rotateImageIfNecessary(ret, f);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            ret = null;
        }
        return ret;
    }

    public static Bitmap getScaledBitmapFromResources(int resourceId, int reqWidth, int reqHeight, Context context) {
        Bitmap ret;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        Log.d("ImageUtil", "getScaledBitmap original image height is: " + options.outHeight);
        Log.d("ImageUtil", "getScaledBitmap original image width is: " + options.outWidth);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        ret = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        Log.d("ImageUtil", "getScaledBitmap scaled image height is: " + ret.getHeight());
        Log.d("ImageUtil", "getScaledBitmap scaled image width is: " + ret.getWidth());
        Log.d("ImageUtil", "image in bytes: " + ret.getByteCount());

        return ret;
    }

    public static Bitmap getScaledBitmapFromGallery(Uri uri, int reqWidth, int reqHeight, Context context) {
        Bitmap ret;
        try {
            int orientation = getOrientation(context, uri);
            ContentResolver contentResolver = context.getContentResolver();
            InputStream imageStream = contentResolver.openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(imageStream, null, options);
            Log.d("ImageUtil", "getScaledBitmap original image height is: " + options.outHeight);
            Log.d("ImageUtil", "getScaledBitmap original image width is: " + options.outWidth);
            imageStream.close();

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            imageStream = contentResolver.openInputStream(uri);
            ret = BitmapFactory.decodeStream(imageStream, null, options);
            Log.d("ImageUtil", "getScaledBitmap scaled image height is: " + ret.getHeight());
            Log.d("ImageUtil", "getScaledBitmap scaled image width is: " + ret.getWidth());
            Log.d("ImageUtil", "image in bytes: " + ret.getByteCount());
            imageStream.close();
            ret = rotateImageIfNecessary(ret, orientation);
        }
        catch (Exception e) {
            e.printStackTrace();
            ret = null;
        }
        return ret;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap getBitmap(Uri imageUri) {
        Bitmap ret = null;
        try {
            File f = new File(imageUri.getPath());
            if (f.exists()) {
                Log.d("decodeUri", f.getAbsolutePath());
                ret = BitmapFactory.decodeFile(f.getAbsolutePath());
            }
            else {
                Log.d("CA:decodeUri", "File not found: " + imageUri.getPath());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static File saveBitmapToInternalStorage(String outputFilename, Bitmap in, Context context) {
        FileOutputStream out;
        File buttonResourceFile = null;
        outputFilename += DateUtil.getDateString();
        try {
            buttonResourceFile = new File(context.getFilesDir(), outputFilename);
            out = new FileOutputStream(buttonResourceFile);
            in.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return buttonResourceFile;
    }

    private static Bitmap rotateImageIfNecessary(Bitmap bitmap, int orientation) {
        Bitmap ret = bitmap;
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            ret = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
        }
        return ret;
    }

    private static Bitmap rotateImageIfNecessary(Bitmap bitmap, File imgFile) {
        Bitmap ret = bitmap;
        try {
            ExifInterface exif = new ExifInterface(imgFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            if (orientation > 0) {
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                ret = Bitmap.createBitmap(ret, 0, 0, ret.getWidth(), ret.getHeight(), matrix, true); // rotating bitmap
            }
        }
        catch (Exception e) {
            ret = bitmap; //original image
        }
        return ret;
    }

    public static int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);
        if (cursor.getCount() != 1) {
            return -1;
        }
        cursor.moveToFirst();
        return cursor.getInt(0);
    }
}
