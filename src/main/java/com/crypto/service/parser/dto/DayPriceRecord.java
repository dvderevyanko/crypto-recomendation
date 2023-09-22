package com.crypto.service.parser.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DayPriceRecord {

    Long timestamp;
    String currency;
    BigDecimal price;

}
