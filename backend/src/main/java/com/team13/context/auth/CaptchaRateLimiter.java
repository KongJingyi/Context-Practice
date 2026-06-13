package com.team13.context.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图形验证码获取频率限制（按 IP）。
 */
@Component
@Slf4j
public class CaptchaRateLimiter {

    private static final int MAX_PER_WINDOW = 20;
    private static final long WINDOW_MS = 60_000L;

    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public void checkAllowed(String clientKey) {
        if (clientKey == null || clientKey.isBlank()) {
            return;
        }
        long now = System.currentTimeMillis();
        WindowCounter counter = counters.compute(clientKey, (k, existing) -> {
            if (existing == null || now - existing.windowStartMs > WINDOW_MS) {
                return new WindowCounter(now, 1);
            }
            existing.count++;
            return existing;
        });
        if (counter.count > MAX_PER_WINDOW) {
            throw new IllegalArgumentException("验证码请求过于频繁，请稍后再试");
        }
    }

    private static final class WindowCounter {
        private final long windowStartMs;
        private int count;

        private WindowCounter(long windowStartMs, int count) {
            this.windowStartMs = windowStartMs;
            this.count = count;
        }
    }
}
