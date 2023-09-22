package com.crypto.controller;

import com.crypto.controller.dto.AddCurrencyDto;
import com.crypto.controller.dto.CurrencySummaryDto;
import com.crypto.controller.dto.NormalizedCurrencyRangeDto;
import com.crypto.controller.mapper.CurrencyApiMapper;
import com.crypto.model.CurrencyRangeSummary;
import com.crypto.service.CryptoCurrencyRangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/crypto")
@RequiredArgsConstructor
public class CryptoCurrencyController {

    private final CryptoCurrencyRangeService currencyRangeService;
    private final CurrencyApiMapper mapper;

    @GetMapping("/max")
    public NormalizedCurrencyRangeDto getMaxRangeCurrency(@RequestParam Integer year, @RequestParam Integer month,
            @RequestParam Integer day) {
        return mapper.currencyRangeToDto(currencyRangeService.getMaxRangeDayCurrency(year, month, day));
    }

    @GetMapping("/{currency}")
    public CurrencySummaryDto getCurrencyRange(@PathVariable String currency) {
        return mapper.summaryToDto(currencyRangeService.getLastCurrencySummary(currency));
    }

    @GetMapping
    public List<NormalizedCurrencyRangeDto> getAllCryptoCurrencies(@RequestParam(required = false) String filter) {
        List<CurrencyRangeSummary> summaries = currencyRangeService.getSortedCurrencyRanges();
        return mapper.rangeSummaryToDtos(summaries);
    }

    @PostMapping
    public void addCurrency(@RequestBody AddCurrencyDto currency) {
        currencyRangeService.watchCurrency(currency.getName());
    }

    @PostMapping("/{currency}/upload")
    public void uploadCurrencyPrices(@PathVariable String currency, @RequestParam("file") MultipartFile file) {
        currencyRangeService.uploadRange(currency, file);
    }

}
