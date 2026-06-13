package com.team13.context.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthVerificationStore {

    private static final String CAPTCHA_PREFIX = "auth:captcha:";
    private static final String SMS_PREFIX = "auth:sms:";
    private static final String LOCK_PREFIX = "auth:lock:";

    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    private final ConcurrentHashMap<String, MemoryEntry> memory = new ConcurrentHashMap<>();

    private record MemoryEntry(String value, long expireAtEpochMs) {
    }

    public void saveCaptcha(String key, String code, Duration ttl) {
        put(CAPTCHA_PREFIX + key, code.toLowerCase(), ttl);
    }

    /** 校验图形验证码（一次性，校验后删除） */
    public boolean verifyCaptcha(String key, String input) {
        if (key == null || key.isBlank() || input == null) {
            return false;
        }
        String cacheKey = CAPTCHA_PREFIX + key;
        String expected = get(cacheKey);
        delete(cacheKey);
        return expected != null && expected.equals(input.trim().toLowerCase());
    }

    /** 仅读取是否有效，不消耗（内部调试用） */
    public boolean peekCaptchaValid(String key, String input) {
        if (key == null || key.isBlank() || input == null) {
            return false;
        }
        String expected = get(CAPTCHA_PREFIX + key);
        return expected != null && expected.equals(input.trim().toLowerCase());
    }

    public void saveSmsCode(String phone, String scene, String code, Duration ttl) {
        put(SMS_PREFIX + phone + ":" + scene, code, ttl);
    }

    public boolean verifySmsCode(String phone, String scene, String input) {
        String expected = get(SMS_PREFIX + phone + ":" + scene);
        if (expected == null) {
            expected = get(SMS_PREFIX + phone + ":login");
        }
        if (expected == null) {
            expected = get(SMS_PREFIX + phone + ":register");
        }
        if (expected == null || input == null) {
            return false;
        }
        boolean ok = expected.equals(input.trim());
        if (ok) {
            delete(SMS_PREFIX + phone + ":" + scene);
            delete(SMS_PREFIX + phone + ":login");
            delete(SMS_PREFIX + phone + ":register");
        }
        return ok;
    }

    public LoginLockRecord getLoginLock(String phone) {
        String json = get(LOCK_PREFIX + phone);
        if (json == null || json.isBlank()) {
            return LoginLockRecord.empty();
        }
        try {
            return objectMapper.readValue(json, LoginLockRecord.class);
        } catch (JsonProcessingException e) {
            log.warn("parse login lock failed: {}", phone, e);
            return LoginLockRecord.empty();
        }
    }

    public void saveLoginLock(String phone, LoginLockRecord record, Duration ttl) {
        try {
            put(LOCK_PREFIX + phone, objectMapper.writeValueAsString(record), ttl);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("保存登录锁定状态失败", e);
        }
    }

    public void clearLoginLock(String phone) {
        delete(LOCK_PREFIX + phone);
    }

    private static final String PNVS_PREFIX = "auth:pnvs:";

    public void saveSmsPnvsMeta(String phone, String scene, String outId, String schemeName, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(new SmsPnvsMeta(outId, schemeName));
            put(PNVS_PREFIX + phone + ":" + scene, json, ttl);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("保存短信认证会话失败", e);
        }
    }

    public SmsPnvsMeta getSmsPnvsMeta(String phone, String scene) {
        String json = get(PNVS_PREFIX + phone + ":" + scene);
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, SmsPnvsMeta.class);
        } catch (JsonProcessingException e) {
            log.warn("parse sms pnvs meta failed: {}:{}", phone, scene, e);
            return null;
        }
    }

    public void clearSmsPnvsMeta(String phone, String scene) {
        delete(PNVS_PREFIX + phone + ":" + scene);
        delete(PNVS_PREFIX + phone + ":login");
        delete(PNVS_PREFIX + phone + ":register");
    }

    public record SmsPnvsMeta(String outId, String schemeName) {
    }

    private void put(String key, String value, Duration ttl) {
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.opsForValue().set(key, value, ttl);
                return;
            } catch (Exception e) {
                log.warn("Redis unavailable, fallback to memory for key {}", key, e);
            }
        }
        memory.put(key, new MemoryEntry(value, System.currentTimeMillis() + ttl.toMillis()));
    }

    private String get(String key) {
        if (stringRedisTemplate != null) {
            try {
                String value = stringRedisTemplate.opsForValue().get(key);
                if (value != null) {
                    return value;
                }
            } catch (Exception e) {
                log.warn("Redis read failed, fallback to memory for key {}", key, e);
            }
        }
        MemoryEntry entry = memory.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expireAtEpochMs() < System.currentTimeMillis()) {
            memory.remove(key);
            return null;
        }
        return entry.value();
    }

    private void delete(String key) {
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("Redis delete failed for key {}", key, e);
            }
        }
        memory.remove(key);
    }

    public record LoginLockRecord(int failCount, long lockedUntilEpochMs) {
        public static LoginLockRecord empty() {
            return new LoginLockRecord(0, 0L);
        }

        public boolean isLockedNow() {
            return lockedUntilEpochMs > System.currentTimeMillis();
        }

        public int remainSeconds() {
            if (!isLockedNow()) {
                return 0;
            }
            return (int) Math.ceil((lockedUntilEpochMs - System.currentTimeMillis()) / 1000.0);
        }
    }
}
