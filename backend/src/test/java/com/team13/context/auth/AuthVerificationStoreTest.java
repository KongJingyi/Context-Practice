package com.team13.context.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class AuthVerificationStoreTest {

    private AuthVerificationStore store;

    @BeforeEach
    void setUp() {
        store = new AuthVerificationStore(new ObjectMapper());
    }

    @Test
    void captchaIsOneTimeUse() {
        store.saveCaptcha("key-1", "Ab12", Duration.ofMinutes(5));
        assertThat(store.verifyCaptcha("key-1", "ab12")).isTrue();
        assertThat(store.verifyCaptcha("key-1", "ab12")).isFalse();
    }

    @Test
    void captchaExpiresInMemoryFallback() throws InterruptedException {
        store.saveCaptcha("key-2", "X9YZ", Duration.ofMillis(50));
        assertThat(store.peekCaptchaValid("key-2", "x9yz")).isTrue();
        Thread.sleep(80);
        assertThat(store.peekCaptchaValid("key-2", "x9yz")).isFalse();
    }
}
