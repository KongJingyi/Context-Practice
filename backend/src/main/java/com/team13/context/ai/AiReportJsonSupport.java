package com.team13.context.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 解析大模型返回的 JSON 报告，并映射到 {@code ctx_training_report} 字段。
 */
@Component
@RequiredArgsConstructor
public class AiReportJsonSupport {

    public static final String FALLBACK_JSON =
            "{\"score\":60,\"suggestions\":[\"AI分析服务暂时开小差了，请重试\"]}";

    private final ObjectMapper objectMapper;

    public ParsedAiReport parse(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            int totalScore = root.path("score").asInt(60);
            String highlights = root.has("suggestions")
                    ? root.get("suggestions").toString()
                    : null;
            return new ParsedAiReport(
                    rawJson,
                    toFivePointScale(totalScore),
                    toFivePointScale(totalScore),
                    toFivePointScale(totalScore),
                    highlights);
        } catch (Exception e) {
            return new ParsedAiReport(FALLBACK_JSON, 3, 3, 3, "[\"解析报告失败，请重试\"]");
        }
    }

    /** 将 0–100 总分映射为 1–5 档（答辩展示用）。 */
    static int toFivePointScale(int totalScore) {
        if (totalScore <= 0) {
            return 1;
        }
        if (totalScore >= 100) {
            return 5;
        }
        return Math.min(5, Math.max(1, (totalScore + 19) / 20));
    }

    public record ParsedAiReport(
            String coachFeedbackJson,
            Integer scoreLogic,
            Integer scoreFluency,
            Integer scorePressure,
            String highlightsJson) {
    }
}
