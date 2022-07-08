package com.example.reesit.utils;

import com.example.reesit.models.Merchant;
import com.parse.ParseException;

import java.util.List;

public interface GetMerchantsCallback {
    public void done(List<Merchant> merchants, ParseException e);
}
