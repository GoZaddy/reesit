package com.example.reesit.exceptions;

public class ReceiptParsingException extends Exception{
    public ReceiptParsingException(String message, Throwable err){
        super(message, err);
    }
}
