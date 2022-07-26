package com.faruq.reesit.utils;

public class Validator {
    public static boolean isValidEmail(String email){
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        return email.length() > 0 && email.matches(emailPattern);
    }


    public static boolean isValidFloat(String floatValue){
        floatValue = floatValue.replaceAll("\\$", "");
        try{
            Double.valueOf(floatValue);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }
}
