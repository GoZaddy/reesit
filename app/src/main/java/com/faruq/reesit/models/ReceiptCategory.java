package com.faruq.reesit.models;

import android.util.Log;

import com.faruq.reesit.misc.Filter;
import com.parse.ParseException;
import com.parse.ParseObject;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Parcel
public class ReceiptCategory {
    private String id;
    private String name;
    private Filter filter;
    private User user;
    private ParseObject parseObject;
    private String slug;


    public static final String KEY_FILTER_SEARCH_QUERY = "searchQuery";
    public static final String KEY_FILTER_GREATER_THAN = "greaterThan";
    public static final String KEY_FILTER_LESS_THAN = "lessThan";
    public static final String KEY_FILTER_TAG = "tag";
    public static final String KEY_FILTER_MERCHANTS = "merchants";
    public static final String KEY_FILTER_BEFORE_DATE_TIMESTAMP = "beforeDateTimestamp";
    public static final String KEY_FILTER_AFTER_DATE_TIMESTAMP = "afterDateTimestamp";
    public static final String KEY_USER = "user";
    public static final String KEY_NAME = "name";
    public static final String KEY_SLUG = "slug";
    public static final String KEY_IS_REIMBURSEMENT = "isReimbursement";
    public static final String KEY_REIMBURSEMENT_STATE = "reimbursementState";
    public static final String PARSE_CLASS_NAME = "Category";

    private static final String TAG = "ReceiptCategory";

    public ReceiptCategory(){}

    public ReceiptCategory(String name, Filter filter){
        this.name = name.trim();
        this.slug = getSlugFromName(name);
        this.filter = filter;
        this.parseObject = null;
    }

    public static ReceiptCategory fromParseObject(ParseObject parseObject){
        ReceiptCategory receiptCategory = new ReceiptCategory();
        receiptCategory.parseObject = parseObject;

        receiptCategory.id = parseObject.getObjectId();
        receiptCategory.user = User.fromParseUser(parseObject.getParseUser(KEY_USER));
        receiptCategory.name = parseObject.getString(KEY_NAME);
        receiptCategory.slug = parseObject.getString(KEY_SLUG);

        Filter filter = new Filter();
        filter.setSearchQuery(parseObject.getString(KEY_FILTER_SEARCH_QUERY));
        filter.setAfterDateTimestamp(parseObject.getString(KEY_FILTER_AFTER_DATE_TIMESTAMP));
        filter.setBeforeDateTimestamp(parseObject.getString(KEY_FILTER_BEFORE_DATE_TIMESTAMP));
        filter.setReimbursement(parseObject.getBoolean(KEY_IS_REIMBURSEMENT));
        if (filter.isReimbursement()){
           filter.setReimbursementState(Receipt.ReimbursementState.fromString(parseObject.getString(KEY_REIMBURSEMENT_STATE)));
        } else {
            filter.setReimbursementState(null);
        }
        if (parseObject.getInt(KEY_FILTER_GREATER_THAN) != 0){
            filter.setGreaterThanAmount(parseObject.getInt(KEY_FILTER_GREATER_THAN));
        }

        if (parseObject.getInt(KEY_FILTER_LESS_THAN) != 0){
            filter.setLessThanAmount(parseObject.getInt(KEY_FILTER_LESS_THAN));
        }

        if (parseObject.getParseObject(KEY_FILTER_TAG) != null){
            try {
                filter.setTag(Tag.fromParseObject(Objects.requireNonNull(parseObject.getParseObject(KEY_FILTER_TAG)).fetchIfNeeded()));
            } catch(ParseException e){
                Log.e(TAG, "ParseException thrown while fetching filter tag", e);
            }


        }

        List<ParseObject> merchantsParseList = parseObject.getList(KEY_FILTER_MERCHANTS);
        if (merchantsParseList != null){
            for(ParseObject merchantParseObject: merchantsParseList){
                try {
                    if(filter.getMerchants() == null){
                        filter.setMerchants(new ArrayList<>());
                    }
                    filter.getMerchants().add(Merchant.fromParseObject(merchantParseObject.fetchIfNeeded()));
                } catch(ParseException e){
                    Log.e(TAG, "ParseException thrown while fetching filter merchants", e);
                }
            }
        }

        receiptCategory.filter = filter;

        return receiptCategory;
    }

    public ParseObject getParseObject(){
        return parseObject;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Filter getFilter() {
        return filter;
    }

    public User getUser() {
        return user;
    }

    public String getSlug(){
        return slug;
    }

    public static String getSlugFromName(String categoryName){
        return categoryName.toLowerCase(Locale.ROOT);
    }
}
