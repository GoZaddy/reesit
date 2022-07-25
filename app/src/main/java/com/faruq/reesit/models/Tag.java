package com.faruq.reesit.models;

import com.parse.ParseObject;

import org.parceler.Parcel;

import java.util.Locale;

@Parcel
public class Tag {
    private String id;
    private String name;
    private String slug;
    private String userID;
    private ParseObject parseObject;

    public static final String KEY_NAME = "name";
    public static final String KEY_ID = "id";
    public static final String KEY_SLUG = "slug";
    public static final String KEY_USER = "user";
    public static final String PARSE_CLASS_NAME = "Tag";


    public Tag(){}

    public Tag(String name, User user){
        this.name = name;
        this.slug = getSlugFromName(name);
        this.userID = user.getID();
    }

    public static Tag fromParseObject(ParseObject parseObject){
        Tag tag = new Tag();
        tag.parseObject = parseObject;
        tag.name = parseObject.getString(KEY_NAME);
        tag.userID = parseObject.getParseUser(KEY_USER).getObjectId();
        tag.id = parseObject.getObjectId();
        tag.slug = parseObject.getString(KEY_SLUG);
        return tag;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public ParseObject getParseObject() {
        return parseObject;
    }

    public String getSlug(){
        return slug;
    }

    public String getId() {
        return id;
    }

    public static String getSlugFromName(String name){
        return name.toLowerCase(Locale.ROOT);
    }
}
