package com.example.reesit.models;

import com.example.reesit.misc.Filter;
import com.parse.ParseObject;

public class ReceiptCategory {
    private Filter filter;
    private ParseObject parseObject;

    public ReceiptCategory(Filter filter){
        this.filter = filter;
        this.parseObject = null;
    }

    public static ReceiptCategory fromParseObject(ParseObject parseObject){
        ReceiptCategory receiptCategory = new ReceiptCategory(null);
        receiptCategory.parseObject = parseObject;


        return receiptCategory;
    }
}
