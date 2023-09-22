package com.crypto.model.exception;

public class InvalidDateException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Invalid date in price list";
    }
}
