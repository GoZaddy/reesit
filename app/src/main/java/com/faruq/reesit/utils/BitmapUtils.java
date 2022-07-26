package com.faruq.reesit.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {
    public static Bitmap scaleToFitWidth(Bitmap b, int width)
    {
        float factor = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
    }


    // Scale and maintain aspect ratio given a desired height
    // BitmapScaler.scaleToFitHeight(bitmap, 100);
    public static Bitmap scaleToFitHeight(Bitmap b, int height)
    {
        float factor = height / (float) b.getHeight();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
    }

    public static Bitmap getBitmapFromURI(Context context, Uri uri) throws IOException {
        InputStream inputStream = new BufferedInputStream(context.getContentResolver().openInputStream(uri));
        inputStream.mark(inputStream.available());

        ExifInterface exifInterface = new ExifInterface(inputStream);
        inputStream.reset();
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        return BitmapUtils.rotateBitmapWithExif(bitmap, exifInterface);
    }

    public static Bitmap rotateBitmapWithExif(@NonNull Bitmap sourceBitmap, @NonNull ExifInterface exifInterface){
        String orientString = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);

        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) sourceBitmap.getWidth() / 2, (float) sourceBitmap.getHeight() / 2);
        // Return result
        return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
    }

    public static Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif != null){
            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);

            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
            // Rotate Bitmap
            Matrix matrix = new Matrix();
            matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
            // Return result
            return Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        } else {
            return bm;
        }
    }
}
