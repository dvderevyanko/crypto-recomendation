package com.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CurrencyRange {

    private String currency;
    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal normalized;

}
