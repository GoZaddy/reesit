package com.example.reesit.models;

import com.parse.ParseObject;

import org.parceler.Parcel;

@Parcel
public class Merchant {
    public String id;
    public String name;

    public Merchant(){}

    public Merchant(String name){
        this.name = name;
    }

    public static Merchant fromParseObject(ParseObject object){
        // todo: write this

        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
