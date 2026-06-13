package com.team13.context.auth;

import com.team13.context.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信发送频率限制（按手机号）。
 */
@Component
@RequiredArgsConstructor
public class SmsRateLimiter {

    private final AuthProperties authProperties;
    private final ConcurrentHashMap<String, Long> lastSentAt = new ConcurrentHashMap<>();

    public void checkAllowed(String phone) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        long intervalMs = authProperties.getSmsMinIntervalSeconds() * 1000L;
        long now = System.currentTimeMillis();
        Long previous = lastSentAt.put(phone, now);
        if (previous != null && now - previous < intervalMs) {
            long waitSec = (intervalMs - (now - previous) + 999) / 1000;
            throw new IllegalArgumentException("发送过于频繁，请 " + waitSec + " 秒后再试");
        }
    }
}
