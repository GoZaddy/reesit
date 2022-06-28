package com.example.reesit.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexHelpers {
    // extracts a floating point number from the string argument
    public static String extractFloat(String value){
        Pattern pattern = Pattern.compile("([0-9]+\\.[0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()){
            return matcher.group();
        } else {
            return null;
        }
    }

    public static boolean findWholeWord(String input, String wordToFind){
        Pattern pattern = Pattern.compile("\\b(" + wordToFind + ")\\b");
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }
}
