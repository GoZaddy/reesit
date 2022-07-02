package com.example.reesit.misc;

import android.net.Uri;

import com.example.reesit.models.Receipt;

import org.parceler.Parcel;

import java.io.File;

@Parcel
public class ReceiptWithImage {
    public Receipt receipt;
    public Uri imageFile;

    public ReceiptWithImage(){}

    public ReceiptWithImage(Receipt receipt, Uri imageFile, String imageURL){
        this.receipt = receipt;
        this.imageFile = imageFile;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public Uri getImageFile() {
        return imageFile;
    }

    public void setImageFile(Uri imageFile) {
        this.imageFile = imageFile;
    }
}
