package com.example.reesit.misc;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.reesit.R;
import com.example.reesit.models.Merchant;
import com.example.reesit.models.Receipt;
import com.example.reesit.models.Tag;
import com.example.reesit.utils.CurrencyUtils;
import com.example.reesit.utils.DateTimeUtils;
import com.example.reesit.utils.Utils;
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
    private List<Tag> tags;
    private List<Merchant> merchants;
    private String beforeDateTimestamp;
    private String afterDateTimestamp;

    public class FilterGenerateQueryException extends Exception{
        public FilterGenerateQueryException(String message){
            super(message);
        }
        public FilterGenerateQueryException(String message, Throwable err){
            super(message, err);
        }
    }

    public Filter(){

    }




    public ParseQuery<ParseObject> getParseQuery() throws FilterGenerateQueryException {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(Receipt.PARSE_CLASS_NAME);
        query.include(Receipt.KEY_MERCHANT);
        if (searchQuery != null){
            query.whereContains(Receipt.KEY_RECEIPT_TEXT, searchQuery.toLowerCase(Locale.ROOT));
        }

        if (greaterThanAmount != null && lessThanAmount != null){
            if (greaterThanAmount > lessThanAmount){
                throw new FilterGenerateQueryException("greaterThanAmount value cannot be greater than lessThanAmount value");
            }
            query.whereGreaterThanOrEqualTo(Receipt.KEY_AMOUNT, greaterThanAmount);
            query.whereLessThanOrEqualTo(Receipt.KEY_AMOUNT, lessThanAmount);
        } else if (greaterThanAmount != null){
            query.whereGreaterThanOrEqualTo(Receipt.KEY_AMOUNT, greaterThanAmount);
        } else if (lessThanAmount != null){
            query.whereLessThanOrEqualTo(Receipt.KEY_AMOUNT, lessThanAmount);
        }

        // add tag support


        if (merchants != null){
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
                throw new FilterGenerateQueryException("afterDateTimestamp cannot be greater than beforeDateTimestamp");
            }
            query.whereGreaterThanOrEqualTo(Receipt.KEY_DATE_TIME_STAMP, afterDateTimestamp);
            query.whereLessThanOrEqualTo(Receipt.KEY_DATE_TIME_STAMP, beforeDateTimestamp);
        } else if (beforeDateTimestamp != null){
            query.whereLessThanOrEqualTo(Receipt.KEY_DATE_TIME_STAMP, beforeDateTimestamp);
        } else if (afterDateTimestamp != null){
            query.whereGreaterThanOrEqualTo(Receipt.KEY_DATE_TIME_STAMP, afterDateTimestamp);
        }


        return query;
    }

    public String getStringValue(Context context){
        String result = "";
        if (searchQuery != null){
            result += "Query: "+ "'" + searchQuery+ "'" + "\n";
        }
        String amountLine = "";
        if (greaterThanAmount != null && lessThanAmount != null){
            amountLine = context.getString(R.string.filter_tostring_amount_format, CurrencyUtils.integerToCurrency(greaterThanAmount), CurrencyUtils.integerToCurrency(lessThanAmount));
        } else if (greaterThanAmount != null){
            amountLine = context.getString(R.string.filter_tostring_amount_greater_format, CurrencyUtils.integerToCurrency(greaterThanAmount));
        } else if (lessThanAmount != null) {
            amountLine = context.getString(R.string.filter_tostring_amount_less_format, CurrencyUtils.integerToCurrency(lessThanAmount));
        }
        result += amountLine + "\n";

        if (tags.size() > 0){
            StringBuilder tagsLine = new StringBuilder();
            for(Tag tag: tags){
                tagsLine.append(tag.getName()).append(", ");
            }
            // remove trailing ", "
            tagsLine.delete(tagsLine.length()-2, tagsLine.length());
            result += context.getString(R.string.filter_tostring_tags_format, tagsLine.toString()+"\n");
        }

        if (merchants.size() > 0){
            StringBuilder merchantsLine = new StringBuilder();
            for(Merchant merchant: merchants){
                merchantsLine.append(merchant.getName()).append(", ");
            }
            // remove trailing ", "
            merchantsLine.delete(merchantsLine.length()-2, merchantsLine.length());
            result += context.getString(R.string.filter_tostring_merchants_format, merchantsLine.toString()+"\n");
        }

        String dateLine = "";
        if (beforeDateTimestamp != null && afterDateTimestamp != null){
            dateLine = context.getString(R.string.filter_tostring_date_format,
                    DateTimeUtils.getDateAndTimeReceiptCard(afterDateTimestamp),
                    DateTimeUtils.getDateAndTimeReceiptCard(beforeDateTimestamp));
        } else if (beforeDateTimestamp != null){
            dateLine = context.getString(R.string.filter_tostring_date_before_format,
                    DateTimeUtils.getDateAndTimeReceiptCard(beforeDateTimestamp));
        } else if (afterDateTimestamp != null){
            dateLine = context.getString(R.string.filter_tostring_date_after_format,
                    DateTimeUtils.getDateAndTimeReceiptCard(afterDateTimestamp));
        }

        result += dateLine;


        return result;
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

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
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
}
