package com.example.reesit.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reesit.R;
import com.example.reesit.activities.FilterActivity;
import com.example.reesit.databinding.FragmentFilterReceiptsBinding;
import com.example.reesit.misc.Debouncer;
import com.example.reesit.misc.Filter;
import com.example.reesit.models.Merchant;
import com.example.reesit.models.Tag;
import com.example.reesit.models.User;
import com.example.reesit.services.MerchantService;
import com.example.reesit.services.TagService;
import com.example.reesit.utils.CurrencyUtils;
import com.example.reesit.utils.DateTimeUtils;
import com.example.reesit.utils.ReesitCallback;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FilterReceiptsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterReceiptsFragment extends BottomSheetDialogFragment {

    private static final String ARG_PARAM1 = "param1";
    public static final String FRAGMENT_RESULT_KEY = "FILTER_RECEIPTS_BOTTOM_SHEET_FRAGMENT_RESULT_KEY";
    public static final String FRAGMENT_RESULT_NEW_FILTER_KEY = "FRAGMENT_RESULT_NEW_FILTER_KEY";
    public static final String TAG = "FilterReceiptsBottomSheet";
    private static final String DEBOUNCE_MERCHANT_NAME_KEY = "FILTER_DEBOUNCE_MERCHANT_NAME_KEY";
    private static final Integer DEBOUNCE_MERCHANT_NAME_INTERVAL = 1000;
    private static final String DEBOUNCE_TAG_NAME_KEY = "FILTER_DEBOUNCE_TAG_NAME_KEY";
    private static final Integer DEBOUNCE_TAG_NAME_INTERVAL = 1000;
    private static final int MAX_NUMBER_OF_MERCHANT_FILTER = 5;

    private Filter filter;

    private FragmentFilterReceiptsBinding binding;

    private TextView merchantSuggestionsLoadingText;
    private ProgressBar merchantSuggestionsProgressBar;
    private ChipGroup merchantSuggestionsChipGroup;
    private ChipGroup selectedMerchantsChipGroup;
    private TextInputLayout greaterThanEditText;
    private TextInputLayout lessThanEditText;
    private TextInputLayout merchantsEditText;
    private TextView afterDateText;
    private TextView beforeDateText;
    private ImageButton editAfterDateButton;
    private ImageButton editBeforeDateButton;
    private Button applyFilterButton;
    private TextInputLayout tagsEditText;
    private ProgressBar tagsSuggestionsProgressBar;
    private TextView tagsSuggestionsLoadingMessage;
    private ChipGroup tagsSuggestionsChipGroup;


    public FilterReceiptsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param filter Filter object representing initial state of filter bottom sheet
     * @return A new instance of fragment FilterReceiptsBottomSheet.
     */
    public static FilterReceiptsFragment newInstance(Filter filter) {
        FilterReceiptsFragment fragment = new FilterReceiptsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, Parcels.wrap(filter));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filter = (Filter) Parcels.unwrap(getArguments().getParcelable(ARG_PARAM1));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFilterReceiptsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        merchantSuggestionsLoadingText = binding.filterMerchantSuggestionsMessage;
        merchantSuggestionsProgressBar = binding.filterProgressBarSuggestionChips;

        applyFilterButton = binding.applyFilterButton;

        greaterThanEditText = binding.greaterThanEdittext;
        lessThanEditText = binding.lessThanEdittext;
        beforeDateText = binding.beforeDateTextView;
        afterDateText = binding.afterDateTextView;
        editAfterDateButton = binding.afterDateButton;
        editBeforeDateButton = binding.beforeDateButton;
        merchantsEditText = binding.merchantsEditText;

        merchantSuggestionsChipGroup = binding.merchantSuggestionsChipGroup;
        selectedMerchantsChipGroup = binding.selectedMerchantsChipGroup;

        tagsSuggestionsProgressBar = binding.progressBarTagSuggestions;
        tagsSuggestionsLoadingMessage = binding.tagSuggestionsLoadingMessage;
        tagsSuggestionsChipGroup = binding.tagSuggestionsChipGroup;
        tagsEditText = binding.tagEditText;

        tagsSuggestionsChipGroup.setSingleSelection(true);

        // listen to changes to the filter object
        getParentFragmentManager().setFragmentResultListener(FilterActivity.FILTER_CHANGE_FRAGMENT_RESULT_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                filter = (Filter) Parcels.unwrap(result.getParcelable(FilterActivity.FILTER_CHANGE_FRAGMENT_NEW_FRAGMENT_RESULT_KEY));
                setFieldValues();
            }
        });


        // set field values
        setFieldValues();


        // functionality for merchants section
        Objects.requireNonNull(merchantsEditText.getEditText()).addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (merchantSuggestionsChipGroup.getChildCount() > 0){
                            merchantSuggestionsChipGroup.removeAllViews();
                        }

                        setMerchantsSuggestionLoading();
                        Debouncer.call(DEBOUNCE_MERCHANT_NAME_KEY, new ReesitCallback() {
                            public void run() {
                                fetchMerchantSuggestions(s.toString());
                            }
                        }, DEBOUNCE_MERCHANT_NAME_INTERVAL);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                }
        );

        // functionality for tag section
        Objects.requireNonNull(tagsEditText.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tagsSuggestionsChipGroup.clearCheck();
                if (tagsSuggestionsChipGroup.getChildCount() > 0){
                    tagsSuggestionsChipGroup.removeAllViews();
                }

                setTagSuggestionsLoading();
                Debouncer.call(DEBOUNCE_TAG_NAME_KEY, new ReesitCallback() {
                    public void run() {
                        fetchTagSuggestions(s.toString());
                    }
                }, DEBOUNCE_TAG_NAME_INTERVAL);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // functionality for date-time section
        editBeforeDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                if (filter.getBeforeDateTimestamp() != null){
                    int[] receiptDate = DateTimeUtils.getDate(filter.getBeforeDateTimestamp());
                    datePickerFragment.setDialog(new DatePickerDialog(getContext(), datePickerFragment, receiptDate[2], receiptDate[1], receiptDate[0]));
                }
                datePickerFragment.setTimeSetListener((view, year, month, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    filter.setBeforeDateTimestamp(Long.toString(cal.getTimeInMillis()));


                    // refresh date text view
                    beforeDateText.setText(DateTimeUtils.getDate(cal));
                });
                datePickerFragment.show(getParentFragmentManager(), TAG);
            }
        });

        editAfterDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                if (filter.getAfterDateTimestamp() != null){
                    int[] receiptDate = DateTimeUtils.getDate(filter.getAfterDateTimestamp());
                    datePickerFragment.setDialog(new DatePickerDialog(getContext(), datePickerFragment, receiptDate[2], receiptDate[1], receiptDate[0]));
                }
                datePickerFragment.setTimeSetListener((view, year, month, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    filter.setAfterDateTimestamp(Long.toString(cal.getTimeInMillis()));


                    // refresh date text view
                    afterDateText.setText(DateTimeUtils.getDate(cal));
                });
                datePickerFragment.show(getParentFragmentManager(), TAG);
            }
        });


        // functionality for 'apply' button
        applyFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set greaterThanAmount and lessThanAmount
                if (greaterThanEditText.getEditText() != null){
                    if (greaterThanEditText.getEditText().getText().toString().length() != 0){
                        try {
                            filter.setGreaterThanAmount(CurrencyUtils.stringToCurrency(greaterThanEditText.getEditText().getText().toString()));
                        } catch (CurrencyUtils.CurrencyUtilsException e) {
                            greaterThanEditText.setError(e.getMessage());
                            return;
                        }
                    } else {
                        filter.setGreaterThanAmount(null);
                    }
                }

                if (lessThanEditText.getEditText() != null){
                    if (lessThanEditText.getEditText().getText().toString().length() != 0){
                        try {
                            filter.setLessThanAmount(CurrencyUtils.stringToCurrency(lessThanEditText.getEditText().getText().toString()));
                        } catch (CurrencyUtils.CurrencyUtilsException e) {
                            lessThanEditText.setError(e.getMessage());
                            return;
                        }
                    } else {
                        filter.setLessThanAmount(null);
                    }
                }

                // validate beforeDateTimestamp and afterDateTimestamp
                try {
                   filter.validate(getContext());
                } catch (Filter.FilterValidationException e){
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (getContext() != null){
                    Intent intent = new Intent();
                    intent.putExtra(FilterActivity.FILTER_OBJECT_RESULT_INTENT_KEY, Parcels.wrap(filter));
                    if (getActivity() != null){
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();

                    } else {
                        Toast.makeText(getContext(), getText(R.string.generic_error_message), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "getActivity() returned null");
                    }
                }
            }
        });


    }

    private void setMerchantsSuggestionLoading(){
        merchantSuggestionsProgressBar.setVisibility(View.VISIBLE);
        merchantSuggestionsLoadingText.setVisibility(View.VISIBLE);
    }

    private void setMerchantsSuggestionNotLoading(){
        merchantSuggestionsProgressBar.setVisibility(View.INVISIBLE);
        merchantSuggestionsLoadingText.setVisibility(View.INVISIBLE);
    }
    private void fetchMerchantSuggestions(String merchantName){
        MerchantService.getSuggestedMerchants(merchantName, new MerchantService.GetMerchantsCallback() {
            @Override
            public void done(List<Merchant> merchants, Exception e) {
                if (e == null){
                    for(Merchant suggestedMerchant: merchants){
                        Chip chip = new Chip(getContext());
                        chip.setText(suggestedMerchant.getName());
                        chip.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectMerchant(suggestedMerchant);
                            }
                        });
                        merchantSuggestionsChipGroup.addView(chip);
                    }
                } else {
                    Log.e(TAG, "Error getting suggested merchants",e);
                }
                setMerchantsSuggestionNotLoading();
            }
        });
    }

    private void selectMerchant(Merchant merchant){
        // add merchant
        if (filter.getMerchants() == null){
            filter.setMerchants(new ArrayList<>());
        } else {
            if (filter.getMerchants().size() >= MAX_NUMBER_OF_MERCHANT_FILTER){
                merchantsEditText.setError(getString(R.string.filter_too_many_merchants_error_message, MAX_NUMBER_OF_MERCHANT_FILTER));
                return;
            }
        }


        if (!isMerchantInList(merchant.getId())){
            filter.getMerchants().add(merchant);
            Chip chip  = new Chip(getContext());
            chip.setText(merchant.getName());
            chip.setCheckable(true);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unselectMerchant(filter.getMerchants().size()-1);
                }
            });

            chip.setChecked(true);
            selectedMerchantsChipGroup.addView(chip);
        }
    }

    private void unselectMerchant(Integer index){
        filter.getMerchants().remove(index.intValue());
        selectedMerchantsChipGroup.removeViewAt(index);
    }

    private boolean isMerchantInList(String merchantID){
        if (filter.getMerchants() != null){
            for(Merchant merchant: filter.getMerchants()){
                if (Objects.equals(merchant.getId(), merchantID)){
                    return true;
                }
            }
        } else {
            return false;
        }
        return false;
    }

    private void setFieldValues(){
        if (greaterThanEditText.getEditText() != null){
            if (filter.getGreaterThanAmount() != null){
                greaterThanEditText.getEditText().setText(CurrencyUtils.integerToCurrency(filter.getGreaterThanAmount()));
            } else {
                greaterThanEditText.getEditText().setText("");
            }
        }


        if (lessThanEditText.getEditText() != null){
            if (filter.getLessThanAmount() != null){
                lessThanEditText.getEditText().setText(CurrencyUtils.integerToCurrency(filter.getLessThanAmount()));
            } else {
                lessThanEditText.getEditText().setText("");
            }
        }


        if (filter.getBeforeDateTimestamp() != null){
            beforeDateText.setText(DateTimeUtils.getDateWithLongMonth(filter.getBeforeDateTimestamp()));
        } else {
            beforeDateText.setText(getString(R.string.filter_modal_no_date_selected_text));
        }

        if (filter.getAfterDateTimestamp() != null){
            afterDateText.setText(DateTimeUtils.getDateWithLongMonth(filter.getAfterDateTimestamp()));
        } else {
            afterDateText.setText(getString(R.string.filter_modal_no_date_selected_text));
        }

        if (filter.getMerchants() != null){
            for(int i = 0; i < filter.getMerchants().size(); ++i){
                Merchant merchant = filter.getMerchants().get(i);
                Chip chip  = new Chip(requireContext());
                chip.setText(merchant.getName());
                chip.setCheckable(true);
                int finalI = i;
                chip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        unselectMerchant(finalI);
                    }
                });

                chip.setChecked(true);
                selectedMerchantsChipGroup.addView(chip);
            }
        } else {
            selectedMerchantsChipGroup.removeAllViews();
        }

        if (filter.getTag() != null){
            if (tagsSuggestionsChipGroup.getChildCount() > 0){
                tagsSuggestionsChipGroup.removeAllViews();
            }
            tagsSuggestionsChipGroup.clearCheck();
            Chip chip = new Chip(requireContext());
            chip.setText(filter.getTag().getName());
            chip.setCheckable(true);
            chip.setChecked(true);
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked){
                        filter.setTag(null);
                        tagsSuggestionsChipGroup.removeAllViews();
                        setTagSuggestionsLoading();
                        fetchTags();
                    }
                }
            });
            tagsSuggestionsChipGroup.addView(chip);
        } else {
            setTagSuggestionsLoading();
            fetchTags();
        }
    }

    private void fetchTags(){
        TagService.getTags(Objects.requireNonNull(User.getCurrentUser()), new TagService.GetTagsCallback() {
            @Override
            public void done(List<Tag> tags, Exception e) {
                setTagSuggestionsNotLoading();
                if (e == null){
                    if (tagsSuggestionsChipGroup.getChildCount() > 0){
                        tagsSuggestionsChipGroup.clearCheck();
                        tagsSuggestionsChipGroup.removeAllViews();
                    }
                    for(Tag tag: tags){
                        Chip chip = new Chip(requireContext());
                        chip.setText(tag.getName());
                        chip.setCheckable(true);
                        chip.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (tagsSuggestionsChipGroup.getCheckedChipId() == View.NO_ID){
                                    // enable text field so users can get suggestions
                                    filter.setTag(null);
                                    tagsEditText.setEnabled(true);
                                } else {
                                    filter.setTag(tag);
                                    tagsEditText.setEnabled(false);
                                }
                            }
                        });
                        tagsSuggestionsChipGroup.addView(chip);
                    }
                } else {
                    Log.e(TAG, "Error fetching tags", e);
                }

            }
        });
    }
    private void fetchTagSuggestions(String input){
        TagService.getSuggestedTags(input, Objects.requireNonNull(User.getCurrentUser()), new TagService.GetTagsCallback() {
            @Override
            public void done(List<Tag> tags, Exception e) {
                setTagSuggestionsNotLoading();
                if (e == null){
                    if (tags.size() > 0){
                        for(Tag tag: tags){
                            Chip chip = new Chip(requireContext());
                            chip.setText(tag.getName());
                            chip.setCheckable(true);
                            chip.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (tagsSuggestionsChipGroup.getCheckedChipId() == View.NO_ID){
                                        // enable text field so users can get suggestions
                                        filter.setTag(null);
                                        tagsEditText.setEnabled(true);
                                    } else {
                                        filter.setTag(tag);
                                        tagsEditText.setEnabled(false);
                                    }
                                }
                            });
                            tagsSuggestionsChipGroup.addView(chip);
                        }
                    } else {
                        // if there are no suggestions just get the tags they've added
                        setTagSuggestionsLoading();
                        fetchTags();
                    }
                } else {
                    Log.e(TAG, "Error fetching tag suggestions", e);
                }

            }
        });
    }

    private void setTagSuggestionsLoading(){
        tagsSuggestionsProgressBar.setVisibility(View.VISIBLE);
        tagsSuggestionsLoadingMessage.setVisibility(View.VISIBLE);
    }

    private void setTagSuggestionsNotLoading(){
        tagsSuggestionsProgressBar.setVisibility(View.INVISIBLE);
        tagsSuggestionsLoadingMessage.setVisibility(View.INVISIBLE);
    }
}