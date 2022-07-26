package com.faruq.reesit.services;


import com.faruq.reesit.misc.Filter;
import com.faruq.reesit.models.Merchant;
import com.faruq.reesit.models.ReceiptCategory;
import com.faruq.reesit.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class CategoryService {
    private static final String REIMBURSEMENT_CATEGORY_NAME = "Reimbursements";

    public interface AddCategoryCallback {
        void done(ReceiptCategory newCategory, Exception e);
    }

    public interface GetCategoriesCallback {
        void done(List<ReceiptCategory> categories, Exception e);
    }

    public static class CategoryAlreadyExistsException extends Exception{
        public CategoryAlreadyExistsException(String message){ super(message); }
    }



    /** Gets a receipt category. This method is for internal use only as specific database objects
     * should only be fetched using their ID or as a group.
     * This is used by internal methods to ensure that receipt categories are unique by name
     * @param categoryName Receipt category name
     * @param callback Callback function
     */
    public static void getCategoryByName(String categoryName, User user, GetCategoriesCallback callback){
        ParseQuery<ParseObject> query = new ParseQuery<>(ReceiptCategory.PARSE_CLASS_NAME);
        query.whereEqualTo(ReceiptCategory.KEY_SLUG, ReceiptCategory.getSlugFromName(categoryName));
        query.whereEqualTo(ReceiptCategory.KEY_USER, user.getParseUser());
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    List<ReceiptCategory> receiptCategories = new ArrayList<>();
                    if (objects.size() > 0){
                        receiptCategories.add(ReceiptCategory.fromParseObject(objects.get(0)));
                    }
                    callback.done(receiptCategories, null);
                } else {
                    callback.done(null, e);
                }
            }
        });
    }

    public static void getCategories(User user, GetCategoriesCallback callback){
        ParseQuery<ParseObject> query = new ParseQuery<>(ReceiptCategory.PARSE_CLASS_NAME);
        query.whereEqualTo(ReceiptCategory.KEY_USER, user.getParseUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    List<ReceiptCategory> categories = new ArrayList<>();
                    for(ParseObject object: objects){
                        categories.add(ReceiptCategory.fromParseObject(object));
                    }
                    callback.done(categories, null);
                } else {
                    callback.done(null, e);
                }

            }
        });
    }

    public static void createCategory(ReceiptCategory category, User user, AddCategoryCallback callback){
        if (category.getId() == null){
            // ensure that each category has a unique name
            // check that a merchant with the same slug doesn't exist
            getCategoryByName(category.getName(), user, new GetCategoriesCallback() {
                @Override
                public void done(List<ReceiptCategory> categories, Exception e) {
                    if (e == null){
                        if (categories.size() == 0){
                            ParseObject object = new ParseObject(ReceiptCategory.PARSE_CLASS_NAME);
                            object.put(ReceiptCategory.KEY_NAME, category.getName());
                            object.put(ReceiptCategory.KEY_SLUG, category.getSlug());
                            object.put(ReceiptCategory.KEY_USER, user.getParseUser());

                            // add filter object
                            Filter filter = category.getFilter();
                            if (filter.getSearchQuery() != null){
                                object.put(ReceiptCategory.KEY_FILTER_SEARCH_QUERY, filter.getSearchQuery());
                            }

                            if (filter.getGreaterThanAmount() != null){
                                object.put(ReceiptCategory.KEY_FILTER_GREATER_THAN, filter.getGreaterThanAmount());
                            }

                            if (filter.getLessThanAmount() != null){
                                object.put(ReceiptCategory.KEY_FILTER_LESS_THAN, filter.getLessThanAmount());
                            }

                            if (filter.getTag() != null){
                                object.put(ReceiptCategory.KEY_FILTER_TAG, filter.getTag().getParseObject());
                            }

                            if (filter.getBeforeDateTimestamp() != null){
                                object.put(ReceiptCategory.KEY_FILTER_BEFORE_DATE_TIMESTAMP, filter.getBeforeDateTimestamp());
                            }

                            if (filter.getAfterDateTimestamp() != null){
                                object.put(ReceiptCategory.KEY_FILTER_AFTER_DATE_TIMESTAMP, filter.getAfterDateTimestamp());
                            }

                            if (filter.isReimbursement() != null){
                                object.put(ReceiptCategory.KEY_IS_REIMBURSEMENT, filter.isReimbursement());

                                if (filter.isReimbursement()){
                                    if (filter.getReimbursementState() != null){
                                        object.put(ReceiptCategory.KEY_REIMBURSEMENT_STATE, filter.getReimbursementState().name());
                                    }
                                }
                            }


                            List<ParseObject> merchantsParseObjects = new ArrayList<>();
                            if (filter.getMerchants() != null){
                                for(Merchant merchant: filter.getMerchants()){
                                    merchantsParseObjects.add(merchant.getParseObject());
                                }
                            }
                            object.put(ReceiptCategory.KEY_FILTER_MERCHANTS, merchantsParseObjects);
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null){
                                        callback.done(ReceiptCategory.fromParseObject(object), null);
                                    } else {
                                        callback.done(null, e);
                                    }

                                }
                            });
                        } else {
                            callback.done(null, new CategoryAlreadyExistsException(category.getName() + " category already exists"));
                        }
                    } else {
                        callback.done(null, new ParseException(new Exception("Error checking if category with slug already exists",e)));
                    }
                }
            });

        } else {
            callback.done(null, new CategoryAlreadyExistsException(category.getName() + " category already exists"));
        }
    }

    public static void createReimbursementCategoryForUser(User user, AddCategoryCallback callback){
        ReceiptCategory category = new ReceiptCategory(REIMBURSEMENT_CATEGORY_NAME, Filter.getReimbursementFilter());
        ParseObject object = new ParseObject(ReceiptCategory.PARSE_CLASS_NAME);
        object.put(ReceiptCategory.KEY_NAME, category.getName());
        object.put(ReceiptCategory.KEY_SLUG, category.getSlug());
        object.put(ReceiptCategory.KEY_USER, user.getParseUser());
        object.put(ReceiptCategory.KEY_IS_REIMBURSEMENT, category.getFilter().isReimbursement());
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    callback.done(ReceiptCategory.fromParseObject(object), null);
                } else {
                    callback.done(null, e);
                }
            }
        });
    }

    public static void deleteCategory(User user, ReceiptCategory category){}

    public static void updateCategory(User user, ReceiptCategory category, AddCategoryCallback callback){}
}
