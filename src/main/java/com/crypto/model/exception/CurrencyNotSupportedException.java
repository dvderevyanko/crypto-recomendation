package com.crypto.model.exception;

public class CurrencyNotSupportedException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Crypto currency not supported";
    }
}
