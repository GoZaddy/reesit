package com.faruq.reesit.models;

import com.parse.ParseUser;

import org.parceler.Parcel;

// User is a wrapper class that simply wraps around ParseUser to reduce amount of redundant code used by clients
@Parcel
public class User {
    private ParseUser parseUser;
    public static User fromParseUser(ParseUser user){
        User result = new User();
        result.parseUser = user;
        return result;
    }

    public boolean isLoggedIn(){
        return parseUser != null;
    }

    public ParseUser getParseUser(){ return parseUser; }

    public String getID(){
        return parseUser.getObjectId();
    }

    public static User getCurrentUser(){
        if (ParseUser.getCurrentUser() != null){
            return User.fromParseUser(ParseUser.getCurrentUser());
        } else {
            return null;
        }
    }

}
