package com.team13.context.trtc;

import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 解析腾讯云 TRTC 云录制回调（EventType=311）及内部测试格式。
 */
public final class TrtcRecordingCallbackParser {

    private TrtcRecordingCallbackParser() {
    }

    public static ParsedCallback parse(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return ParsedCallback.empty();
        }
        Object directUrl = payload.get("recordingUrl");
        Object directTaskId = payload.get("taskId");
        if (directUrl != null && StringUtils.hasText(String.valueOf(directUrl))) {
            return new ParsedCallback(
                    directTaskId != null ? String.valueOf(directTaskId) : null,
                    String.valueOf(directUrl),
                    payload.get("trainingId") != null ? Long.parseLong(String.valueOf(payload.get("trainingId"))) : null);
        }

        Object eventType = payload.get("EventType");
        if (eventType != null && !"311".equals(String.valueOf(eventType))) {
            return ParsedCallback.empty();
        }
        Map<String, Object> eventInfo = asMap(payload.get("EventInfo"));
        if (eventInfo.isEmpty()) {
            return ParsedCallback.empty();
        }
        String taskId = eventInfo.get("TaskId") != null ? String.valueOf(eventInfo.get("TaskId")) : null;
        Map<String, Object> innerPayload = asMap(eventInfo.get("Payload"));
        String videoUrl = extractVideoUrl(innerPayload);
        if (!StringUtils.hasText(videoUrl)) {
            return new ParsedCallback(taskId, null, null);
        }
        return new ParsedCallback(taskId, videoUrl, null);
    }

    private static String extractVideoUrl(Map<String, Object> payload) {
        if (payload.isEmpty()) {
            return null;
        }
        Map<String, Object> tencentVod = asMap(payload.get("TencentVod"));
        if (tencentVod.get("VideoUrl") != null) {
            return String.valueOf(tencentVod.get("VideoUrl"));
        }
        if (payload.get("VideoUrl") != null) {
            return String.valueOf(payload.get("VideoUrl"));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object raw) {
        if (!(raw instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    public record ParsedCallback(String taskId, String recordingUrl, Long trainingId) {
        public static ParsedCallback empty() {
            return new ParsedCallback(null, null, null);
        }

        public boolean hasRecordingUrl() {
            return StringUtils.hasText(recordingUrl);
        }
    }
}
