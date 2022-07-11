package com.example.reesit.services;

import com.example.reesit.models.Merchant;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class MerchantService {

    public interface AddMerchantCallback {
        public void done(Merchant newMerchant, Exception e);
    }

    public interface GetMerchantsCallback {
        public void done(List<Merchant> merchants, Exception e);
    }

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


    /**
     * Adds a merchant to the database and returns the added merchant through the callback.
     * If the merchant already exists, pass it to the callback.
     * @param merchant Merchant to be added to DB
     * @param callback Callback function that is called after adding merchant to database
     */
    public static void addMerchant(Merchant merchant, AddMerchantCallback callback){
        // merchants should be unique
        if (merchant.getId() == null){
            // check that a merchant with the same slug doesn't exist
            getMerchant(merchant.name, new GetMerchantsCallback() {
                @Override
                public void done(List<Merchant> merchants, Exception e) {
                    if (e == null){
                        if (merchants.size() == 0){
                            ParseObject object = new ParseObject(Merchant.PARSE_CLASS_NAME);
                            object.put(Merchant.KEY_NAME, merchant.getName());
                            object.put(Merchant.KEY_SLUG, merchant.getSlug());
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null){
                                        callback.done(Merchant.fromParseObject(object), null);
                                    } else {
                                        callback.done(null, e);
                                    }

                                }
                            });
                        } else {
                            callback.done(merchants.get(0), null);
                        }
                    } else {
                        callback.done(null, new ParseException(new Exception("Error checking if merchant with slug already exists",e)));
                    }
                }
            });
        } else {
            callback.done(merchant, null);
        }
    }

    public static void getSuggestedMerchants(String merchantName, GetMerchantsCallback callback){
        String generatedSlug = Merchant.getSlugFromString(merchantName);
        if (generatedSlug.length() != 0){
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Merchant.PARSE_CLASS_NAME);
            query.whereContains(Merchant.KEY_SLUG, generatedSlug);
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
        } else {
            callback.done(new ArrayList<>(), null);
        }
    }
}
