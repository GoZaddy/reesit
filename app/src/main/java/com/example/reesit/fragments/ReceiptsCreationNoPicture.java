package com.example.reesit.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.reesit.R;
import com.example.reesit.databinding.FragmentReceiptsCreationNoPictureBinding;
import com.example.reesit.misc.UriAndSource;
import com.example.reesit.utils.ReesitCallback;
import com.example.reesit.utils.CameraUtils;
import com.example.reesit.utils.RuntimePermissions;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReceiptsCreationNoPicture#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiptsCreationNoPicture extends Fragment {

    private FragmentReceiptsCreationNoPictureBinding fragmentReceiptsCreationNoPictureBinding;

    private UriAndSource takenPictureURI;

    private ImageButton takePictureButton;
    private ImageButton choosePictureButton;

    private ActivityResultLauncher<Uri> takePhotoLauncher;
    private ActivityResultLauncher<String> selectPhotoLauncher;
    private ActivityResultLauncher<String> requestStoragePermissionLauncher;

    private static final String TAG = "ReceiptsCreationNoPicture";
    private static final String JPEG_MIME_TYPE = "image/jpeg";



    public ReceiptsCreationNoPicture() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment ReceiptsPictureNoPicture.
     */
    public static ReceiptsCreationNoPicture newInstance() {
        ReceiptsCreationNoPicture fragment = new ReceiptsCreationNoPicture();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentReceiptsCreationNoPictureBinding = FragmentReceiptsCreationNoPictureBinding.inflate(inflater, container, false);
        return fragmentReceiptsCreationNoPictureBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                    // switch to ReceiptCreationPictureTaken fragment
                    getParentFragmentManager().beginTransaction().replace(R.id.receiptsCreationFragmentContainer, ReceiptsPictureTaken.newInstance(takenPictureURI)).commit();
                }

            }
        });

        takePictureButton = fragmentReceiptsCreationNoPictureBinding.takePicture;
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takenPictureURI = CameraUtils.launchCamera(getContext(), takePhotoLauncher);
            }
        });

        selectPhotoLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null){
                    getParentFragmentManager().beginTransaction().replace(R.id.receiptsCreationFragmentContainer, ReceiptsPictureTaken.newInstance(UriAndSource.fromGallery(result))).commit();
                }
            }
        });

        requestStoragePermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        selectPhotoLauncher.launch("image/*");
                    } else {
                        Toast.makeText(getContext(), getString(R.string.media_access_permissions_message), Toast.LENGTH_SHORT).show();
                    }
                });
        choosePictureButton = fragmentReceiptsCreationNoPictureBinding.choosePicture;
        choosePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // request storage access permissions
                RuntimePermissions.requestStoragePermissions(ReceiptsCreationNoPicture.this, getContext(), requestStoragePermissionLauncher, new ReesitCallback() {
                    @Override
                    public void run() {
                        selectPhotoLauncher.launch("image/*");
                    }
                });
            }
        });
    }
}