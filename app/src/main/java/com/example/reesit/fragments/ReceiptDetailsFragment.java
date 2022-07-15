package com.example.reesit.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.reesit.R;
import com.example.reesit.activities.ReceiptDetailsActivity;
import com.example.reesit.databinding.FragmentReceiptDetailsBinding;
import com.example.reesit.models.Receipt;
import com.example.reesit.models.Tag;
import com.example.reesit.models.User;
import com.example.reesit.services.ReceiptService;
import com.example.reesit.utils.CurrencyUtils;
import com.example.reesit.utils.DateTimeUtils;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReceiptDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiptDetailsFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "ReceiptDetailsFragment";

    private Receipt receipt;


    private FragmentReceiptDetailsBinding binding;
    private Toolbar toolbar;
    private TextView merchantNameTV;
    private TextView amountTV;
    private TextView dateTimeTV;
    private TextView tagsTV;
    private TextView referenceNumTV;
    private Button updateReceipt;

    public static final String RECEIPT_UPDATE_INFORMATION_RESULT_KEY = "RECEIPT_UPDATE_INFORMATION_RESULT_KEY";


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentReceiptDetailsBinding.inflate(inflater, container, false);
        return  binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_receipt_details, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finishActivityAfterUpdate();
                return true;
            case R.id.delete_receipt_menu_item:
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
            case R.id.share_receipt_menu_item:
                return true;
            case R.id.download_receipt_menu_item:
                return true;
            default:
                return false;
        }
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
}