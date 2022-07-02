package com.example.reesit.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.reesit.misc.UriAndSource;

import java.util.Objects;

public class FileUtils {
    public static String getImagePathFromURI(Context context, UriAndSource contentUri, String TAG) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            String selection;
            Uri uriToQuery;
            if (Objects.equals(contentUri.getSource(), UriAndSource.GALLERY)){
                selection = MediaStore.Images.Media._ID + " = " + contentUri.getUri().getLastPathSegment().split(":")[1];
                uriToQuery = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            } else {
                selection = null;
                uriToQuery = contentUri.getUri();
            }

            cursor = context.getContentResolver().query(uriToQuery,  proj, selection, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e(TAG, "getRealPathFromURI Exception : " + e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
