package com.example.reesit.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.reesit.R;
import com.example.reesit.databinding.FragmentReceiptsPictureTakenBinding;
import com.example.reesit.misc.ReceiptWithImage;
import com.example.reesit.misc.UriAndSource;
import com.example.reesit.models.Receipt;
import com.example.reesit.utils.BitmapUtils;
import com.example.reesit.utils.FileUtils;
import com.example.reesit.utils.CameraUtils;
import com.example.reesit.utils.ReceiptTextParser;
import com.example.reesit.utils.ReesitCallback;
import com.example.reesit.utils.RuntimePermissions;
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
    private ProgressBar progressBar;

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

        progressBar = fragmentReceiptsPictureTakenBinding.receiptsPictureTakenProgressBar;

        receiptImageView = fragmentReceiptsPictureTakenBinding.receiptImageView;
        renderImageOnPreview(getContext(), takenPictureURI);

        processReceiptButton = fragmentReceiptsPictureTakenBinding.processReceiptButton;
        processReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPageStateLoading();
                try{

                    TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                    InputImage inputImage = InputImage.fromFilePath(getContext(), takenPictureURI.getUri());
                    Task<Text> result =
                            textRecognizer.process(inputImage)
                                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                                        @Override
                                        public void onSuccess(Text visionText) {
                                            // parse visionText
                                            ReceiptTextParser.parseReceiptText(visionText, new ReceiptTextParser.ReceiptParseCallback() {
                                                @Override
                                                public void onSuccess(Receipt receipt) {
                                                    if (receipt != null){
                                                        setPageStateNotLoading();
                                                        getParentFragmentManager().beginTransaction().
                                                                replace(R.id.receiptsCreationFragmentContainer, ReceiptCreationFinalFragment.newInstance(new ReceiptWithImage(receipt, takenPictureURI))).commit();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(ReceiptTextParser.ReceiptParsingException e) {
                                                    if (e != null){
                                                        Toast.makeText(getContext(), getString(R.string.receipt_picture_taken_process_receipt_error_message), Toast.LENGTH_SHORT).show();
                                                        Log.e(TAG, "Error parsing receipt!", e);
                                                        setPageStateNotLoading();
                                                    }
                                                }
                                            });
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
                takenPictureURI = CameraUtils.launchCamera(getContext(), retakePhotoLauncher);

            }
        });


        rechoosePictureButton = fragmentReceiptsPictureTakenBinding.rechoosePictureButton;
        rechoosePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // request storage access permissions
                RuntimePermissions.requestStoragePermissions(ReceiptsPictureTaken.this, getContext(), requestStoragePermissionLauncher, new ReesitCallback() {
                    @Override
                    public void run() {
                        reselectPhotoLauncher.launch("image/*");
                    }
                });
            }
        });

    }



    private void renderImageOnPreview(Context context, UriAndSource photoURI){
        File file = new File(FileUtils.getImagePathFromURI(context, photoURI, TAG));
        Bitmap bitmap = BitmapUtils.rotateBitmapOrientation(file.getAbsolutePath());
        receiptImageView.setImageBitmap(BitmapUtils.scaleToFitHeight(bitmap, 800));
    }

    public void setPageStateLoading(){
        progressBar.setVisibility(View.VISIBLE);
        rechoosePictureButton.setEnabled(false);
        retakePictureButton.setEnabled(false);
        processReceiptButton.setEnabled(false);
    }


    public void setPageStateNotLoading(){
        progressBar.setVisibility(View.INVISIBLE);
        rechoosePictureButton.setEnabled(true);
        retakePictureButton.setEnabled(true);
        processReceiptButton.setEnabled(true);
    }
}