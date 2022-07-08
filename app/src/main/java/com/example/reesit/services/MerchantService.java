package com.example.reesit.services;

import com.example.reesit.models.Merchant;
import com.example.reesit.utils.GetMerchantsCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MerchantService {

    public static void getMerchant(String merchantName, GetMerchantsCallback callback){
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Merchant.PARSE_CLASS_NAME);
        query.whereEqualTo(Merchant.KEY_SLUG, Merchant.getSlugFromString(merchantName));
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    List<Merchant> merchant = new ArrayList<>();
                    if (objects.size() > 0){
                        merchant.add(Merchant.fromParseObject(objects.get(0)));
                    }
                    callback.done(merchant, null);
                } else {
                    callback.done(null, e);
                }
            }
        });
    }
    public static void addMerchant(Merchant merchant, SaveCallback callback){
        // merchants should be unique
        if (merchant.getId() == null){
            // check that a merchant with the same slug doesn't exist
            getMerchant(merchant.getName(), new GetMerchantsCallback() {
                @Override
                public void done(List<Merchant> merchants, ParseException e) {
                    if (e == null){
                        if (merchants.size() == 0){
                            ParseObject object = new ParseObject(Merchant.PARSE_CLASS_NAME);
                            object.put(Merchant.KEY_NAME, merchant.getName());
                            object.saveInBackground(callback);
                        }
                        callback.done(null);
                    } else {
                        callback.done(new ParseException(new Exception("Error checking if merchant with slug already exists",e)));
                    }
                }
            });
        }
    }

    public static void getSuggestedMerchants(String merchantName, GetMerchantsCallback callback){
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Merchant.PARSE_CLASS_NAME);
        query.whereFullText(Merchant.KEY_SLUG, Merchant.getSlugFromString(merchantName));
        List<Merchant> merchants = new ArrayList<>();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    for(ParseObject object: objects){
                        merchants.add(Merchant.fromParseObject(object));
                    }
                    callback.done(merchants, null);

                } else {
                    callback.done(null, e);
                }
            }
        });
    }
}
