package com.crypto.repository;

import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class WatchedCurrencyRepository {

    private final static String WATCHED_CRYPTO = "crypto:watched";

    private final SetOperations<String, String> setOperations;

    public WatchedCurrencyRepository(StringRedisTemplate template) {
        this.setOperations = template.opsForSet();
    }

    public Set<String> get() {
        return setOperations.members(WATCHED_CRYPTO);
    }

    public void add(String currency) {
        setOperations.add(WATCHED_CRYPTO, currency);
    }
}
