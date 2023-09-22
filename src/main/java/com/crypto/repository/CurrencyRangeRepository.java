package com.crypto.repository;

import com.crypto.model.CurrencyRange;
import com.crypto.model.CurrencyRangeSummary;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class CurrencyRangeRepository {

    private final static String DAY_RANGE_KEY = "crypto:%s:year:%d:month:%d:day";
    private final static String LAST_CURRENCY_SUMMARY = "crypto:%s:summary";
    private final static String YEAR_CURRENCY_SUMMARY = "crypto:%s:year:%d:summary";
    private final static String MONTH_CURRENCY_SUMMARY = "crypto:%s:year:%d:month:%d:summary";

    private final HashOperations<String, String, CurrencyRange> hashOperations;
    private final ValueOperations<String, CurrencyRangeSummary> valueOperations;

    public CurrencyRangeRepository(RedisTemplate<String, Object> redisTemplate,
            RedisTemplate<String, CurrencyRangeSummary> currencySummaryRedisTemplate) {
        this.hashOperations = redisTemplate.opsForHash();
        this.valueOperations = currencySummaryRedisTemplate.opsForValue();
    }

    public CurrencyRangeSummary getLastSummary(String currency) {
        return valueOperations.get(String.format(LAST_CURRENCY_SUMMARY, currency));
    }

    public void saveLastSummary(CurrencyRangeSummary summary, String currency) {
        valueOperations.set(String.format(LAST_CURRENCY_SUMMARY, currency), summary);
    }

    public CurrencyRangeSummary getYearSummary(String currency, Integer year) {
        return valueOperations.get(String.format(YEAR_CURRENCY_SUMMARY, currency, year));
    }

    public void saveYearSummary(CurrencyRangeSummary summary, String currency, Integer year) {
        valueOperations.set(String.format(YEAR_CURRENCY_SUMMARY, currency, year), summary);
    }

    public void saveMonthSummary(CurrencyRangeSummary summary, String currency, Integer year, Integer month) {
        valueOperations.set(String.format(MONTH_CURRENCY_SUMMARY, currency, year, month), summary);
    }

    public void saveDayRange(CurrencyRange rate, String currency, Integer year, Integer month, Integer day) {
        hashOperations.put(String.format(DAY_RANGE_KEY, currency, year, month), day.toString(), rate);
    }

    public CurrencyRange getDayRange(String currency, Integer year, Integer month, Integer day) {
        return hashOperations.get(String.format(DAY_RANGE_KEY, currency, year, month), day.toString());
    }
}
