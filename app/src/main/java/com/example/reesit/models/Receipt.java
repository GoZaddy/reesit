package com.example.reesit.models;

import androidx.annotation.NonNull;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.parceler.Parcel;

import java.util.Date;


@Parcel
public class Receipt {
    public String id;
    public Merchant merchant;
    public String receiptText;
    public String amount;
    public String referenceNumber;
    public String dateTimestamp;

    public Receipt() {
    }

    public Receipt(Merchant merchant, String receiptText, String amount, String referenceNumber, String dateTimestamp) {
        this.id = null;
        this.merchant = merchant;
        this.receiptText = receiptText;
        this.amount = amount;
        this.referenceNumber = referenceNumber;
        this.dateTimestamp = dateTimestamp;
    }

    public static Receipt fromParseObject(ParseObject object) {
        // todo: write this
        return null;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public String getReceiptText() {
        return receiptText;
    }

    public void setReceiptText(String receiptText) {
        this.receiptText = receiptText;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getDateTimestamp() {
        return dateTimestamp;
    }

    public void setDateTimestamp(String dateTimestamp) {
        this.dateTimestamp = dateTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return "Merchant name: " + merchant.getName() + "\n" + "Amount: " + amount + "\n" + "Ref: "
                + referenceNumber + "\n" + "DateTime: " + dateTimestamp
                + "\n" + "Receipt body: " + receiptText;
    }
}
