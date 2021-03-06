package com.faruq.reesit.misc;

import android.content.Context;

import com.faruq.reesit.R;
import com.faruq.reesit.models.Merchant;
import com.faruq.reesit.models.Receipt;
import com.faruq.reesit.models.Tag;
import com.faruq.reesit.utils.CurrencyUtils;
import com.faruq.reesit.utils.DateTimeUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Parcel
public class Filter {
    private String searchQuery;
    private Integer greaterThanAmount;
    private Integer lessThanAmount;
    private Tag tag;
    private List<Merchant> merchants;
    private String beforeDateTimestamp;
    private String afterDateTimestamp;
    private Boolean isReimbursement;
    private Receipt.ReimbursementState reimbursementState;

    public static class FilterGenerateQueryException extends Exception{
        public FilterGenerateQueryException(String message){
            super(message);
        }
        public FilterGenerateQueryException(Throwable e){
            super(e);
        }
        public FilterGenerateQueryException(String message, Throwable err){
            super(message, err);
        }
    }

    public static class FilterValidationException extends Exception{
        public FilterValidationException(String message){
            super(message);
        }
    }

    public Filter(){}

    public static Filter getReimbursementFilter(){
        Filter filter = new Filter();
        filter.reset();
        filter.isReimbursement = true;
        return filter;
    }

    public ParseQuery<ParseObject> getParseQuery() throws FilterGenerateQueryException {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Receipt.PARSE_CLASS_NAME);
        query.include(Receipt.KEY_MERCHANT);
        if (searchQuery != null){
            query.whereContains(Receipt.KEY_RECEIPT_TEXT, searchQuery.toLowerCase(Locale.ROOT));
        }

        if (greaterThanAmount != null && lessThanAmount != null){
            if (greaterThanAmount > lessThanAmount){
                throw new FilterGenerateQueryException(new FilterValidationException("greaterThanAmount value cannot be greater than lessThanAmount value"));
            }
            query.whereGreaterThanOrEqualTo(Receipt.KEY_AMOUNT, greaterThanAmount);
            query.whereLessThanOrEqualTo(Receipt.KEY_AMOUNT, lessThanAmount);
        } else if (greaterThanAmount != null){
            query.whereGreaterThanOrEqualTo(Receipt.KEY_AMOUNT, greaterThanAmount);
        } else if (lessThanAmount != null){
            query.whereLessThanOrEqualTo(Receipt.KEY_AMOUNT, lessThanAmount);
        }

        // add tag support
        if (tag != null){
            List<ParseObject> queryHelperListOfTags = new ArrayList<>();
            queryHelperListOfTags.add(tag.getParseObject());
            query.whereContainsAll(Receipt.KEY_TAGS, queryHelperListOfTags);
        }


        if (merchants != null && merchants.size() != 0){
            List<ParseObject> merchantParseObjects = new ArrayList<>();
            for(Merchant merchant: merchants){
                if (merchant.getParseObject() != null){
                    merchantParseObjects.add(merchant.getParseObject());
                }
            }
            query.whereContainedIn(Receipt.KEY_MERCHANT, merchantParseObjects);
        }

        if (beforeDateTimestamp != null && afterDateTimestamp != null){
            if (Long.parseLong(afterDateTimestamp) > Long.parseLong(beforeDateTimestamp)){
                throw new FilterGenerateQueryException(new FilterValidationException("afterDateTimestamp cannot be greater than beforeDateTimestamp"));
            }
            query.whereGreaterThanOrEqualTo(Receipt.KEY_DATE_TIME_STAMP, afterDateTimestamp);
            query.whereLessThanOrEqualTo(Receipt.KEY_DATE_TIME_STAMP, beforeDateTimestamp);
        } else if (beforeDateTimestamp != null){
            query.whereLessThanOrEqualTo(Receipt.KEY_DATE_TIME_STAMP, beforeDateTimestamp);
        } else if (afterDateTimestamp != null){
            query.whereGreaterThanOrEqualTo(Receipt.KEY_DATE_TIME_STAMP, afterDateTimestamp);
        }

        if (isReimbursement != null){
            query.whereEqualTo(Receipt.KEY_IS_REIMBURSEMENT, isReimbursement);
            if (isReimbursement && reimbursementState != null){
                query.whereEqualTo(Receipt.KEY_REIMBURSEMENT_STATE, reimbursementState.name());
            }
        }


        return query;
    }

    public String getStringValue(Context context){
        String result = "";
        if (searchQuery != null){
            result += "Query: "+ "'" + searchQuery+ "'" + "\n";
        }
        String amountLine = null;
        if (greaterThanAmount != null && lessThanAmount != null){
            amountLine = context.getString(R.string.filter_tostring_amount_format, CurrencyUtils.integerToCurrency(greaterThanAmount), CurrencyUtils.integerToCurrency(lessThanAmount));
        } else if (greaterThanAmount != null){
            amountLine = context.getString(R.string.filter_tostring_amount_greater_format, CurrencyUtils.integerToCurrency(greaterThanAmount));
        } else if (lessThanAmount != null) {
            amountLine = context.getString(R.string.filter_tostring_amount_less_format, CurrencyUtils.integerToCurrency(lessThanAmount));
        }

        if (amountLine != null){
            result += amountLine + "\n";
        }

        if (tag != null){
            result += "Tag: "+tag.getName()+"\n";
        }

        if (merchants != null){
            if (merchants.size() > 0){
                StringBuilder merchantsLine = new StringBuilder();
                for(Merchant merchant: merchants){
                    merchantsLine.append(merchant.getName()).append(", ");
                }
                // remove trailing ", "
                merchantsLine.delete(merchantsLine.length()-2, merchantsLine.length());
                result += context.getString(R.string.filter_tostring_merchants_format, merchantsLine.toString()+"\n");
            }
        }

        String dateLine = null;
        if (beforeDateTimestamp != null && afterDateTimestamp != null){
            dateLine = context.getString(R.string.filter_tostring_date_format,
                    DateTimeUtils.getDateWithLongMonth(afterDateTimestamp),
                    DateTimeUtils.getDateWithLongMonth(beforeDateTimestamp));
        } else if (beforeDateTimestamp != null){
            dateLine = context.getString(R.string.filter_tostring_date_before_format,
                    DateTimeUtils.getDateWithLongMonth(beforeDateTimestamp));
        } else if (afterDateTimestamp != null){
            dateLine = context.getString(R.string.filter_tostring_date_after_format,
                    DateTimeUtils.getDateWithLongMonth(afterDateTimestamp));
        }

        if (dateLine != null){
            result += dateLine+"\n";
        }


        String reimbursementLine = null;
        if (isReimbursement != null){
            reimbursementLine = context.getString(R.string.filter_tostring_to_be_reimbursed_format, isReimbursement ? "True" : "False");
            if (isReimbursement && reimbursementState != null){
                reimbursementLine += "\n"+context.getString(R.string.filter_tostring_reimbursement_state_format, reimbursementState.getShorterTitle(context));
            }
        }

        if (reimbursementLine != null){
            result += reimbursementLine;
        }

        return result.trim();
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public Integer getGreaterThanAmount() {
        return greaterThanAmount;
    }

    public void setGreaterThanAmount(Integer greaterThanAmount) {
        this.greaterThanAmount = greaterThanAmount;
    }

    public Integer getLessThanAmount() {
        return lessThanAmount;
    }

    public void setLessThanAmount(Integer lessThanAmount) {
        this.lessThanAmount = lessThanAmount;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public List<Merchant> getMerchants() {
        return merchants;
    }

    public void setMerchants(List<Merchant> merchants) {
        this.merchants = merchants;
    }

    public String getBeforeDateTimestamp() {
        return beforeDateTimestamp;
    }

    public void setBeforeDateTimestamp(String beforeDateTimestamp) {
        this.beforeDateTimestamp = beforeDateTimestamp;
    }

    public String getAfterDateTimestamp() {
        return afterDateTimestamp;
    }

    public void setAfterDateTimestamp(String afterDateTimestamp) {
        this.afterDateTimestamp = afterDateTimestamp;
    }

    public Boolean isReimbursement() {
        return isReimbursement;
    }

    public Receipt.ReimbursementState getReimbursementState() {
        return reimbursementState;
    }

    public void setReimbursement(Boolean reimbursement) {
        isReimbursement = reimbursement;
    }

    public void setReimbursementState(Receipt.ReimbursementState reimbursementState) {
        this.reimbursementState = reimbursementState;
    }

    /** Performs validation on the fields of the Filter object. Can be used to generate user-facing error messages as long as a Context object is passed
     * @param context Context object
     * @throws FilterValidationException throws a FilterValidationException when the Filter object fails validation
     */
    public void validate(Context context) throws FilterValidationException {
        if (beforeDateTimestamp != null && afterDateTimestamp != null){
            if (Long.parseLong(afterDateTimestamp) > Long.parseLong(beforeDateTimestamp)){
                if (context != null && context.getString(R.string.filter_invalid_before_after_date_error_message) != null){
                    throw new FilterValidationException(context.getString(R.string.filter_invalid_before_after_date_error_message));
                } else {
                    throw new FilterValidationException("afterDateTimestamp cannot be greater than beforeDateTimestamp");
                }
            }
        }
        if (greaterThanAmount != null && lessThanAmount != null){
            if (greaterThanAmount > lessThanAmount){
                if (context != null && context.getString(R.string.filter_invalid_greater_less_amount_error_message) != null){
                    throw new FilterValidationException(context.getString(R.string.filter_invalid_greater_less_amount_error_message));
                } else {
                    throw new FilterValidationException("greaterThanAmount value cannot be greater than lessThanAmount value");
                }
            }
        }
    }

    /**
     * Sets all filter fields to null
     */
    public void reset(){
        searchQuery = null;
        greaterThanAmount = null;
        lessThanAmount = null;
        tag = null;
        merchants = null;
        beforeDateTimestamp = null;
        afterDateTimestamp = null;
        isReimbursement = null;
        reimbursementState = null;
    }
}
