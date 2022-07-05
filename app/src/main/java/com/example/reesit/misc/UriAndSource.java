package com.example.reesit.misc;

import android.net.Uri;

import org.parceler.Parcel;

@Parcel
public class UriAndSource {
    private Uri uri;
    private String source;
    public static final String GALLERY = "gallery";
    public static final String CAMERA = "camera";

    // empty constructor for Parcel
    UriAndSource(){}

    UriAndSource(Uri uri, String source){
        this.uri = uri;
        this.source = source;
    }

    public static UriAndSource fromGallery(Uri uri){
        return new UriAndSource(uri, GALLERY);
    }

    public static UriAndSource fromCamera(Uri uri){
        return new UriAndSource(uri, CAMERA);
    }

    public Uri getUri() {
        return uri;
    }

    public String getSource() {
        return source;
    }
}
