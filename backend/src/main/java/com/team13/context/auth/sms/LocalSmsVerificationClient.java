package com.team13.context.auth.sms;

import com.team13.context.auth.AuthCaptchaGenerator;
import com.team13.context.auth.AuthVerificationStore;
import com.team13.context.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * 开发模式：本地生成并存储验证码，不真实发短信。
 */
@Slf4j
@RequiredArgsConstructor
public class LocalSmsVerificationClient implements SmsVerificationClient {

    private final AuthProperties authProperties;
    private final AuthVerificationStore verificationStore;

    @Override
    public SmsSendResult send(String phone, String scene) {
        String code = StringUtils.hasText(authProperties.getSmsDevFixedCode())
                ? authProperties.getSmsDevFixedCode()
                : AuthCaptchaGenerator.randomSmsCode();
        verificationStore.saveSmsCode(
                phone,
                scene,
                code,
                Duration.ofSeconds(authProperties.getSmsExpireSeconds()));
        log.info("[SMS-DEV] scene={} phone={} code={}", scene, phone, code);
        String devCode = authProperties.isSmsDevExpose() ? code : null;
        return new SmsSendResult(devCode);
    }

    @Override
    public boolean verify(String phone, String scene, String verifyCode) {
        if (StringUtils.hasText(authProperties.getSmsDevFixedCode())
                && authProperties.getSmsDevFixedCode().equals(verifyCode.trim())) {
            return true;
        }
        return verificationStore.verifySmsCode(phone, scene, verifyCode);
    }
}
