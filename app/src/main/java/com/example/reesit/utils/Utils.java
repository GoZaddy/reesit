package com.example.reesit.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

// General utility functions
public class Utils {
    /** Pads input string with paddingValue until input string reaches a certain length
     * @param input input string to be padded
     * @param paddingValue padding string
     * @param length target length
     * @return padded string
     */
    //
    public static String padStringLeft(String input, String paddingValue, Integer length){
        if (input.length() < length) {
            StringBuilder inputBuilder = new StringBuilder(input);
            while (inputBuilder.length() < length) {
                inputBuilder.insert(0, paddingValue);
            }
            input = inputBuilder.toString();
        }
        return input;
    }



    public static int convertDPToPixels(Context context, int dpValue){
        Resources r = context.getResources();

        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpValue,
                r.getDisplayMetrics()
        );
    }
}
