package com.example.reesit.utils;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

// General utility functions
public class Utils {
    public static String getDate(Calendar cal){
        return cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                + " " + padStringLeft(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)), "0", 2)
                +", "
                + Integer.toString(cal.get(Calendar.YEAR));
    }

    public static String getTime(Calendar cal){
        String hour;
        if (cal.get(Calendar.HOUR) == 0){
            if (Objects.equals(cal.getDisplayName(Calendar.AM_PM, Calendar.LONG, Locale.getDefault()), "AM")){
                hour = "00";
            } else {
                hour = "12";
            }
        } else {
            hour = padStringLeft(Integer.toString(cal.get(Calendar.HOUR)), "0", 2);
        }

        return hour
                +" : "
                + padStringLeft(Integer.toString(cal.get(Calendar.MINUTE)), "0", 2)
                + " "
                + cal.getDisplayName(Calendar.AM_PM, Calendar.LONG, Locale.getDefault());
    }


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
}
