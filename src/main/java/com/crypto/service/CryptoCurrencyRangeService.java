package com.crypto.service;

import com.crypto.model.CurrencyPrice;
import com.crypto.model.CurrencyRange;
import com.crypto.model.CurrencyRangeSummary;
import com.crypto.model.exception.CurrencyNotSupportedException;
import com.crypto.model.exception.InvalidDateException;
import com.crypto.repository.CurrencyRangeRepository;
import com.crypto.repository.WatchedCurrencyRepository;
import com.crypto.service.parser.PriceFileReader;
import com.crypto.service.parser.dto.DayPriceRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CryptoCurrencyRangeService {

    private final WatchedCurrencyRepository watchedCurrencyRepository;
    private final CurrencyRangeRepository currencyRangeRepository;
    private final PriceFileReader priceFileReader;
    private final static int NORMALIZATION_SCALE = 10;
    private final static BiFunction<BigDecimal, BigDecimal, BigDecimal> NORMALIZE_RANGE =
            (max, min) -> max.subtract(min).divide(min, NORMALIZATION_SCALE, RoundingMode.HALF_DOWN);

    public CurrencyRange getMaxRangeDayCurrency(Integer year, Integer month, Integer day) {
        return watchedCurrencyRepository.get()
                .stream().map(currency -> currencyRangeRepository.getDayRange(currency, year, month, day))
                .filter(Objects::nonNull)
                .max(Comparator.comparing(CurrencyRange::getNormalized)).orElse(null);
    }

    public List<CurrencyRangeSummary> getSortedCurrencyRanges() {
        return watchedCurrencyRepository.get()
                .stream().map(currencyRangeRepository::getLastSummary)
                .filter(Objects::nonNull)
                .sorted((s1, s2) -> s2.getRange().getNormalized().compareTo(s1.getRange().getNormalized()))
                .collect(Collectors.toList());
    }

    public CurrencyRangeSummary getLastCurrencySummary(String currency) {
        currency = currency.toUpperCase();
        validateSupportedCurrency(currency);
        return currencyRangeRepository.getLastSummary(currency.toUpperCase());
    }

    public void watchCurrency(String currency) {
        watchedCurrencyRepository.add(currency.toUpperCase());
    }

    public void uploadRange(String currency, MultipartFile file) {
        currency = currency.toUpperCase();
        validateSupportedCurrency(currency);

        List<DayPriceRecord> priceRecords = priceFileReader.readFile(file);

        if (priceRecords.isEmpty()) {
            return;
        }

        LocalDateTime reportDate = getRecordDate(priceRecords.get(0).getTimestamp());

        // get all currency range summaries to update
        CurrencyRangeSummary rangeLastSummary = currencyRangeRepository.getLastSummary(currency);
        CurrencyRangeSummary rangeYearSummary = currencyRangeRepository.getYearSummary(currency, reportDate.getYear());
        CurrencyRangeSummary rangeMonthSummary = new CurrencyRangeSummary();


        Integer currentDay = null;
        BigDecimal currentDayMin = null;
        BigDecimal currentDayMax = null;
        BigDecimal currentMonthMin = priceRecords.get(0).getPrice();
        BigDecimal currentMonthMax = priceRecords.get(0).getPrice();

        for (int i = 0; i < priceRecords.size(); i++) {
            LocalDateTime recordDate = getRecordDate((priceRecords.get(i).getTimestamp()));
            validateRecord(priceRecords.get(i), reportDate, recordDate);
            if (i == 0) {
                currentDay = recordDate.getDayOfMonth();
                currentDayMin = priceRecords.get(i).getPrice();
                currentDayMax = priceRecords.get(i).getPrice();
                // save the oldest price of month because of first element of the list
                rangeMonthSummary.setOldest(
                        new CurrencyPrice(priceRecords.get(i).getTimestamp(), priceRecords.get(i).getPrice())
                );
            }

            currentMonthMin = currentMonthMin.min(priceRecords.get(i).getPrice());
            currentMonthMax = currentMonthMax.max(priceRecords.get(i).getPrice());

            if (currentDay != recordDate.getDayOfMonth()) {
                // save day range because of record day changed
                saveDayRange(currency, currentDayMin, currentDayMax, recordDate, currentDay);
                currentDay = recordDate.getDayOfMonth();
                currentDayMin = priceRecords.get(i).getPrice();
                currentDayMax = priceRecords.get(i).getPrice();
            } else {
                currentDayMin = currentDayMin.min(priceRecords.get(i).getPrice());
                currentDayMax = currentDayMax.max(priceRecords.get(i).getPrice());
            }

            if (i == priceRecords.size() - 1) {
                // save day range because of last record
                saveDayRange(currency, currentDayMin, currentDayMax, recordDate, currentDay);
                // save the newest price of month because of the last element of the list
                rangeMonthSummary.setNewest(new CurrencyPrice(priceRecords.get(i).getTimestamp(), priceRecords.get(i).getPrice()));
            }
        }

        // update month summary
        rangeMonthSummary.setRange(
                new CurrencyRange(currency, currentMonthMin, currentMonthMax, NORMALIZE_RANGE.apply(currentMonthMax, currentMonthMin))
        );
        currencyRangeRepository.saveMonthSummary(rangeMonthSummary, currency, reportDate.getYear(), reportDate.getMonthValue() + 1);

        // update year summary
        rangeYearSummary = refreshSummary(currency, rangeYearSummary, rangeMonthSummary);
        currencyRangeRepository.saveYearSummary(rangeMonthSummary, currency, reportDate.getYear());

        // update summary
        rangeLastSummary = refreshSummary(currency, rangeLastSummary, rangeYearSummary);
        currencyRangeRepository.saveLastSummary(rangeLastSummary, currency);
    }

    private LocalDateTime getRecordDate(Long timestamp) {
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
        } catch (DateTimeException e) {
            throw new InvalidDateException();
        }
    }

    private void validateRecord(DayPriceRecord record, LocalDateTime reportedDate, LocalDateTime recordDate) {
        validateSupportedCurrency(record.getCurrency());
        if (record.getPrice() == null || record.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CurrencyNotSupportedException();
        }
        if (recordDate.getYear() != reportedDate.getYear() && recordDate.getMonthValue() != reportedDate.getYear()) {
            throw new InvalidDateException();
        }
    }

    private void validateSupportedCurrency(String currency) {
        if (!watchedCurrencyRepository.get().contains(currency)) {
            throw new CurrencyNotSupportedException();
        }
    }

    private void saveDayRange(String currency, BigDecimal currentDayMin, BigDecimal currentDayMax, LocalDateTime recordDate, Integer currentDay) {
        currencyRangeRepository.saveDayRange(
                new CurrencyRange(currency, currentDayMin, currentDayMax, NORMALIZE_RANGE.apply(currentDayMax, currentDayMin)),
                currency,
                recordDate.getYear(),
                recordDate.getMonthValue(),
                currentDay
        );
    }

    private static CurrencyRangeSummary refreshSummary(String currency, CurrencyRangeSummary summary, CurrencyRangeSummary lessPeriodSummary) {
        if (summary == null) {
            summary = new CurrencyRangeSummary();
            summary.setOldest(lessPeriodSummary.getOldest());
            summary.setNewest(lessPeriodSummary.getNewest());
            summary.setRange(lessPeriodSummary.getRange());
            summary.setCurrency(currency);
        } else {
            if (summary.getNewest().getTimestamp() > lessPeriodSummary.getNewest().getTimestamp()) {
                summary.setNewest(lessPeriodSummary.getNewest());
            }

            if (summary.getOldest().getTimestamp() < lessPeriodSummary.getOldest().getTimestamp()) {
                summary.setOldest(lessPeriodSummary.getOldest());
            }

            summary.getRange().setMin(summary.getRange().getMin().min(lessPeriodSummary.getRange().getMin()));
            summary.getRange().setMax(summary.getRange().getMax().max(lessPeriodSummary.getRange().getMax()));

            summary.getRange().setNormalized(NORMALIZE_RANGE.apply(summary.getRange().getMax(), summary.getRange().getMin()));
        }
        return summary;
    }

}
