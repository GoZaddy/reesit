package com.example.reesit.models;

import androidx.annotation.NonNull;

import com.example.reesit.utils.DateTimeUtils;
import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.parceler.Parcel;

import java.util.Date;
import java.util.Locale;


@Parcel
public class Receipt {
    private String id;
    private String userID;
    private Merchant merchant;
    private String receiptImage;
    private String receiptText;
    private String amount;
    private String referenceNumber;
    private String dateTimestamp;
    private ParseObject parseObject;

    public static final String KEY_USER = "user";
    public static final String KEY_MERCHANT = "merchant";
    public static final String KEY_RECEIPT_IMAGE = "receiptImage";
    public static final String KEY_RECEIPT_TEXT = "receiptText";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_REFERENCE_NUMBER = "referenceNumber";
    public static final String KEY_DATE_TIME_STAMP = "dateTimestamp";
    public static final String PARSE_CLASS_NAME = "Receipt";


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
        Receipt receipt = new Receipt(
                Merchant.fromParseObject(object.getParseObject(KEY_MERCHANT)),
                object.getString(KEY_RECEIPT_TEXT).toLowerCase(Locale.ROOT),
                object.getString(KEY_AMOUNT),
                object.getString(KEY_REFERENCE_NUMBER),
                object.getString(KEY_DATE_TIME_STAMP)
        );
        receipt.parseObject = object;
        receipt.userID = object.getParseUser(KEY_USER).getObjectId();
        receipt.id = object.getObjectId();
        receipt.receiptImage = object.getParseFile(KEY_RECEIPT_IMAGE).getUrl();
        return receipt;
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
        this.receiptText = receiptText.toLowerCase(Locale.ROOT);
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
        return merchant.getName().toLowerCase(Locale.ROOT) + " " + amount.toLowerCase(Locale.ROOT) + " "
                + referenceNumber.toLowerCase(Locale.ROOT) + " " + DateTimeUtils.getDateAndTimeReceiptCard(dateTimestamp).toLowerCase(Locale.ROOT);
    }

    public String getReceiptImage(){
        return this.receiptImage;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setReceiptImage(String receiptImage) {
        this.receiptImage = receiptImage;
    }

    public ParseObject getParseObject(){
        return this.parseObject;
    }
}
