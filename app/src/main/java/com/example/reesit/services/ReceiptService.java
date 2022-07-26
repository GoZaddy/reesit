package com.example.reesit.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.reesit.misc.Filter;
import com.example.reesit.misc.SortReceiptOption;
import com.example.reesit.models.Merchant;
import com.example.reesit.models.Receipt;
import com.example.reesit.models.Tag;
import com.example.reesit.models.User;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiptService {
    private static final Integer GET_PAGE_LIMIT = 20;


    public static class ReceiptServiceException extends Exception{
        public ReceiptServiceException(String message, Throwable e){ super(message, e); }
    }

    public interface AddReceiptCallback {
        public void done(Receipt receipt, Exception e);
    }

    public interface GetReceiptsCallback {
        public void done(List<Receipt> receipts, Boolean isLastPage, Exception e);
    }

    public interface DeleteReceiptCallback {
        public void done(Exception e);
    }

    public interface UpdateReceiptCallback{
        public void done(Receipt newReceipt, Exception e);
    }

    /**
     * @param query ParseQuery object to use to execute the query. All fields must be set before passing to this metho
     * @param skip Number of db rows to skip
     * @param callback Callback function to be called after getting results
     */
    private static void getReceiptsWithPaginationHelper(@NonNull ParseQuery<ParseObject> query, @NonNull Integer skip, @NonNull GetReceiptsCallback callback){
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

    private static void applySortPropertiesToQuery(ParseQuery<ParseObject> query, SortReceiptOption sortReceiptOption) throws ReceiptServiceException {
        String keyToSortBy = sortReceiptOption.getKeyToSort();
        SortReceiptOption.SortOrder order = sortReceiptOption.getSortOrder();
        if (keyToSortBy == null){
            if (order == null){
                query.addDescendingOrder(Receipt.KEY_DATE_TIME_STAMP);
            } else {
                if (order == SortReceiptOption.SortOrder.ASCENDING){
                    query.addAscendingOrder(Receipt.KEY_DATE_TIME_STAMP);
                } else {
                    query.addDescendingOrder(Receipt.KEY_DATE_TIME_STAMP);
                }
            }
        } else if (order != null){
            switch (keyToSortBy) {
                case Receipt.KEY_AMOUNT:
                    if (order == SortReceiptOption.SortOrder.ASCENDING) {
                        query.addAscendingOrder(Receipt.KEY_AMOUNT);
                    } else {
                        query.addDescendingOrder(Receipt.KEY_AMOUNT);
                    }
                    break;
                case Receipt.KEY_MERCHANT:
                    if (order == SortReceiptOption.SortOrder.ASCENDING) {
                        query.addAscendingOrder(Receipt.KEY_MERCHANT);
                    } else {
                        query.addDescendingOrder(Receipt.KEY_MERCHANT);
                    }
                    break;
                case Receipt.KEY_DATE_TIME_STAMP:
                    if (order == SortReceiptOption.SortOrder.ASCENDING) {
                        query.addAscendingOrder(Receipt.KEY_DATE_TIME_STAMP);
                    } else {
                        query.addDescendingOrder(Receipt.KEY_DATE_TIME_STAMP);
                    }
                    break;
                default:
                    throw new ReceiptServiceException("keyToSortBy value: " + keyToSortBy + ", is invalid", null);
            }
        } else{
            throw new ReceiptServiceException("keyToSortBy is not null but order is null", null);
        }


    }


    public static void getAllReceipts(User user, @NonNull Integer skip, SortReceiptOption sortReceiptOption, GetReceiptsCallback callback){
        // use this to check if there are more pages to be fetched

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Receipt.PARSE_CLASS_NAME);
        query.whereEqualTo(Receipt.KEY_USER, user.getParseUser());
        query.include(Receipt.KEY_MERCHANT);
        // add query parameters for sorting
        try{
            applySortPropertiesToQuery(query, sortReceiptOption);

        } catch(ReceiptServiceException e){
            callback.done(null, null, e);
        }
        getReceiptsWithPaginationHelper(query, skip, callback);
    }

    // second parameter should be a filter object or something
    public static void getReceiptsWithFilter(Filter filter, User user, @NonNull Integer skip, SortReceiptOption sortReceiptOption, GetReceiptsCallback callback){
        ParseQuery<ParseObject> query;
        try{
            query = filter.getParseQuery();
        } catch (Filter.FilterGenerateQueryException e){
            Log.e("ReceiptService","error caught while getting query", e);
            callback.done(null, null, e);
            return;
        }

        query.whereEqualTo(Receipt.KEY_USER, user.getParseUser());
        // add query parameters for sorting
        try{
            applySortPropertiesToQuery(query, sortReceiptOption);
        } catch(ReceiptServiceException e){
            callback.done(null, null, e);
        }
        getReceiptsWithPaginationHelper(query, skip, callback);
    }


    public static void addReceipt(Receipt receipt, File receiptImage, User user, AddReceiptCallback callback){
        ParseObject receiptObj = new ParseObject(Receipt.PARSE_CLASS_NAME);
        receiptObj.put(Receipt.KEY_USER, user.getParseUser());
        receiptObj.put(Receipt.KEY_DATE_TIME_STAMP, receipt.getDateTimestamp());
        receiptObj.put(Receipt.KEY_REFERENCE_NUMBER, receipt.getReferenceNumber());
        receiptObj.put(Receipt.KEY_AMOUNT, receipt.getAmount());
        receiptObj.put(Receipt.KEY_RECEIPT_IMAGE, new ParseFile(receiptImage));
        List<ParseObject> tagsParseObjects = new ArrayList<>();
        if (receipt.getTags() != null){
            for(Tag tag: receipt.getTags()){
                // if the tag has no id - it means it hasn't been stored in the database yet
                if (tag.getId() == null){
                    TagService.addTag(tag, User.getCurrentUser(), new TagService.AddTagCallback() {
                        @Override
                        public void done(Tag newTag, Exception e) {
                            if (e == null){
                                tagsParseObjects.add(newTag.getParseObject());
                            } else {
                                callback.done(null, e);
                            }
                        }
                    });
                } else {
                    tagsParseObjects.add(tag.getParseObject());
                }

            }
        }
        receiptObj.put(Receipt.KEY_TAGS, tagsParseObjects);

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



    public static void deleteReceipt(String receiptID, User user, DeleteReceiptCallback callback){
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Receipt.PARSE_CLASS_NAME);
        query.whereEqualTo(Receipt.KEY_USER, user.getParseUser());
        query.whereEqualTo("objectId", receiptID);
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    if (objects.size() == 1){
                        objects.get(0).deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                callback.done(e);
                            }
                        });
                    }
                } else {
                    callback.done(e);
                }
            }
        });
    }


    public static void updateReceipt(Receipt receipt, User user, UpdateReceiptCallback callback){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Receipt.PARSE_CLASS_NAME);
                query.whereEqualTo(Receipt.KEY_USER, user.getParseUser());
                query.whereEqualTo("objectId", receipt.getId());
                query.setLimit(1);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null){
                            if (objects.size() == 1){
                                ParseObject parseObject = objects.get(0);
                                MerchantService.addMerchant(receipt.getMerchant(), new MerchantService.AddMerchantCallback() {
                                    @Override
                                    public void done(Merchant newMerchant, Exception e) {
                                        if (e == null){
                                            parseObject.put(Receipt.KEY_MERCHANT, newMerchant.getParseObject());
                                            parseObject.put(Receipt.KEY_AMOUNT, receipt.getAmount());
                                            parseObject.put(Receipt.KEY_REFERENCE_NUMBER, receipt.getReferenceNumber());
                                            parseObject.put(Receipt.KEY_DATE_TIME_STAMP, receipt.getDateTimestamp());
                                            if (receipt.getTags() != null){
                                                List<ParseObject> tagsParseObjects = new ArrayList<>();
                                                for(Tag tag: receipt.getTags()){
                                                    // if the tag has no id - it means it hasn't been stored in the database yet
                                                    if (tag.getId() == null){
                                                        // todo: tags are being added to the after the function has finished calling
                                                        try {
                                                            Tag newTag = TagService.addTag(tag, User.getCurrentUser());
                                                            tagsParseObjects.add(newTag.getParseObject());
                                                        } catch (ParseException parseException){
                                                            callback.done(null, parseException);
                                                        }
                                                    } else {
                                                        tagsParseObjects.add(tag.getParseObject());
                                                    }

                                                }
                                                parseObject.put(Receipt.KEY_TAGS, tagsParseObjects);
                                            } else {
                                                parseObject.put(Receipt.KEY_TAGS, new ArrayList<>());
                                            }
                                            parseObject.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null){
                                                        callback.done(Receipt.fromParseObject(parseObject), null);
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
                        } else {
                            callback.done(null, e);
                        }
                    }
                });

                executorService.shutdown();
            }
        });

    }

}
