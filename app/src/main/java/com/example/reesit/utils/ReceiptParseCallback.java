package com.example.reesit.utils;

import com.example.reesit.exceptions.ReceiptParsingException;
import com.example.reesit.models.Receipt;

public interface ReceiptParseCallback{
    public void onSuccess(Receipt receipt);

    public void onFailure(ReceiptParsingException e);
}
