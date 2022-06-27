package com.example.reesit.utils;

public class Validator {
    public static boolean isValidEmail(String email){
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        return email.length() > 0 && email.matches(emailPattern);
    }
}