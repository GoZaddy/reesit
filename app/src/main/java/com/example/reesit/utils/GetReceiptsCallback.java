package com.example.reesit.utils;

import com.example.reesit.models.Receipt;
import com.parse.ParseException;

import java.util.List;

public interface GetReceiptsCallback {
    public void done(List<Receipt> receipts, ParseException e);
}
