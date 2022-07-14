package com.example.reesit.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reesit.R;
import com.example.reesit.activities.ReceiptCreationActivity;
import com.example.reesit.activities.ReceiptDetailsActivity;
import com.example.reesit.databinding.FragmentReceiptCreationFinalBinding;
import com.example.reesit.misc.Debouncer;
import com.example.reesit.misc.ReceiptWithImage;
import com.example.reesit.misc.UriAndSource;
import com.example.reesit.models.Merchant;
import com.example.reesit.models.Receipt;
import com.example.reesit.models.Tag;
import com.example.reesit.models.User;
import com.example.reesit.services.MerchantService;
import com.example.reesit.services.ReceiptService;
import com.example.reesit.services.TagService;
import com.example.reesit.utils.CurrencyUtils;
import com.example.reesit.utils.DateTimeUtils;
import com.example.reesit.utils.FileUtils;
import com.example.reesit.utils.ReesitCallback;
import com.example.reesit.utils.Utils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.ParseException;

import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReceiptCreationFinalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiptCreationFinalFragment extends Fragment{
    public static final String NEW_RECEIPT_RESULT_KEY = "NEW_RECEIPT";
    private static final String ARG_PARAM1 = "param1";
    private static final String FRAGMENT_TASK = "FRAGMENT_TASK";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "ReceiptCreationFinalFragment";
    private static final String DEBOUNCE_MERCHANT_NAME_KEY = "DEBOUNCE_MERCHANT_NAME";
    private static final String DEBOUNCE_TAG_NAME_KEY = "DEBOUNCE_TAG_NAME_KEY";
    private static final int DEBOUNCE_MERCHANT_NAME_INTERVAL = 1000;
    private static final int MAX_NUMBER_OF_TAGS = 10;


    private Receipt receipt;
    private UriAndSource imageFile;

    private FragmentReceiptCreationFinalBinding binding;

    private TextInputLayout merchantTextField;
    private TextInputLayout refTextField;
    private TextInputLayout amountTF;
    private TextView dateTextView;
    private TextView timeTextView;
    private ImageButton editDateButton;
    private ImageButton editTimeButton;
    private Button addReceiptButton;

    private ProgressBar progressBar;
    private ProgressBar progressBarSuggestionChips;
    private ChipGroup chipGroup;
    private TextView merchantSuggestionsMessage;
    private ChipGroup suggestedTagsChipGroup;
    private ChipGroup selectedTagsChipGroup;
    private ProgressBar progressBarSuggestedTags;
    private TextView suggestedTagsLoadingMessage;
    private TextInputLayout tagsTextField;
    private TextView subTitle;

    private enum Task{
        CREATE_RECEIPT,
        UPDATE_RECEIPT
    }

    private Task task;

    private boolean triggerMerchantSuggestionsSearch = true;



    public ReceiptCreationFinalFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param receipt Receipt object.
     * @param uriAndSource UriAndSource
     * @return A new instance of fragment ReceiptCreationFinalFragment.
     */
    public static ReceiptCreationFinalFragment newInstance(Receipt receipt, UriAndSource uriAndSource) {
        ReceiptCreationFinalFragment fragment = new ReceiptCreationFinalFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, Parcels.wrap(receipt));
        args.putParcelable(ARG_PARAM2, Parcels.wrap(uriAndSource));
        args.putSerializable(FRAGMENT_TASK, Task.CREATE_RECEIPT);
        fragment.setArguments(args);
        return fragment;
    }

    public static ReceiptCreationFinalFragment newInstance(Receipt receipt){
        ReceiptCreationFinalFragment fragment = new ReceiptCreationFinalFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, Parcels.wrap(receipt));
        args.putSerializable(FRAGMENT_TASK, Task.UPDATE_RECEIPT);
        fragment.setArguments(args);
        return fragment;
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            task = (Task) getArguments().getSerializable(FRAGMENT_TASK);
            if (task == Task.CREATE_RECEIPT){
                receipt = (Receipt) Parcels.unwrap(getArguments().getParcelable(ARG_PARAM1));
                imageFile = (UriAndSource) Parcels.unwrap(getArguments().getParcelable(ARG_PARAM2));
            } else {
                receipt = (Receipt) Parcels.unwrap(getArguments().getParcelable(ARG_PARAM1));
            }

        }

        if (requireActivity() instanceof ReceiptDetailsActivity){
            setHasOptionsMenu(true);
            requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    getParentFragmentManager().beginTransaction().replace(R.id.receipt_details_fragment_container, ReceiptDetailsFragment.newInstance(receipt))
                            .setReorderingAllowed(true)
                            .commit();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (requireActivity() instanceof ReceiptDetailsActivity){
            switch(item.getItemId()){
                case android.R.id.home:
                    getParentFragmentManager().beginTransaction().replace(R.id.receipt_details_fragment_container, ReceiptDetailsFragment.newInstance(receipt))
                            .setReorderingAllowed(true)
                            .commit();
                    return true;
                default:
                    return false;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (task == Task.CREATE_RECEIPT){
            if (requireActivity() instanceof ReceiptCreationActivity){
                ReceiptCreationActivity parentActivity = ((ReceiptCreationActivity) requireActivity());
                parentActivity.changeToolbarTitle(getString(R.string.receipt_creation_final_fragment_toolbar_title));
            }
        } else {
            if (requireActivity() instanceof ReceiptDetailsActivity){
                // remove options menu from toolbar
                requireActivity().invalidateOptionsMenu();
                // set title
                Objects.requireNonNull(((ReceiptDetailsActivity) requireActivity()).getSupportActionBar()).setTitle(getString(R.string.update_receipt_toolbar_title));
            }
        }

        binding = FragmentReceiptCreationFinalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        merchantTextField = binding.merchantNameEdittext;
        refTextField = binding.refNumberEdittext;
        amountTF = binding.amountEdittext;
        dateTextView = binding.dateTextView;
        timeTextView = binding.timeTextView;
        editDateButton = binding.editDateButton;
        editTimeButton = binding.editTimeButton;
        addReceiptButton = binding.addReceiptButton;
        progressBar = binding.pageProgressBar;
        progressBarSuggestionChips = binding.progressBarSuggestionChips;
        merchantSuggestionsMessage = binding.merchantSuggestionsMessage;
        chipGroup = binding.merchantSuggestionsChipGroup;
        suggestedTagsLoadingMessage = binding.tagsSuggestionsLoadingMessage;
        suggestedTagsChipGroup = binding.tagsSuggestionsChipGroup;
        selectedTagsChipGroup = binding.selectedTagsChipGroup;
        progressBarSuggestedTags = binding.progressBarTagsSuggestions;
        tagsTextField = binding.tagsEdittext;
        subTitle = binding.subTitle;


        // set visibility for sub title
        if (task == Task.UPDATE_RECEIPT){
            subTitle.setVisibility(View.GONE);
        }

        // set field values
        if (receipt.getMerchant() != null && merchantTextField.getEditText() != null){
            merchantTextField.getEditText().setText(receipt.getMerchant().getName());
        }

        if (receipt.getReferenceNumber() != null && refTextField.getEditText() != null){
            refTextField.getEditText().setText(receipt.getReferenceNumber());
        }

        if (receipt.getAmount() != null){
            if (amountTF.getEditText() != null){
                amountTF.getEditText().setText(CurrencyUtils.integerToCurrency(receipt.getAmount()));
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

        if (receipt.getTags() != null && receipt.getTags().size() > 0){
            for(int index = 0; index < receipt.getTags().size(); ++index){
                Tag tag = receipt.getTags().get(index);
                Chip chip  = new Chip(requireContext());
                chip.setText(tag.getName());
                chip.setCheckable(true);
                int finalIndex = index;
                chip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        unselectTag(finalIndex);
                    }
                });

                chip.setChecked(true);
                selectedTagsChipGroup.addView(chip);
            }
        }



        // set button text if needed
        if (task == Task.UPDATE_RECEIPT){
            addReceiptButton.setText(getString(R.string.receipt_creation_final_update_button_text));
        }




        // set listener for merchant field
        Objects.requireNonNull(merchantTextField.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (triggerMerchantSuggestionsSearch){
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


        // set listener for tags field
        Objects.requireNonNull(tagsTextField.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (suggestedTagsChipGroup.getChildCount() > 0){
                    suggestedTagsChipGroup.removeAllViews();
                }

                // implemented debouncing to limit API requests
                setTagSuggestionsLoading();

                Debouncer.call(DEBOUNCE_TAG_NAME_KEY, new ReesitCallback() {
                    public void run() {
                        fetchTagSuggestions(s.toString());
                    }
                }, DEBOUNCE_MERCHANT_NAME_INTERVAL);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


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
                if (amountTF.getEditText() != null){
                    String amount = amountTF.getEditText().getText().toString();
                    Integer intValue;
                    try{
                        intValue = CurrencyUtils.stringToCurrency(amount);
                        receipt.setAmount(intValue);
                    } catch (CurrencyUtils.CurrencyUtilsException e) {
                        amountTF.setError(e.getMessage());
                        return;
                    }
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


                if (imageFile != null && task == Task.CREATE_RECEIPT){
                    setPageStateLoading();
                    ReceiptService.addReceipt(receipt, new File(FileUtils.getImagePathFromURI(getContext(), imageFile, TAG)), Objects.requireNonNull(User.getCurrentUser()), new ReceiptService.AddReceiptCallback() {
                        @Override
                        public void done(Receipt newReceipt, Exception e) {
                            setPageStateNotLoading();
                            if (e == null){
                                if (getContext() != null){
                                    Intent intent = new Intent();
                                    intent.putExtra(NEW_RECEIPT_RESULT_KEY, Parcels.wrap(newReceipt));
                                    if (getActivity() != null){
                                        getActivity().setResult(Activity.RESULT_OK, intent);
                                        getActivity().finish();
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
                } else {
                    setPageStateLoading();
                    ReceiptService.updateReceipt(receipt, Objects.requireNonNull(User.getCurrentUser()), new ReceiptService.UpdateReceiptCallback() {
                        @Override
                        public void done(Receipt newReceipt, Exception e) {
                            setPageStateNotLoading();
                            if (e == null){
                                getParentFragmentManager().beginTransaction().replace(R.id.receipt_details_fragment_container, ReceiptDetailsFragment.newInstance(newReceipt))
                                        .setReorderingAllowed(true)
                                        .commit();
                            } else {
                                Toast.makeText(getContext(), getText(R.string.receipt_creation_final_update_receipt_error_message), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error saving receipt", e);
                            }
                        }
                    });
                }
            }
        });
    }

    private void fetchMerchantSuggestions(String merchantName){
        MerchantService.getSuggestedMerchants(merchantName, new MerchantService.GetMerchantsCallback() {
            @Override
            public void done(List<Merchant> merchants, Exception e) {
                if (e == null){
                    for(Merchant suggestedMerchant: merchants){
                        Chip chip = new Chip(requireContext());
                        chip.setText(suggestedMerchant.getName());
                        chip.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                triggerMerchantSuggestionsSearch = false;
                                merchantTextField.getEditText().setText(suggestedMerchant.getName());
                                merchantTextField.getEditText().setSelection(suggestedMerchant.getName().length());
                                receipt.setMerchant(suggestedMerchant);
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
        amountTF.setEnabled(false);
        addReceiptButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setPageStateNotLoading(){
        merchantTextField.setEnabled(true);
        editTimeButton.setEnabled(true);
        editDateButton.setEnabled(true);
        refTextField.setEnabled(true);
        amountTF.setEnabled(true);
        addReceiptButton.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void setTagSuggestionsLoading(){
        suggestedTagsLoadingMessage.setVisibility(View.VISIBLE);
        progressBarSuggestedTags.setVisibility(View.VISIBLE);
    }

    private void setTagSuggestionsNotLoading(){
        suggestedTagsLoadingMessage.setVisibility(View.INVISIBLE);
        progressBarSuggestedTags.setVisibility(View.INVISIBLE);
    }

    private void fetchTagSuggestions(String input){
        TagService.getSuggestedTags(input, User.getCurrentUser(), new TagService.GetTagsCallback() {
            @Override
            public void done(List<Tag> tags, Exception e) {
                if (e == null){
                    for(Tag suggestedTag: tags){
                        Chip chip = new Chip(requireContext());
                        chip.setText(suggestedTag.getName());
                        chip.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectTag(suggestedTag);
                            }
                        });
                        suggestedTagsChipGroup.addView(chip);
                    }

                    if (tags.size() == 0){
                        // if there are no suggested tags, give users the option to create their own tag
                        tagsTextField.setEndIconDrawable(R.drawable.ic_baseline_add_circle_outline_24);
                        tagsTextField.setEndIconOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Tag tag = new Tag(Objects.requireNonNull(tagsTextField.getEditText()).getText().toString(), Objects.requireNonNull(User.getCurrentUser()));
                                selectTag(tag);
                            }
                        });
                        tagsTextField.setEndIconVisible(true);
                    } else {
                        tagsTextField.setEndIconVisible(false);
                        tagsTextField.setEndIconOnClickListener(null);
                    }
                } else {
                    Log.e(TAG, "Error getting suggested merchants",e);
                }
                setTagSuggestionsNotLoading();
            }
        });
    }

    private void selectTag(Tag tag){
        // add merchant
        if (receipt.getTags() == null){
            receipt.setTags(new ArrayList<>());
        } else {
            if (receipt.getTags().size() >= MAX_NUMBER_OF_TAGS){
                tagsTextField.setError(getString(R.string.receipt_creation_final_too_many_tags_error_message, MAX_NUMBER_OF_TAGS));
                return;
            }
        }


        if (tag.getId() == null || !isTagInList(tag.getId())){
            receipt.getTags().add(tag);
            Chip chip = new Chip(requireContext());
            chip.setText(tag.getName());
            chip.setCheckable(true);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unselectTag(receipt.getTags().size()-1);
                }
            });

            chip.setChecked(true);
            selectedTagsChipGroup.addView(chip);
        }
    }


    private void unselectTag(Integer index){
        receipt.getTags().remove(index.intValue());
        selectedTagsChipGroup.removeViewAt(index);
    }

    private boolean isTagInList(String tagID){
        if (receipt.getTags() != null){
            for(Tag tag: receipt.getTags()){
                if (Objects.equals(tag.getId(), tagID)){
                    return true;
                }
            }
        } else {
            return false;
        }
        return false;
    }
}