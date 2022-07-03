package com.example.reesit.utils;

import com.example.reesit.models.Receipt;
import com.parse.ParseException;

public interface AddReceiptCallback {
    public void done(Receipt receipt, ParseException e);
}
