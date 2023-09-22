package com.crypto.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Duration DURATION = Duration.ofMinutes(60);
    private static final int REQUEST_NUMBER_LIMIT = 10;

    private final Map<String, Bucket> ipAddressStore = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Bucket tokenBucket = resolveBucket(request.getRemoteAddr());
        ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            return true;
        } else {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests");
            return false;
        }
    }

    private Bucket resolveBucket(String ipAddress) {
        return ipAddressStore.computeIfAbsent(ipAddress, ignored -> {
            Refill refill = Refill.intervally(REQUEST_NUMBER_LIMIT, DURATION);
            Bandwidth limit = Bandwidth.classic(REQUEST_NUMBER_LIMIT, refill);
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        });
    }

}