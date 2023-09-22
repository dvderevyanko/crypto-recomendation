package com.crypto.controller.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CurrencySummaryDto {

    private String currency;
    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal oldest;
    private BigDecimal newest;

}
