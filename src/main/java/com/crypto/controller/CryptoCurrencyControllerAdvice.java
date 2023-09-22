package com.crypto.controller;

import com.crypto.controller.error.RestError;
import com.crypto.model.exception.CurrencyNotSupportedException;
import com.crypto.model.exception.InvalidCurrencyPriceException;
import com.crypto.model.exception.InvalidDateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice("com.crypto.controller")
public class CryptoCurrencyControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidDateException.class})
    public RestError invalidDataException(InvalidDateException exception) {
        log.error(exception.getMessage());
        return new RestError(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidCurrencyPriceException.class})
    public RestError invalidCurrencyPriceException(InvalidCurrencyPriceException exception) {
        log.error(exception.getMessage());
        return new RestError(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({CurrencyNotSupportedException.class})
    public RestError currencyNotSupportedException(Exception exception) {
        log.error(exception.getMessage());
        return new RestError(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public RestError internalException(Exception exception) {
        log.error(exception.getMessage());
        return new RestError(exception.getMessage());
    }

}