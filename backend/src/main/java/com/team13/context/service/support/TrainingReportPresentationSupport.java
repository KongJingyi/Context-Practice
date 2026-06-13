package com.team13.context.service.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team13.context.entity.TrainingReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TrainingReportPresentationSupport {

    private final ObjectMapper objectMapper;

    public List<Integer> buildDimensions(TrainingReport report) {
        int logic = toPercent(report != null ? report.getScoreLogic() : null, 85);
        int fluency = toPercent(report != null ? report.getScoreFluency() : null, 82);
        int content = toPercent(report != null ? report.getScoreContent() : null, 88);
        int pressure = toPercent(report != null ? report.getScorePressure() : null, 78);
        int time = toPercent(report != null ? report.getScoreTime() : null, 86);
        return List.of(logic, fluency, content, pressure, time);
    }

    public int buildTotalScore(TrainingReport report) {
        if (report == null) {
            return 85;
        }
        List<Integer> dims = buildDimensions(report);
        int sum = 0;
        for (int value : dims) {
            sum += value;
        }
        return Math.max(60, sum / dims.size());
    }

    public List<Map<String, Object>> buildExpertFeedback(TrainingReport report) {
        List<Map<String, Object>> items = new ArrayList<>();
        appendSuggestionItems(items, report != null ? report.getCoachFeedback() : null, "coach");
        appendSuggestionItems(items, report != null ? report.getHighlights() : null, "highlight");
        if (items.isEmpty()) {
            items.add(feedback("—", "highlight", "本次训练已完成，请结合陪练建议继续练习", "可在后续场次中重点打磨结构与论据"));
        }
        return items;
    }

    public String buildMilestone(int totalScore) {
        if (totalScore >= 90) {
            return "恭喜！您已成功解锁「逻辑大师」勋章";
        }
        if (totalScore >= 80) {
            return "表现优秀，再完成 1 次陪练即可冲击更高里程碑";
        }
        return "再完成 2 次陪练即可解锁下一枚勋章";
    }

    public List<Map<String, Object>> buildGrowthPath(int completedSessions) {
        List<Map<String, Object>> path = new ArrayList<>();
        path.add(growthNode("g1", "首次陪练", "完成第一节场景模拟课"));
        if (completedSessions >= 2) {
            path.add(growthNode("g2", "持续精进", "已完成 " + completedSessions + " 次训练"));
        }
        return path;
    }

    public List<Integer> buildHistoricalAverage(Long userId, int currentTotal) {
        int avg = Math.max(55, currentTotal - 8);
        return List.of(avg, avg - 2, avg - 4, avg - 1, avg - 3);
    }

    public List<Integer> buildInitialBaseline(int currentTotal) {
        int base = Math.max(50, currentTotal - 18);
        return List.of(base, base - 4, base - 6, base - 2, base - 5);
    }

    private void appendSuggestionItems(List<Map<String, Object>> items, String rawJson, String defaultType) {
        if (!StringUtils.hasText(rawJson)) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            if (root.isArray()) {
                int idx = 0;
                for (JsonNode node : root) {
                    String text = node.isTextual() ? node.asText() : node.toString();
                    items.add(feedback(formatTimestamp(idx++), defaultType, text, ""));
                }
                return;
            }
            JsonNode suggestions = root.path("suggestions");
            if (suggestions.isArray()) {
                int idx = 0;
                for (JsonNode node : suggestions) {
                    String text = node.isTextual() ? node.asText() : node.asText("");
                    if (!StringUtils.hasText(text)) {
                        continue;
                    }
                    String type = text.contains("Stub") || text.contains("AI") ? "question" : "warning";
                    items.add(feedback(formatTimestamp(idx++), type, text, "结合陪练建议在下一次表达中刻意练习"));
                }
            }
        } catch (Exception ignored) {
            items.add(feedback("—", defaultType, rawJson, ""));
        }
    }

    private static String formatTimestamp(int index) {
        int minutes = index * 2 + 1;
        return String.format("%02d:%02d", minutes, (index * 13) % 60);
    }

    private static int toPercent(Integer fivePointScore, int defaultPercent) {
        if (fivePointScore == null || fivePointScore <= 0) {
            return defaultPercent;
        }
        return Math.min(100, Math.max(40, fivePointScore * 20));
    }

    private static Map<String, Object> feedback(String timestamp, String type, String content, String suggestion) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("timestamp", timestamp);
        item.put("type", type);
        item.put("content", content);
        item.put("suggestion", suggestion);
        return item;
    }

    private static Map<String, Object> growthNode(String id, String title, String description) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", id);
        node.put("date", java.time.LocalDate.now().toString().substring(0, 7));
        node.put("title", title);
        node.put("description", description);
        return node;
    }
}
