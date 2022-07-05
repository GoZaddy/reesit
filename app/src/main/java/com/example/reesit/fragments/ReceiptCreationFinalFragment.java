package com.example.reesit.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.reesit.R;
import com.example.reesit.activities.ReceiptCreationActivity;
import com.example.reesit.databinding.FragmentReceiptCreationFinalBinding;
import com.example.reesit.models.Receipt;
import com.example.reesit.utils.Utils;
import com.google.android.material.textfield.TextInputLayout;

import org.parceler.Parcels;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReceiptCreationFinalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiptCreationFinalFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String TAG = "ReceiptCreationFinalFragment";

    private Receipt receipt;

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


    public ReceiptCreationFinalFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param receipt Receipt object.
     * @return A new instance of fragment ReceiptCreationFinalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReceiptCreationFinalFragment newInstance(Receipt receipt) {
        ReceiptCreationFinalFragment fragment = new ReceiptCreationFinalFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, Parcels.wrap(receipt));
        fragment.setArguments(args);
        return fragment;
    }

    public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener{



        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(requireContext(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.parseLong(receipt.getDateTimestamp()));
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            receipt.setDateTimestamp(Long.toString(cal.getTimeInMillis()));

            // refresh date text view
            dateTextView.setText(Utils.getDate(cal));

        }
    }

    public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {



        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.parseLong(receipt.getDateTimestamp()));
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            cal.set(Calendar.MINUTE, minute);
            receipt.setDateTimestamp(Long.toString(cal.getTimeInMillis()));

            // refresh time text view
            timeTextView.setText(Utils.getTime(cal));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            receipt = (Receipt) Parcels.unwrap(getArguments().getParcelable(ARG_PARAM1));
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

        merchantTextField = binding.merchantNameEdittext;
        refTextField = binding.refNumberEdittext;
        characteristicAmountTF = binding.characteristicAmountTextView;
        mantissaAmountTF = binding.mantissaAmountTextView;
        dateTextView = binding.dateTextView;
        timeTextView = binding.timeTextView;
        editDateButton = binding.editDateButton;
        editTimeButton = binding.editTimeButton;
        addReceiptButton = binding.addReceiptButton;

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
            System.out.println("receipt cal to string: "+receiptCal.getTime().toString());
            dateTextView.setText(Utils.getDate(receiptCal));
            timeTextView.setText(Utils.getTime(receiptCal));
        } else {
            dateTextView.setText(R.string.receipt_creation_final_date_text_default);
            timeTextView.setText(R.string.receipt_creation_final_time_text_default);
        }


        editDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getParentFragmentManager(), TAG);
            }
        });

        editTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.show(getParentFragmentManager(), TAG);
            }
        });



    }
}