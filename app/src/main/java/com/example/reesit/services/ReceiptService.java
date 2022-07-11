package com.example.reesit.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.reesit.misc.Filter;
import com.example.reesit.misc.ReceiptWithImage;
import com.example.reesit.models.Merchant;
import com.example.reesit.models.Receipt;
import com.example.reesit.models.User;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReceiptService {
    private static final Integer GET_PAGE_LIMIT = 20;

    public interface AddReceiptCallback {
        public void done(Receipt receipt, Exception e);
    }

    public interface GetReceiptsCallback {
        public void done(List<Receipt> receipts, Boolean isLastPage, Exception e);
    }


    public static void getAllReceipts(User user, @NonNull Integer skip, GetReceiptsCallback callback){
        // use this to check if there are more pages to be fetched
        ParseQuery<ParseObject> getCountQuery = new ParseQuery<ParseObject>(Receipt.PARSE_CLASS_NAME);
        getCountQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException countException) {
                if (countException == null){
                    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Receipt.PARSE_CLASS_NAME);
                    query.setLimit(GET_PAGE_LIMIT);
                    query.setSkip(skip);

                    query.include(Receipt.KEY_MERCHANT);
                    query.addDescendingOrder("dateTimestamp");
                    query.whereEqualTo("user", user.getParseUser());

                    List<Receipt> receipts = new ArrayList<>();

                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException findException) {
                            if (findException == null){
                                for(ParseObject object: objects){
                                    receipts.add(Receipt.fromParseObject(object));
                                }
                                boolean isLastPage = skip+receipts.size() >= count;
                                callback.done(receipts, isLastPage ,null);
                            } else {
                                callback.done(null, null, findException);
                            }
                        }
                    });
                } else {
                    callback.done(null, null, new ParseException(new Exception("Error getting count of receipts", countException)));
                }

            }
        });
    }

    // second parameter should be a filter object or something
    public static void getReceiptsWithFilter(Filter filter, User user, @NonNull Integer skip, GetReceiptsCallback callback){
        ParseQuery<ParseObject> query;
        try{
            query = filter.getParseQuery();
        } catch (Filter.FilterGenerateQueryException e){
            Log.e("ReceiptService","error caught while getting query", e);
            callback.done(null, null, e);
            return;
        }

        query.whereEqualTo("user", user.getParseUser());
        query.addDescendingOrder("dateTimestamp");
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException countException) {
                if (countException == null){
                    query.setLimit(GET_PAGE_LIMIT);
                    query.setSkip(skip);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException findException) {
                            if (findException == null){
                                List<Receipt> receipts = new ArrayList<>();
                                for(ParseObject object: objects){
                                    receipts.add(Receipt.fromParseObject(object));
                                }
                                boolean isLastPage = skip+receipts.size() >= count;
                                callback.done(receipts, isLastPage ,null);
                            } else {
                                callback.done(null, null, findException);
                            }
                        }
                    });
                } else {
                    callback.done(null, null, countException);
                }
            }
        });
    }

    public static void addReceipt(Receipt receipt, File receiptImage, AddReceiptCallback callback){
        ParseObject receiptObj = new ParseObject(Receipt.PARSE_CLASS_NAME);
        receiptObj.put(Receipt.KEY_USER, ParseUser.getCurrentUser());
        receiptObj.put(Receipt.KEY_DATE_TIME_STAMP, receipt.getDateTimestamp());
        receiptObj.put(Receipt.KEY_REFERENCE_NUMBER, receipt.getReferenceNumber());
        receiptObj.put(Receipt.KEY_AMOUNT, receipt.getAmount());
        receiptObj.put(Receipt.KEY_RECEIPT_IMAGE, new ParseFile(receiptImage));

        MerchantService.addMerchant(receipt.getMerchant(), new MerchantService.AddMerchantCallback() {
            @Override
            public void done(Merchant newMerchant, Exception e) {
                if (e == null){
                    receiptObj.put(Receipt.KEY_MERCHANT, newMerchant.getParseObject());

                    // adding the parsed information from the receipt to the receipt text to improve search queries
                    receiptObj.put(Receipt.KEY_RECEIPT_TEXT, receipt.toString() + " " + receipt.getReceiptText());
                    receiptObj.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null){
                                callback.done(Receipt.fromParseObject(receiptObj), null);
                            } else {
                                callback.done(null, e);
                            }
                        }
                    });
                } else {
                    callback.done(null, e);
                }
            }
        });
    }



    public static void deleteReceipt(String receiptID){

    }

    // only fields that should be updated should be provided, every other field should be null
    public static void updateReceipt(ReceiptWithImage receiptWithImage){

    }

}
