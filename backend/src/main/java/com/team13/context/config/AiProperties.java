package com.team13.context.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型 API 配置（兼容 OpenAI Chat Completions，默认 DeepSeek）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /** 是否启用真实大模型（false 时使用 StubAiReportFacade） */
    private boolean enabled = false;

    /** 如 https://api.deepseek.com 或 https://api.moonshot.cn/v1 */
    private String baseUrl = "https://api.deepseek.com";

    private String apiKey = "";

    /** 如 deepseek-chat / moonshot-v1-8k */
    private String model = "deepseek-chat";

    private int connectTimeoutMs = 10000;

    private int readTimeoutMs = 120000;
}
