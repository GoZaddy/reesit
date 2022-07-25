package com.faruq.reesit.misc;

import com.faruq.reesit.models.Receipt;

import org.parceler.Parcel;

@Parcel
public class ReceiptWithImage {
    public Receipt receipt;
    public UriAndSource imageFile;

    public ReceiptWithImage(){}

    public ReceiptWithImage(Receipt receipt, UriAndSource imageFile){
        this.receipt = receipt;
        this.imageFile = imageFile;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public UriAndSource getImageFile() {
        return imageFile;
    }

    public void setImageFile(UriAndSource imageFile) {
        this.imageFile = imageFile;
    }
}
