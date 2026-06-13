package com.team13.context.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team13.context.ai.AiReportFacade;
import com.team13.context.ai.AiReportJsonSupport;
import com.team13.context.config.AiProperties;
import com.team13.context.entity.Scene;
import com.team13.context.entity.TrainingRecord;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.SceneMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 通过 OpenAI 兼容接口（DeepSeek / Kimi 等）生成结构化训练报告 JSON。
 */
@Service
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true")
@Slf4j
public class RealAiReportFacade implements AiReportFacade {

    private static final String SYSTEM_PROMPT =
            "你是一个专业的高级职场沟通专家和大厂面试官。请针对用户的临场表达文本，"
                    + "从【结构化表达（如金字塔原理）】、【逻辑严密性】、【抗压沟通策略】三个维度进行深度纠错与打分。"
                    + "必须严格返回JSON格式，包含 score(0-100整数总分), suggestions(字符串数组改进建议) 字段，不要输出其它字段。";

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final OrderMapper orderMapper;
    private final SceneMapper sceneMapper;
    private final RestClient restClient;

    public RealAiReportFacade(
            AiProperties aiProperties,
            ObjectMapper objectMapper,
            OrderMapper orderMapper,
            SceneMapper sceneMapper,
            @Autowired(required = false) RestClient aiRestClient) {
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.orderMapper = orderMapper;
        this.sceneMapper = sceneMapper;
        this.restClient = aiRestClient;
    }

    @Override
    public String generateTrainingReport(TrainingRecord record, String sessionSummary) {
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank() || restClient == null) {
            log.warn("AI 未配置 API Key，使用降级报告");
            return AiReportJsonSupport.FALLBACK_JSON;
        }

        String sceneName = resolveSceneName(record);
        String transcript = sessionSummary == null || sessionSummary.isBlank()
                ? "（用户未提交文字稿，请基于场景给出通用改进建议）"
                : sessionSummary;
        String userPrompt = String.format(
                "当前模拟场景：%s。以下是用户的表达文本：\"%s\"", sceneName, transcript);

        Map<String, Object> requestBody = Map.of(
                "model", aiProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userPrompt)),
                "response_format", Map.of("type", "json_object"));

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);
            if (response == null) {
                return AiReportJsonSupport.FALLBACK_JSON;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return AiReportJsonSupport.FALLBACK_JSON;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) {
                return AiReportJsonSupport.FALLBACK_JSON;
            }
            String content = (String) message.get("content");
            if (content == null || content.isBlank()) {
                return AiReportJsonSupport.FALLBACK_JSON;
            }
            // 校验是否为合法 JSON
            JsonNode node = objectMapper.readTree(content);
            return node.toString();
        } catch (Exception e) {
            log.error("调用大模型生成报告失败", e);
            return AiReportJsonSupport.FALLBACK_JSON;
        }
    }

    private String resolveSceneName(TrainingRecord record) {
        if (record.getOrderId() == null) {
            return "通用沟通训练";
        }
        var order = orderMapper.selectById(record.getOrderId());
        if (order == null || order.getSceneId() == null) {
            return "通用沟通训练";
        }
        Scene scene = sceneMapper.selectById(order.getSceneId());
        return scene != null && scene.getName() != null ? scene.getName() : "通用沟通训练";
    }
}
