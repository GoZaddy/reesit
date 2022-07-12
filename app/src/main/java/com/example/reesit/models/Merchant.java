package com.example.reesit.models;

import com.parse.ParseObject;

import org.parceler.Parcel;

import java.util.Locale;

@Parcel
public class Merchant {
    private String id;
    private String name;
    private ParseObject parseObject;
    // slugs help to ensure that merchants are unique
    private String slug;

    public static final String KEY_NAME = "name";
    public static final String KEY_SLUG = "slug";
    public static final String PARSE_CLASS_NAME = "Merchant";


    public Merchant(){}

    public Merchant(String name){
        this.name = name;
        this.slug = name.toLowerCase(Locale.ROOT).trim().replaceAll("(\\s+)", "");
    }

    public static Merchant fromParseObject(ParseObject object){
        Merchant merchant = new Merchant(object.getString(KEY_NAME));
        merchant.parseObject = object;
        merchant.id = object.getObjectId();
        return merchant;
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

    public ParseObject getParseObject() {
        return parseObject;
    }

    public String getSlug(){
        return slug;
    }

    public static String getSlugFromString(String merchantName){
        return merchantName.toLowerCase(Locale.ROOT).trim().replaceAll("(\\s+)", "");
    }
}
