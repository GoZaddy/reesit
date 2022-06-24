package com.example.reesit.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.reesit.R;
import com.example.reesit.databinding.FragmentReceiptsPictureTakenBinding;
import com.example.reesit.misc.UriAndSource;
import com.example.reesit.utils.BitmapUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReceiptsPictureTaken#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiptsPictureTaken extends Fragment {


    private static final String ARG_PARAM1 = "takenPictureURI";
    private static final String TAG = "ReceiptsPictureTaken";
    private ImageView receiptImageView;
    private Button processReceiptButton;
    private ImageButton retakePictureButton;
    private ImageButton rechoosePictureButton;

    private FragmentReceiptsPictureTakenBinding fragmentReceiptsPictureTakenBinding;

    private ActivityResultLauncher<Uri> retakePhotoLauncher;
    private ActivityResultLauncher<String> reselectPhotoLauncher;
    private ActivityResultLauncher<String> requestStoragePermissionLauncher;

    private UriAndSource takenPictureURI;

    private static final String JPEG_MIME_TYPE = "image/jpeg";



    public ReceiptsPictureTaken() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param uri Uri of receipt image.
     * @return A new instance of fragment ReceiptsPictureTaken.
     */

    public static ReceiptsPictureTaken newInstance(UriAndSource uri) {
        ReceiptsPictureTaken fragment = new ReceiptsPictureTaken();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, Parcels.wrap(uri));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            takenPictureURI = (UriAndSource) Parcels.unwrap(getArguments().getParcelable(ARG_PARAM1));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentReceiptsPictureTakenBinding = FragmentReceiptsPictureTakenBinding.inflate(inflater, container, false);
        return fragmentReceiptsPictureTakenBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        receiptImageView = fragmentReceiptsPictureTakenBinding.receiptImageView;
        renderImageOnPreview(getContext(), takenPictureURI);

        processReceiptButton = fragmentReceiptsPictureTakenBinding.processReceiptButton;
        processReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                    InputImage inputImage = InputImage.fromFilePath(getContext(), takenPictureURI.getUri());
                    Task<Text> result =
                            textRecognizer.process(inputImage)
                                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                                        @Override
                                        public void onSuccess(Text visionText) {
                                            // parse visionText
                                        }
                                    })
                                    .addOnFailureListener(
                                            new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "error occurred while performing text recognition: "+e.getLocalizedMessage(), e);
                                                    Toast.makeText(getContext(), "Error processing receipt. Please retake the picture and try again", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                } catch(IOException exception){
                    Log.e(TAG, "Error creating InputImage from URI", exception);
                }

            }
        });

        retakePhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                   // render preview
                    renderImageOnPreview(getContext(), takenPictureURI);
                }

            }
        });


        reselectPhotoLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null){
                    takenPictureURI = UriAndSource.fromGallery(result);
                    renderImageOnPreview(getContext(), takenPictureURI);
                }
            }
        });
        requestStoragePermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        reselectPhotoLauncher.launch("image/*");
                    } else {
                        Toast.makeText(getContext(), getString(R.string.media_access_permissions_message), Toast.LENGTH_SHORT).show();
                    }
                });

        retakePictureButton = fragmentReceiptsPictureTakenBinding.retakePictureButton;
        retakePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete previous photo if it was taken with the camera
                if (Objects.equals(takenPictureURI.getSource(), UriAndSource.CAMERA)){
                    if (getContext() != null){
                        getContext().getContentResolver().delete(takenPictureURI.getUri(), null, null);
                    } else {
                        Log.e(TAG, "getContext() returned null", null);
                    }
                }

                // launch camera
                launchCamera();
            }
        });


        rechoosePictureButton = fragmentReceiptsPictureTakenBinding.rechoosePictureButton;
        rechoosePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // request storage access permissions
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    reselectPhotoLauncher.launch("image/*");
                }
                else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getString(R.string.media_access_permissions_message)).setTitle(getString(R.string.media_access_permissions_dialog_title));
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
        });

    }

    private String getImagePathFromURI(Context context, UriAndSource contentUri) {
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

    private void launchCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri imageCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            imageCollection = MediaStore.Images.Media
                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        if (getContext() != null){
            ContentResolver resolver = getContext().getContentResolver();
            ContentValues newImageDetails = new ContentValues();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            newImageDetails.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
            newImageDetails.put(MediaStore.Images.Media.MIME_TYPE, JPEG_MIME_TYPE);



            takenPictureURI = UriAndSource.fromCamera(resolver.insert(imageCollection, newImageDetails));

            intent.putExtra(MediaStore.EXTRA_OUTPUT, takenPictureURI.getUri());

            if (intent.resolveActivity(getContext().getPackageManager()) != null){
                retakePhotoLauncher.launch(takenPictureURI.getUri());
            }
        } else {
            Log.e(TAG, "getContext() returned null while trying to get content resolver");
        }


    }


    private void renderImageOnPreview(Context context, UriAndSource photoURI){
        File file = new File(getImagePathFromURI(context, photoURI));
        Bitmap bitmap = BitmapUtils.rotateBitmapOrientation(file.getAbsolutePath());
        receiptImageView.setImageBitmap(BitmapUtils.scaleToFitHeight(bitmap, 800));
    }
}