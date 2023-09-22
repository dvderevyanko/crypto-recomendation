package com.crypto.model;

import lombok.Data;

@Data
public class CurrencyRangeSummary {

    private String currency;
    private CurrencyPrice newest;
    private CurrencyPrice oldest;
    private CurrencyRange range;

}
