package com.example.reesit.services;

import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class UserService {
    public static void signUpNewUser(String email, String password, SignUpCallback callback){
        ParseUser user = new ParseUser();
        // Set the user's username and password, which can be obtained by a forms
        user.setEmail(email);
        user.setUsername(email);
        user.setPassword(password);
        user.signUpInBackground(callback);
    }

    public static void loginUser(String email, String password, LogInCallback callback){
        ParseUser.logInInBackground(email, password, callback);
    }

    public static void logOutUser(LogOutCallback callback){
        ParseUser.logOutInBackground(callback);
    }
}
