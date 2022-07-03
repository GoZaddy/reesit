package com.example.reesit.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.reesit.R;
import com.example.reesit.activities.MainActivity;
import com.example.reesit.activities.ReceiptCreationActivity;
import com.example.reesit.databinding.FragmentReceiptCreationFinalBinding;
import com.example.reesit.misc.Debouncer;
import com.example.reesit.misc.ReceiptWithImage;
import com.example.reesit.models.Merchant;
import com.example.reesit.models.Receipt;
import com.example.reesit.services.MerchantService;
import com.example.reesit.services.ReceiptService;
import com.example.reesit.utils.AddReceiptCallback;
import com.example.reesit.utils.DateTimeUtils;
import com.example.reesit.utils.FileUtils;
import com.example.reesit.utils.GetMerchantsCallback;
import com.example.reesit.utils.ReesitCallback;
import com.example.reesit.utils.Utils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.ParseException;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReceiptCreationFinalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiptCreationFinalFragment extends Fragment{
    public static final String NEW_RECEIPT_RESULT_KEY = "NEW_RECEIPT";
    private static final String ARG_PARAM1 = "param1";
    private static final String TAG = "ReceiptCreationFinalFragment";
    private static final String DEBOUNCE_MERCHANT_NAME_KEY = "DEBOUNCE_MERCHANT_NAME";
    private static final int DEBOUNCE_MERCHANT_NAME_INTERVAL = 1000;

    private ReceiptWithImage receiptWithImage;

    private FragmentReceiptCreationFinalBinding binding;

    private TextInputLayout merchantTextField;
    private TextInputLayout refTextField;
    private TextInputLayout characteristicAmountTF;
    private TextInputLayout mantissaAmountTF;
    private TextView dateTextView;
    private TextView timeTextView;
    private ImageButton editDateButton;
    private ImageButton editTimeButton;
    private Button addReceiptButton;

    private ProgressBar progressBar;
    private ProgressBar progressBarSuggestionChips;
    private ChipGroup chipGroup;
    private TextView merchantSuggestionsMessage;

    private boolean triggerMerchantSuggestionsSearch = true;



    public ReceiptCreationFinalFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param receiptWithImage ReceiptWithImage object.
     * @return A new instance of fragment ReceiptCreationFinalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReceiptCreationFinalFragment newInstance(ReceiptWithImage receiptWithImage) {
        ReceiptCreationFinalFragment fragment = new ReceiptCreationFinalFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, Parcels.wrap(receiptWithImage));
        fragment.setArguments(args);
        return fragment;
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            receiptWithImage = (ReceiptWithImage) Parcels.unwrap(getArguments().getParcelable(ARG_PARAM1));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ReceiptCreationActivity parentActivity = ((ReceiptCreationActivity) getActivity());
        if (parentActivity != null){
            parentActivity.changeToolbarTitle(getString(R.string.receipt_creation_final_fragment_toolbar_title));
        }

        binding = FragmentReceiptCreationFinalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Receipt receipt = receiptWithImage.getReceipt();

        merchantTextField = binding.merchantNameEdittext;
        refTextField = binding.refNumberEdittext;
        characteristicAmountTF = binding.characteristicAmountTextView;
        mantissaAmountTF = binding.mantissaAmountTextView;
        dateTextView = binding.dateTextView;
        timeTextView = binding.timeTextView;
        editDateButton = binding.editDateButton;
        editTimeButton = binding.editTimeButton;
        addReceiptButton = binding.addReceiptButton;
        progressBar = binding.pageProgressBar;
        progressBarSuggestionChips = binding.progressBarSuggestionChips;
        merchantSuggestionsMessage = binding.merchantSuggestionsMessage;
        chipGroup = binding.chipGroup;



        merchantTextField.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (triggerMerchantSuggestionsSearch){
                    chipGroup.clearCheck();

                    if (chipGroup.getChildCount() > 0){
                        chipGroup.removeAllViews();
                    }

                    // implemented debouncing to limit API requests
                    progressBarSuggestionChips.setVisibility(View.VISIBLE);
                    merchantSuggestionsMessage.setVisibility(View.VISIBLE);

                    Debouncer.call(DEBOUNCE_MERCHANT_NAME_KEY, new ReesitCallback() {
                        public void run() {
                            receipt.setMerchant(new Merchant(s.toString()));
                            fetchMerchantSuggestions(s.toString());
                        }
                    }, DEBOUNCE_MERCHANT_NAME_INTERVAL);

                } else {
                    triggerMerchantSuggestionsSearch = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        if (receipt.getMerchant() != null && merchantTextField.getEditText() != null){
            merchantTextField.getEditText().setText(receipt.getMerchant().getName());
        }

        if (receipt.getReferenceNumber() != null && refTextField.getEditText() != null){
            refTextField.getEditText().setText(receipt.getReferenceNumber());
        }

        if (receipt.getAmount() != null){
            String[] parts = receipt.getAmount().split("\\.");
            if (characteristicAmountTF.getEditText() != null){
                characteristicAmountTF.getEditText().setText(parts[0]);
            }
            if (mantissaAmountTF.getEditText() != null){
                mantissaAmountTF.getEditText().setText(parts[1]);
            }
        }

        if (receipt.getDateTimestamp() != null){
            Calendar receiptCal = Calendar.getInstance();
            receiptCal.setTimeInMillis(Long.parseLong(receipt.getDateTimestamp()));
            dateTextView.setText(DateTimeUtils.getDate(receiptCal));
            timeTextView.setText(DateTimeUtils.getTime(receiptCal));
        } else {
            dateTextView.setText(R.string.receipt_creation_final_date_text_default);
            timeTextView.setText(R.string.receipt_creation_final_time_text_default);
        }


        editDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                if (receipt.getDateTimestamp() != null){
                    int[] receiptDate = DateTimeUtils.getDate(receipt.getDateTimestamp());
                    datePickerFragment.setDialog(new DatePickerDialog(getContext(), datePickerFragment, receiptDate[2], receiptDate[1], receiptDate[0]));
                }
                datePickerFragment.setTimeSetListener((view, year, month, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(Long.parseLong(receipt.getDateTimestamp()));
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    receipt.setDateTimestamp(Long.toString(cal.getTimeInMillis()));


                    // refresh date text view
                    dateTextView.setText(DateTimeUtils.getDate(cal));
                });
                datePickerFragment.show(getParentFragmentManager(), TAG);
            }
        });

        editTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment timePickerFragment = new TimePickerFragment();
                if (receipt.getDateTimestamp() != null){
                    int[] receiptTime = DateTimeUtils.getTime(receipt.getDateTimestamp());
                    timePickerFragment.setDialog(new TimePickerDialog(getActivity(), timePickerFragment, receiptTime[0], receiptTime[1],
                            DateFormat.is24HourFormat(getActivity())));
                }
                timePickerFragment.setTimeSetListener((view, hourOfDay, minute) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(Long.parseLong(receipt.getDateTimestamp()));
                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    cal.set(Calendar.MINUTE, minute);
                    receipt.setDateTimestamp(Long.toString(cal.getTimeInMillis()));

                    // refresh time text view
                    timeTextView.setText(DateTimeUtils.getTime(cal));
                });
                timePickerFragment.show(getParentFragmentManager(), TAG);
            }
        });

        addReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (characteristicAmountTF.getEditText() != null && mantissaAmountTF.getEditText() != null){
                    String characteristic = characteristicAmountTF.getEditText().getText().toString();
                    String mantissa = mantissaAmountTF.getEditText().getText().toString();
                    if (characteristic.length() == 0 || mantissa.length() == 0){
                        Toast.makeText(getContext(), getText(R.string.receipt_creation_final_invalid_amount_error_message), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    receipt.setAmount(characteristic + "." + mantissa);
                }
                if (merchantTextField.getEditText() != null){
                    String merchant = merchantTextField.getEditText().getText().toString();
                    if (merchant.length() == 0){
                        Toast.makeText(getContext(), getText(R.string.receipt_creation_final_invalid_merchant_error_message), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (refTextField.getEditText() != null){
                    receipt.setReferenceNumber(refTextField.getEditText().getText().toString());
                }


                if (receiptWithImage.getImageFile() != null){
                    setPageStateLoading();
                    ReceiptService.addReceipt(receipt, new File(FileUtils.getImagePathFromURI(getContext(), receiptWithImage.getImageFile(), TAG)), new AddReceiptCallback() {
                        @Override
                        public void done(Receipt newReceipt, ParseException e) {
                            setPageStateNotLoading();
                            if (e == null){
                                if (getContext() != null){
                                    Intent intent = new Intent();
                                    intent.putExtra(NEW_RECEIPT_RESULT_KEY, Parcels.wrap(newReceipt));
                                    if (getActivity() != null){

                                        // TODO: COMPLETE THIS, GET NEW RECEIPT FROM DB AND RETURN TO PREVIOUS ACTIVITY
                                        getActivity().setResult(Activity.RESULT_OK, intent);
                                        getActivity().finish();
//                                        ((ReceiptCreationActivity) getContext()).startActivity(new Intent(getContext(), MainActivity.class));
//                                        ((ReceiptCreationActivity) getContext()).finish();
                                    } else {
                                        Toast.makeText(getContext(), getText(R.string.generic_error_message), Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "getActivity() returned null");
                                    }

                                }
                            } else {
                                Toast.makeText(getContext(), getText(R.string.receipt_creation_final_add_receipt_error_message), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error saving receipt", e);
                            }
                        }
                    });
                }



            }
        });
    }

    private void fetchMerchantSuggestions(String merchantName){
        MerchantService.getSuggestedMerchants(merchantName, new GetMerchantsCallback() {
            @Override
            public void done(List<Merchant> merchants, ParseException e) {
                if (e == null){
                    for(Merchant suggestedMerchant: merchants){
                        Chip chip = new Chip(getContext());
                        chip.setText(suggestedMerchant.getName());
                        chip.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                triggerMerchantSuggestionsSearch = false;
                                merchantTextField.getEditText().setText(suggestedMerchant.getName());
                                merchantTextField.getEditText().setSelection(suggestedMerchant.getName().length());
                                receiptWithImage.getReceipt().setMerchant(suggestedMerchant);
                                chip.setChecked(true);
                            }
                        });
                        chipGroup.addView(chip);
                    }
                } else {
                    Log.e(TAG, "Error getting suggested merchants",e);
                }
                progressBarSuggestionChips.setVisibility(View.INVISIBLE);
                merchantSuggestionsMessage.setVisibility(View.INVISIBLE);
            }
        });
    }


    private void setPageStateLoading(){
        merchantTextField.setEnabled(false);
        editTimeButton.setEnabled(false);
        editDateButton.setEnabled(false);
        refTextField.setEnabled(false);
        characteristicAmountTF.setEnabled(false);
        mantissaAmountTF.setEnabled(false);
        addReceiptButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setPageStateNotLoading(){
        merchantTextField.setEnabled(true);
        editTimeButton.setEnabled(true);
        editDateButton.setEnabled(true);
        refTextField.setEnabled(true);
        characteristicAmountTF.setEnabled(true);
        mantissaAmountTF.setEnabled(true);
        addReceiptButton.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
    }
}