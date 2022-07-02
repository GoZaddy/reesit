package com.example.reesit.services;

import com.example.reesit.misc.Debouncer;
import com.example.reesit.misc.ReceiptWithImage;
import com.example.reesit.models.Merchant;
import com.example.reesit.models.Receipt;
import com.example.reesit.models.User;
import com.example.reesit.utils.GetMerchantsCallback;
import com.example.reesit.utils.GetReceiptsCallback;
import com.parse.FindCallback;
import com.parse.Parse;
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
    private static final Integer PAGE_LIMIT = 20;
    public static void getAllReceipts(User user, Integer skip, GetReceiptsCallback callback){
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Receipt.PARSE_CLASS_NAME);
        query.setLimit(20);
        if(skip != null){
            query.setSkip(skip);
        }

        query.include(Receipt.KEY_MERCHANT);
        query.addDescendingOrder("dateTimestamp");
        query.whereEqualTo("user", user.getParseUser());

        List<Receipt> receipts = new ArrayList<>();

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    for(ParseObject object: objects){
                        receipts.add(Receipt.fromParseObject(object));
                    }
                    callback.done(receipts, null);
                } else {
                    callback.done(null, e);
                }
            }
        });
    }

    // second parameter should be a filter object or something
    public static void getReceiptsWithFilter(String userID, GetReceiptsCallback callback){
    }

    public static void addReceipt(Receipt receipt, File receiptImage, SaveCallback callback){
        ParseObject receiptObj = new ParseObject(Receipt.PARSE_CLASS_NAME);
        receiptObj.put(Receipt.KEY_RECEIPT_TEXT, receipt.getReceiptText());
        receiptObj.put(Receipt.KEY_USER, ParseUser.getCurrentUser());
        receiptObj.put(Receipt.KEY_DATE_TIME_STAMP, receipt.getDateTimestamp());
        receiptObj.put(Receipt.KEY_REFERENCE_NUMBER, receipt.getReferenceNumber());
        receiptObj.put(Receipt.KEY_AMOUNT, receipt.getAmount());
        receiptObj.put(Receipt.KEY_RECEIPT_IMAGE, new ParseFile(receiptImage));

        // if merchant does not exist in database
        if (receipt.getMerchant().getId() == null){
            MerchantService.addMerchant(receipt.getMerchant(), new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null){
                        MerchantService.getMerchant(receipt.getMerchant().getName(), new GetMerchantsCallback() {
                            @Override
                            public void done(List<Merchant> merchants, ParseException e) {
                                if (e == null){
                                    receiptObj.put(Receipt.KEY_MERCHANT, merchants.get(0).getParseObject());
                                    receiptObj.saveInBackground(callback);
                                }
                            }
                        });
                    } else {
                        callback.done(e);
                    }
                }
            });
        }
    }



    public static void deleteReceipt(String receiptID){

    }

    // only fields that should be updated should be provided, every other field should be null
    public static void updateReceipt(ReceiptWithImage receiptWithImage){

    }

}
