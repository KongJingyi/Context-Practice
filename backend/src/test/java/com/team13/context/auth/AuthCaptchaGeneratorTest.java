package com.team13.context.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthCaptchaGeneratorTest {

    @Test
    void generatesPngDataUrl() {
        AuthCaptchaGenerator.CaptchaImage image = AuthCaptchaGenerator.toCaptchaImage("A2B3", 120, 44);
        assertThat(image.getDataUrl()).startsWith("data:image/png;base64,");
        assertThat(image.getCode()).isEqualTo("A2B3");
    }

    @Test
    void randomCodeUsesAllowedChars() {
        String code = AuthCaptchaGenerator.randomCode(4);
        assertThat(code).hasSize(4);
        assertThat(code).matches("[A-Z2-9]{4}");
    }
}
