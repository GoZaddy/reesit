package com.faruq.reesit.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;

import com.faruq.reesit.misc.UriAndSource;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraUtils {
    private static final String JPEG_MIME_TYPE = "image/jpeg";
    private static final String TAG = "CameraUtils";

    public static UriAndSource launchCamera(Context context, ActivityResultLauncher<Uri> takePhotoLauncher){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri imageCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            imageCollection = MediaStore.Images.Media
                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        if (context != null){
            ContentResolver resolver = context.getContentResolver();
            ContentValues newImageDetails = new ContentValues();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            newImageDetails.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
            newImageDetails.put(MediaStore.Images.Media.MIME_TYPE, JPEG_MIME_TYPE);



            UriAndSource takenPictureURI = UriAndSource.fromCamera(resolver.insert(imageCollection, newImageDetails));

            intent.putExtra(MediaStore.EXTRA_OUTPUT, takenPictureURI.getUri());

            if (intent.resolveActivity(context.getPackageManager()) != null){
                takePhotoLauncher.launch(takenPictureURI.getUri());
            }
            return takenPictureURI;
        } else {
            Log.e(TAG, "getContext() returned null while trying to get content resolver");
            return null;
        }
    }
}
