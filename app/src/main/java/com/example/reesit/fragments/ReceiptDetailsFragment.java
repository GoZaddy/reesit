package com.example.reesit.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.FileUtils;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reesit.R;
import com.example.reesit.activities.ReceiptDetailsActivity;
import com.example.reesit.databinding.FragmentReceiptDetailsBinding;
import com.example.reesit.models.Receipt;
import com.example.reesit.models.Tag;
import com.example.reesit.models.User;
import com.example.reesit.services.ReceiptService;
import com.example.reesit.utils.BitmapUtils;
import com.example.reesit.utils.CurrencyUtils;
import com.example.reesit.utils.DateTimeUtils;
import com.example.reesit.utils.ReesitCallback;
import com.example.reesit.utils.RuntimePermissions;
import com.example.reesit.utils.Utils;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReceiptDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiptDetailsFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "ReceiptDetailsFragment";
    private static final String PUBLIC_IMAGES_DIRECTORY_TITLE = "Reesit";
    private static final int JPEG_IMAGE_QUALITY = 60;

    private Receipt receipt;


    private FragmentReceiptDetailsBinding binding;
    private Toolbar toolbar;
    private TextView merchantNameTV;
    private TextView amountTV;
    private TextView dateTimeTV;
    private TextView tagsTV;
    private TextView referenceNumTV;
    private Button updateReceipt;
    private MenuItem downloadMenuItem;

    private ActivityResultLauncher<String> requestStoragePermissionLauncher;

    public static final String RECEIPT_UPDATE_INFORMATION_RESULT_KEY = "RECEIPT_UPDATE_INFORMATION_RESULT_KEY";
    public static final String FILE_PROVIDER_AUTHORITY = "com.example.reesit.fileprovider";


    // defines how the receipt object was changed
    @Parcel
    public static class ReceiptUpdateInformation{
        public enum UpdateType{
            UPDATE,
            DELETE
        }
        private UpdateType updateType;
        private int recyclerViewPosition;
        private Receipt newReceipt;

        public ReceiptUpdateInformation(){}

        public ReceiptUpdateInformation(UpdateType updateType, int recyclerViewPosition, Receipt newReceipt) {
            this.updateType = updateType;
            this.recyclerViewPosition = recyclerViewPosition;
            this.newReceipt = newReceipt;
        }

        public UpdateType getUpdateType() {
            return updateType;
        }

        public int getRecyclerViewPosition() {
            return recyclerViewPosition;
        }

        public Receipt getNewReceipt() {
            return newReceipt;
        }

        public void setRecyclerViewPosition(int recyclerViewPosition) {
            this.recyclerViewPosition = recyclerViewPosition;
        }
    }

    // callback interface for downloading images to external-files-dir
    private interface DownloadImageToExternalFilesDirCallback{
        void done(Uri localImageUri, Exception e);
    }

    private interface DownloadReceiptImageCallback{
        void done(Exception e);
    }

    public ReceiptDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param receipt Receipt to show.
     * @return A new instance of fragment ReceiptDetailsFragment.
     */
    public static ReceiptDetailsFragment newInstance(Receipt receipt) {
        ReceiptDetailsFragment fragment = new ReceiptDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, Parcels.wrap(receipt));
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            receipt = (Receipt) Parcels.unwrap(getArguments().getParcelable(ARG_PARAM1));
        }
        setHasOptionsMenu(true);
        if (requireActivity() instanceof ReceiptDetailsActivity){
            ((ReceiptDetailsActivity) requireActivity()).enableBackButton();
            ((ReceiptDetailsActivity) requireActivity()).getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finishActivityAfterUpdate();
                }
            });
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentReceiptDetailsBinding.inflate(inflater, container, false);
        return  binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_receipt_details, menu);
        downloadMenuItem = menu.findItem(R.id.download_receipt_menu_item);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finishActivityAfterUpdate();
            return true;
        }
        else if (itemId == R.id.delete_receipt_menu_item) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
            dialogBuilder.setTitle(getString(R.string.delete_receipt_dialog_title));
            dialogBuilder.setMessage(getString(R.string.delete_receipt_dialog_message));
            dialogBuilder.setNegativeButton(R.string.delete_receipt_dialog_negative_button_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialogBuilder.setPositiveButton(R.string.delete_receipt_dialog_positive_button_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteReceipt();
                }
            });
            dialogBuilder.show();
            return true;
        }
        else if (itemId == R.id.share_receipt_menu_item) {
            item.setEnabled(false);

            downloadImageToExternalFilesDir(receipt.getReceiptImage(), getReceiptImageFilename(receipt), new DownloadImageToExternalFilesDirCallback() {
                @Override
                public void done(Uri localImageUri, Exception e) {
                    item.setEnabled(true);
                    if (e == null) {
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        shareIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, localImageUri);

                        shareIntent.setType("image/*");
                        startActivity(Intent.createChooser(shareIntent, null));
                    } else {
                        Toast.makeText(requireContext(), R.string.share_receipt_load_receipt_image_error, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error downloading receipt image to external-files-dir", e);
                    }
                }
            });
            return true;
        }
        else if (itemId == R.id.download_receipt_menu_item) {
            item.setEnabled(false);
            Snackbar.make(binding.getRoot(), R.string.download_receipt_loading_message, BaseTransientBottomBar.LENGTH_INDEFINITE).show();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    RuntimePermissions.requestWriteStoragePermissions(ReceiptDetailsFragment.this, requireContext(), requestStoragePermissionLauncher, new ReesitCallback() {
                        @Override
                        public void run() {
                            downloadReceiptImage(new DownloadReceiptImageCallback() {
                                @Override
                                public void done(Exception e) {
                                    requireActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            item.setEnabled(true);
                                            if (e == null){
                                                Snackbar.make(binding.getRoot(), R.string.download_receipt_image_successful, BaseTransientBottomBar.LENGTH_SHORT).show();
                                            } else {
                                                Snackbar.make(binding.getRoot(), R.string.download_receipt_image_error, BaseTransientBottomBar.LENGTH_SHORT).show();
                                                Log.e(TAG, "Error downloading receipt image", e);
                                            }
                                            executorService.shutdownNow();
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });


            return true;
        }
        return false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        merchantNameTV = binding.merchantTextView;
        amountTV = binding.amountTextView;
        referenceNumTV = binding.refNumberTextView;
        tagsTV = binding.tagsTextView;
        dateTimeTV = binding.dateTimeTextView;
        updateReceipt = binding.updateReceiptInfoBtn;

        // initialize launchers
        requestStoragePermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        downloadReceiptImage(new DownloadReceiptImageCallback() {
                            @Override
                            public void done(Exception e) {
                                downloadMenuItem.setEnabled(true);
                                if (e == null){
                                    Snackbar.make(binding.getRoot(), R.string.download_receipt_image_successful, BaseTransientBottomBar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(binding.getRoot(), R.string.download_receipt_image_error, BaseTransientBottomBar.LENGTH_SHORT).show();
                                    Log.e(TAG, "Error downloading receipt image", e);
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), getString(R.string.media_access_permissions_message), Toast.LENGTH_SHORT).show();
                    }
                });


        // bind data to view
        merchantNameTV.setText(receipt.getMerchant().getName());
        amountTV.setText(getString(R.string.dollar_sign_format, CurrencyUtils.integerToCurrency(receipt.getAmount())));
        if (receipt.getReferenceNumber() != null && receipt.getReferenceNumber().trim().length() != 0){
            referenceNumTV.setText(receipt.getReferenceNumber());
        }else {
            referenceNumTV.setText(getString(R.string.not_applicable));
        }

        if (receipt.getTags() != null && receipt.getTags().size() > 0){
            ArrayList<String> tagNames = new ArrayList<>();
            for(Tag tag: receipt.getTags()){
                tagNames.add(tag.getName());
            }
            tagsTV.setText(String.join(", ", tagNames));
        } else {
            tagsTV.setText(getString(R.string.not_applicable));
        }

        dateTimeTV.setText(getString(R.string.receipt_details_datetime_format, DateTimeUtils.getDateLongDayLongMonth(receipt.getDateTimestamp()), DateTimeUtils.getTimeString(receipt.getDateTimestamp())));


        // set listeners
        updateReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction().replace(R.id.receipt_details_fragment_container, ReceiptCreationFinalFragment.newInstance(receipt)).commit();
            }
        });
    }

    // returns the current value of the receipt object to the calling activity
    private void finishActivityAfterUpdate(){
        Intent intent = new Intent();
        ReceiptUpdateInformation receiptUpdateInformation = new ReceiptUpdateInformation(ReceiptUpdateInformation.UpdateType.UPDATE, -1, receipt);
        if (requireActivity() instanceof ReceiptDetailsActivity){
            receiptUpdateInformation.setRecyclerViewPosition(((ReceiptDetailsActivity) requireActivity()).getReceiptPosition());
        }

        intent.putExtra(RECEIPT_UPDATE_INFORMATION_RESULT_KEY, Parcels.wrap(receiptUpdateInformation));
        requireActivity().setResult(Activity.RESULT_OK, intent);
        requireActivity().finish();
    }

    // informs the calling activity that the receipt has been deleted
    private void finishActivityAfterDelete(){
        Intent intent = new Intent();
        ReceiptUpdateInformation receiptUpdateInformation = new ReceiptUpdateInformation(ReceiptUpdateInformation.UpdateType.DELETE, -1, null);
        if (requireActivity() instanceof ReceiptDetailsActivity){
            receiptUpdateInformation.setRecyclerViewPosition(((ReceiptDetailsActivity) requireActivity()).getReceiptPosition());
        }
        intent.putExtra(RECEIPT_UPDATE_INFORMATION_RESULT_KEY, Parcels.wrap(receiptUpdateInformation));
        requireActivity().setResult(Activity.RESULT_OK, intent);
        requireActivity().finish();
    }

    private void deleteReceipt(){
        ReceiptService.deleteReceipt(receipt.getId(), Objects.requireNonNull(User.getCurrentUser()), new ReceiptService.DeleteReceiptCallback() {
            @Override
            public void done(Exception e) {
                if (e == null){
                    finishActivityAfterDelete();
                } else {
                    Log.e(TAG, "Error encountered while deleting receipt: "+receipt.getId(), e);
                    Snackbar.make(binding.getRoot(), R.string.delete_receipt_failure_message, BaseTransientBottomBar.LENGTH_SHORT)
                            .setAction(R.string.delete_receipt_failure_retry_action_text, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteReceipt();
                                }
                            }).show();
                }
            }
        });
    }

    private String getReceiptImageFilename(Receipt receipt){
        String[] parts = receipt.getReceiptImage().split("\\.");
        return "receipt_image_" + receipt.getId() + "." + parts[parts.length-1];
    }

    private String getReceiptImageFilenameRandomized(Receipt receipt){
        String[] parts = receipt.getReceiptImage().split("\\.");
        return "receipt_image_" + receipt.getId() + Long.toString(System.currentTimeMillis() / 1000L) + "." + parts[parts.length-1];
    }



    private String getPublicImagesDirectoryPath(){
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+PUBLIC_IMAGES_DIRECTORY_TITLE;
    }


    private void downloadImageToExternalFilesDir(String imageUrl, String filename, DownloadImageToExternalFilesDirCallback callback){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Uri> future = executorService.submit(new Callable<Uri>() {
            @Override
            public Uri call() throws Exception {
                File file = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
                if (!file.exists()) {
                    URL url = new URL(imageUrl);

                    // write url content to temporary file
                    File tempFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),  "receipt_image_temp." + Utils.getFileExtensionFromURL(imageUrl));
                    FileOutputStream tempOut = new FileOutputStream(tempFile);
                    InputStream urlInputStream = url.openConnection().getInputStream();
                    FileUtils.copy(urlInputStream, tempOut);
                    tempOut.close();
                    urlInputStream.close();

                    Bitmap origBitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());

                    // correct bitmap orientation using exif data from tempFile
                    ExifInterface exif = new ExifInterface(tempFile);
                    Bitmap finalBitmap = BitmapUtils.rotateBitmapWithExif(origBitmap, exif);

                    // compress bitmap
                    FileOutputStream out = new FileOutputStream(file);
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_IMAGE_QUALITY, out);
                    out.close();

                    if (!tempFile.delete()){
                        Log.e(TAG, "could not delete tempFile");
                    }
                }
                return FileProvider.getUriForFile(requireContext(), FILE_PROVIDER_AUTHORITY, file);
            }
        });

        try{
            callback.done(future.get(), null);
        } catch (Exception e) {
            callback.done(null, e);
        }
    }

    private void downloadReceiptImage(DownloadReceiptImageCallback callback){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Exception> future = executorService.submit(new Callable<Exception>() {
            @Override
            public Exception call() throws Exception {
                File privateFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), getReceiptImageFilename(receipt));
                if (privateFile.exists()){
                    File publicFile = new File(getPublicImagesDirectoryPath(), getReceiptImageFilenameRandomized(receipt));
                    FileInputStream inputStream = new FileInputStream(privateFile);
                    FileOutputStream outputStream;


                    if (!publicFile.exists()){
                        if (!Objects.requireNonNull(publicFile.getParentFile()).exists()){
                            if (!publicFile.getParentFile().mkdirs()){
                                return new Exception("could not create directory to hold application's public images");
                            }
                        }
                        if (!publicFile.createNewFile()){
                            return new Exception("could not create file");
                        }
                    }


                    outputStream = new FileOutputStream(publicFile);

                    FileUtils.copy(inputStream, outputStream);

                    inputStream.close();

                    outputStream.flush();
                    outputStream.getFD().sync();
                    outputStream.close();

                    MediaScannerConnection.scanFile(requireContext(), new String[]{publicFile.getAbsolutePath()}, new String[]{"image/*"}, null);
                } else {
                    URL url = new URL(receipt.getReceiptImage());

                    // write url content to temporary file
                    File tempFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),  "receipt_image_temp." + Utils.getFileExtensionFromURL(receipt.getReceiptImage()));
                    FileOutputStream tempOut = new FileOutputStream(tempFile);
                    InputStream urlInputStream = url.openConnection().getInputStream();
                    FileUtils.copy(urlInputStream, tempOut);
                    tempOut.close();
                    urlInputStream.close();

                    Bitmap origBitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());

                    // correct bitmap orientation using exif data from tempFile
                    ExifInterface exif = new ExifInterface(tempFile);
                    Bitmap finalBitmap = BitmapUtils.rotateBitmapWithExif(origBitmap, exif);

                    // compress bitmap
                    File publicFile = new File(getPublicImagesDirectoryPath(), getReceiptImageFilenameRandomized(receipt));
                    FileOutputStream outputStream;
                    if (!publicFile.exists()){
                        // create directory if it doesn't exist
                        if (!Objects.requireNonNull(publicFile.getParentFile()).exists()){
                            if (!publicFile.getParentFile().mkdirs()){
                                return new Exception("could not create directory to hold application's public images");
                            }
                        }
                        // create file
                        if (!publicFile.createNewFile()){
                            return new Exception("could not create file");
                        }
                    }
                    outputStream = new FileOutputStream(publicFile);
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_IMAGE_QUALITY, outputStream);
                    outputStream.flush();
                    outputStream.getFD().sync();
                    outputStream.close();

                    MediaScannerConnection.scanFile(requireContext(), new String[]{publicFile.getAbsolutePath()}, new String[]{"image/*"}, null);

                    if (!tempFile.delete()){
                        Log.e(TAG, "could not delete tempFile");
                    }
                }
                return null;
            }
        });

        Exception exception;
        try{
            exception = future.get();
        } catch (Exception e) {
            exception = e;
        }

        callback.done(exception);
    }

}