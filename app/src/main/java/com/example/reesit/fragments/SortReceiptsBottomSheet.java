package com.example.reesit.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.reesit.R;
import com.example.reesit.databinding.ModalSortReceiptsBottomSheetBinding;
import com.example.reesit.misc.SortReceiptOption;
import com.example.reesit.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SortReceiptsBottomSheet#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SortReceiptsBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_PARAM1 = "param1";
    public static final String FRAGMENT_RESULT_CHECKED_OPTION_KEY = "FRAGMENT_RESULT_CHECKED_OPTION_KEY";
    public static final String TAG = "SortReceiptsBottomSheet";
    public static final String FRAGMENT_RESULT_KEY = "SORT_RECEIPTS_KEY";
    private static final String ARG_PARAM2 = "param2";

    private ArrayList<SortReceiptOption> sortOptions;

    private ModalSortReceiptsBottomSheetBinding binding;

    private RadioGroup radioGroup;
    private Button resetButton;

    private Integer currentOptionIndex;

    private Integer defaultCurrentOptionIndex;



    public SortReceiptsBottomSheet() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param sortOptions List of sorting options.
     * @return A new instance of fragment SortReceiptsBottomSheet.
     */
    public static SortReceiptsBottomSheet newInstance(ArrayList<SortReceiptOption> sortOptions, int currentSortOptionIndex) {
        SortReceiptsBottomSheet fragment = new SortReceiptsBottomSheet();
        Bundle args = new Bundle();

        ArrayList<Parcelable> parcelableList = new ArrayList<>();
        for(SortReceiptOption option: sortOptions){
            parcelableList.add(Parcels.wrap(option));
        }
        args.putParcelableArrayList(ARG_PARAM1, parcelableList);
        args.putInt(ARG_PARAM2, currentSortOptionIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ArrayList<Parcelable> parcelableList = getArguments().getParcelableArrayList(ARG_PARAM1);
            sortOptions = new ArrayList<>();
            for(Parcelable parcelable: parcelableList){
                sortOptions.add(Parcels.unwrap(parcelable));
            }

            currentOptionIndex = getArguments().getInt(ARG_PARAM2);
            defaultCurrentOptionIndex = currentOptionIndex;

        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = ModalSortReceiptsBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        radioGroup = binding.radioGroup;

        resetButton = binding.resetButton;

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // get current sorting option

                if (currentOptionIndex == null || currentOptionIndex != checkedId){
                    Bundle result = new Bundle();
                    result.putInt(FRAGMENT_RESULT_CHECKED_OPTION_KEY, checkedId);
                    currentOptionIndex = checkedId;
                    getParentFragmentManager().setFragmentResult(FRAGMENT_RESULT_KEY, result);
                }

            }
        });


        for(int i = 0; i < sortOptions.size(); ++i){
            SortReceiptOption option = sortOptions.get(i);
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setId(i);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (getContext() != null){
                params.setMargins(0, Utils.convertDPToPixels(getContext(), 16), 0, 0);
            }

            radioButton.setLayoutParams(params);
            radioButton.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            radioButton.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            radioButton.setText(option.getTitle());

            radioGroup.addView(radioButton);
        }

        if (currentOptionIndex != null && radioGroup.getCheckedRadioButtonId() == -1){
            radioGroup.check(currentOptionIndex);
        }

        resetButton = binding.resetButton;
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup.check(defaultCurrentOptionIndex);
            }
        });

    }
}