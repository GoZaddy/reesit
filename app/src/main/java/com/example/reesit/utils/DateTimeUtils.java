package com.example.reesit.utils;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class DateTimeUtils {
    // returns the day of month, month and year (in that order) in an int array
    public static int[] getDate(String unixTimestamp){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.parseLong(unixTimestamp));
        return new int[]{cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR)};
    }
    public static String getDate(Calendar cal){
        return cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                + " " + Utils.padStringLeft(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)), "0", 2)
                +", "
                + Integer.toString(cal.get(Calendar.YEAR));
    }

    public static String getDateWithLongMonth(String unixTimestamp){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.parseLong(unixTimestamp));

        return cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                + " " + Utils.padStringLeft(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)), "0", 2)
                +", "
                + Integer.toString(cal.get(Calendar.YEAR));
    }

    // returns the hour of day and minute (in that order) in an int array
    public static int[] getTime(String unixTimestamp){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.parseLong(unixTimestamp));
        return new int[]{cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)};
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
            hour = Utils.padStringLeft(Integer.toString(cal.get(Calendar.HOUR)), "0", 2);
        }

        return hour
                +" : "
                + Utils.padStringLeft(Integer.toString(cal.get(Calendar.MINUTE)), "0", 2)
                + " "
                + cal.getDisplayName(Calendar.AM_PM, Calendar.LONG, Locale.getDefault());
    }


    public static String getDateAndTimeReceiptCard(String unixTimestamp){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.parseLong(unixTimestamp));
        String result = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
                + " " + Utils.padStringLeft(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)), "0", 2)
                +", "
                + Integer.toString(cal.get(Calendar.YEAR)) + ". " + getTime(cal);



        return result;
    }


}
