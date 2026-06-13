package com.team13.context.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 启动时打印短信联调模式，便于确认 .env 是否生效。
 */
@Component
@Profile("dev")
@Slf4j
@RequiredArgsConstructor
public class DevAuthConfigLogger {

    private final AuthProperties authProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void logAuthMode() {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path envFile = cwd.resolve(".env");
        Path nestedEnv = cwd.resolve("ContextPractice-backend").resolve(".env");
        log.info(
                "Env check: user.dir={}, .env@cwd={}, .env@nested={}",
                cwd,
                Files.exists(envFile),
                Files.exists(nestedEnv));

        String provider = authProperties.getSmsProvider();
        boolean fixed = StringUtils.hasText(authProperties.getSmsDevFixedCode());
        log.info(
                "SMS mode: provider={}, devFixedCode={}, verifyDevAutoApprove={}",
                provider,
                fixed ? "yes" : "no",
                authProperties.isVerifyDevAutoApprove());
        if (!"dev".equalsIgnoreCase(provider) && !fixed) {
            log.info("Tip: local fixed code requires .env SMS_PROVIDER=dev and SMS_DEV_FIXED_CODE=888888");
        }
    }
}
