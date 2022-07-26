package com.faruq.reesit.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private @Nullable DatePickerDialog.OnDateSetListener onDateSetListener;
    private Dialog dialog;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        if (dialog != null){
            return dialog;
        } else {
            final Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getContext(), this, year, month, day);
        }

    }

    public void setTimeSetListener(@NonNull DatePickerDialog.OnDateSetListener listener){
        this.onDateSetListener = listener;
    }

    public void setDialog(Dialog dialog){
        this.dialog = dialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if (onDateSetListener != null){
            onDateSetListener.onDateSet(view, year, month, dayOfMonth);
        }
    }
}
