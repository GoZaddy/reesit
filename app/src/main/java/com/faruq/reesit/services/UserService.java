package com.faruq.reesit.services;

import com.faruq.reesit.models.ReceiptCategory;
import com.faruq.reesit.models.User;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class UserService {
    public interface CreateNewUserCallback {
        void done(Exception e);
    }
    public static void signUpNewUser(String email, String password, CreateNewUserCallback callback){
        ParseUser user = new ParseUser();
        // Set the user's username and password, which can be obtained by a forms
        user.setEmail(email);
        user.setUsername(email);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    CategoryService.createReimbursementCategoryForUser(User.fromParseUser(user), new CategoryService.AddCategoryCallback() {
                        @Override
                        public void done(ReceiptCategory newCategory, Exception createCategoryException) {
                            callback.done(createCategoryException);
                        }
                    });
                } else {
                    callback.done(e);
                }
            }
        });
    }

    public static void loginUser(String email, String password, LogInCallback callback){
        ParseUser.logInInBackground(email, password, callback);
    }

    public static void logOutUser(LogOutCallback callback){
        ParseUser.logOutInBackground(callback);
    }
}
