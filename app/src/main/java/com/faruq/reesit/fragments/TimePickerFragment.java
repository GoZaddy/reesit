package com.faruq.reesit.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

  private @Nullable TimePickerDialog.OnTimeSetListener timeSetListener;
    private Dialog dialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        if (dialog != null){
            return dialog;
        } else {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }
    }

    public void setTimeSetListener(@NonNull TimePickerDialog.OnTimeSetListener listener){
        this.timeSetListener = listener;
    }

    public void setDialog(Dialog dialog){
        this.dialog = dialog;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (timeSetListener != null){
            timeSetListener.onTimeSet(view, hourOfDay, minute);
        }
    }
}
