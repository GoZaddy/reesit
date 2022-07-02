package com.example.reesit.models;

import com.parse.ParseUser;

// User is a wrapper class that simply wraps around ParseUser to reduce amount of redundant code used by clients
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

}
