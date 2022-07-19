package com.example.reesit.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.reesit.R;

public class RuntimePermissions {
    public static void requestReadStoragePermissions(Fragment fragment, Context context, ActivityResultLauncher<String> requestStoragePermissionLauncher, ReesitCallback action){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            action.run();
        }
        else if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
            builder.setMessage(fragment.getString(R.string.media_access_permissions_message)).setTitle(fragment.getString(R.string.media_access_permissions_dialog_title));
            builder.setNegativeButton(R.string.media_access_permissions_dialog_cancel_text, null);
            builder.setPositiveButton(R.string.media_access_permissions_dialog_grant_permission_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    public static void requestWriteStoragePermissions(Fragment fragment, Context context, ActivityResultLauncher<String> requestStoragePermissionLauncher, ReesitCallback action){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            action.run();
        }
        else if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
            builder.setMessage(fragment.getString(R.string.write_store_permissions_message)).setTitle(fragment.getString(R.string.write_storage_permissions_dialog_title));
            builder.setNegativeButton(R.string.write_storage_permissions_dialog_cancel_text, null);
            builder.setPositiveButton(R.string.write_storage_permissions_dialog_grant_permission_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }
}
