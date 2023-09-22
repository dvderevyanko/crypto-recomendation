package com.crypto.controller.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NormalizedCurrencyRangeDto {

    private String currency;
    private BigDecimal normalizedRange;
}
