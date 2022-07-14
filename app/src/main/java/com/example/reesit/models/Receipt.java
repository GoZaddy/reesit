package com.example.reesit.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.reesit.utils.CurrencyUtils;
import com.example.reesit.utils.DateTimeUtils;
import com.parse.ParseException;
import com.parse.ParseObject;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


@Parcel
public class Receipt {
    private String id;
    private String userID;
    private Merchant merchant;
    private String receiptImage;
    private String receiptText;
    private Integer amount;
    private String referenceNumber;
    private String dateTimestamp;
    private ParseObject parseObject;
    private List<Tag> tags;

    public static final String KEY_USER = "user";
    public static final String KEY_MERCHANT = "merchant";
    public static final String KEY_RECEIPT_IMAGE = "receiptImage";
    public static final String KEY_RECEIPT_TEXT = "receiptText";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_REFERENCE_NUMBER = "referenceNumber";
    public static final String KEY_DATE_TIME_STAMP = "dateTimestamp";
    public static final String KEY_TAGS = "tags";
    public static final String PARSE_CLASS_NAME = "Receipt";
    private static final String TAG = "Receipt";


    public Receipt() {
    }

    public Receipt(Merchant merchant, String receiptText, Integer amount, String referenceNumber, String dateTimestamp) {
        this.id = null;
        this.merchant = merchant;
        this.receiptText = receiptText;
        this.amount = amount;
        this.referenceNumber = referenceNumber;
        this.dateTimestamp = dateTimestamp;
    }

    public static Receipt fromParseObject(ParseObject object) {
        Receipt receipt = new Receipt(
                Merchant.fromParseObject(Objects.requireNonNull(object.getParseObject(KEY_MERCHANT))),
                object.getString(KEY_RECEIPT_TEXT).toLowerCase(Locale.ROOT),
                object.getInt(KEY_AMOUNT),
                object.getString(KEY_REFERENCE_NUMBER),
                object.getString(KEY_DATE_TIME_STAMP)
        );
        receipt.parseObject = object;
        receipt.userID = Objects.requireNonNull(object.getParseUser(KEY_USER)).getObjectId();
        receipt.id = object.getObjectId();
        receipt.receiptImage = Objects.requireNonNull(object.getParseFile(KEY_RECEIPT_IMAGE)).getUrl();
        List<ParseObject> tagsParseObject = object.getList(KEY_TAGS);
        if (tagsParseObject != null){
            for(ParseObject tagParseObject: tagsParseObject){
                try {
                    if(receipt.tags == null){
                        receipt.tags = new ArrayList<>();
                    }
                    receipt.tags.add(Tag.fromParseObject(tagParseObject.fetchIfNeeded()));
                } catch(ParseException e){
                    Log.e(TAG, "ParseException throw while fetching receipt tags", e);
                }
            }
        }

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

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
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
        return merchant.getName().toLowerCase(Locale.ROOT) + " " + CurrencyUtils.integerToCurrency(amount) + " "
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

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public ParseObject getParseObject(){
        return this.parseObject;
    }
}
