package com.team13.context.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 在 Spring 解析 application.yml 之前加载 .env 文件。
 * 向上查找含 .env 的目录；并将 SMS_* 同步映射到 app.auth.*，避免占位符未生效。
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "dotenv";

    /** .env 键 → Spring 配置键（与 application-dev.yml 中 app.auth 对应） */
    private static final Map<String, String> APP_AUTH_ALIASES = Map.of(
            "SMS_PROVIDER", "app.auth.sms-provider",
            "SMS_DEV_FIXED_CODE", "app.auth.sms-dev-fixed-code",
            "VERIFY_DEV_AUTO_APPROVE", "app.auth.verify-dev-auto-approve");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envDir = resolveEnvDirectory();
        if (envDir == null) {
            return;
        }

        Dotenv dotenv = Dotenv.configure()
                .directory(envDir.toString())
                .filename(".env")
                .ignoreIfMissing()
                .load();

        Map<String, Object> properties = new LinkedHashMap<>();
        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            if (System.getenv(key) != null || System.getProperty(key) != null) {
                return;
            }
            properties.put(key, value);
            String alias = APP_AUTH_ALIASES.get(key);
            if (alias != null) {
                properties.put(alias, value);
            }
        });

        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        }
    }

    /** 从 user.dir 起向上最多 6 层查找 .env（兼容 IDEA 工作目录在 out、子模块等） */
    private static Path resolveEnvDirectory() {
        Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        for (int i = 0; i < 6 && dir != null; i++) {
            if (Files.exists(dir.resolve(".env"))) {
                return dir;
            }
            dir = dir.getParent();
        }
        return null;
    }
}
