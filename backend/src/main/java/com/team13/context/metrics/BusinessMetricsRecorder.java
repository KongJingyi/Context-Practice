package com.team13.context.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 业务指标写入 Redis（可选：未配置 Redis Bean 时静默跳过，便于测试环境无 Redis）。
 */
@Component
@Slf4j
public class BusinessMetricsRecorder {

    private static final String PREFIX = "metrics:";

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    public void increment(String metricKey) {
        if (stringRedisTemplate == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().increment(PREFIX + metricKey);
        } catch (Exception e) {
            log.warn("metrics increment failed: {}", metricKey, e);
        }
    }

    public void addLong(String metricKey, long delta) {
        if (stringRedisTemplate == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().increment(PREFIX + metricKey, delta);
        } catch (Exception e) {
            log.warn("metrics addLong failed: {}", metricKey, e);
        }
    }
}
