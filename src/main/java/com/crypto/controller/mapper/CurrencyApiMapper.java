package com.crypto.controller.mapper;

import com.crypto.controller.dto.CurrencySummaryDto;
import com.crypto.controller.dto.NormalizedCurrencyRangeDto;
import com.crypto.model.CurrencyRange;
import com.crypto.model.CurrencyRangeSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface CurrencyApiMapper {


    @Mapping(target = "normalizedRange", source = "range.normalized")
    NormalizedCurrencyRangeDto rangeSummaryToDto(CurrencyRangeSummary summary);
    List<NormalizedCurrencyRangeDto> rangeSummaryToDtos(List<CurrencyRangeSummary> summaries);

    @Mapping(target = "normalizedRange", source = "normalized")
    NormalizedCurrencyRangeDto currencyRangeToDto(CurrencyRange range);

    @Mapping(target = "oldest", source = "oldest.price")
    @Mapping(target = "newest", source = "newest.price")
    @Mapping(target = "min", source = "range.min")
    @Mapping(target = "max", source = "range.max")
    CurrencySummaryDto summaryToDto(CurrencyRangeSummary summary);

}