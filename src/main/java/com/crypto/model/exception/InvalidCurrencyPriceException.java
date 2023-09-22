package com.crypto.model.exception;

public class InvalidCurrencyPriceException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Crypto currency cannot be less then 0";
    }
}
