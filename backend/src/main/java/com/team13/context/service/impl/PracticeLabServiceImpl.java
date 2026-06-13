package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.entity.PracticeCategory;
import com.team13.context.entity.PracticeQuestion;
import com.team13.context.mapper.PracticeCategoryMapper;
import com.team13.context.mapper.PracticeQuestionMapper;
import com.team13.context.service.frontend.PracticeLabService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PracticeLabServiceImpl implements PracticeLabService {

    private final PracticeCategoryMapper practiceCategoryMapper;
    private final PracticeQuestionMapper practiceQuestionMapper;

    @Override
    public List<Map<String, Object>> listQuestionCategories() {
        List<PracticeCategory> rows = practiceCategoryMapper.selectList(
                Wrappers.<PracticeCategory>lambdaQuery()
                        .eq(PracticeCategory::getStatus, 1)
                        .orderByAsc(PracticeCategory::getSortOrder));
        List<Map<String, Object>> out = new ArrayList<>();
        for (PracticeCategory c : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId());
            item.put("title", c.getTitle());
            item.put("subtitle", c.getSubtitle());
            item.put("icon", c.getIcon());
            item.put("gradient", List.of(
                    c.getGradientStart() != null ? c.getGradientStart() : "#1e3a8a",
                    c.getGradientEnd() != null ? c.getGradientEnd() : "#3b82f6"));
            item.put("todayCount", c.getTodayCount() != null ? c.getTodayCount() : 0);
            item.put("span", c.getSpan() != null ? c.getSpan() : "normal");
            out.add(item);
        }
        return out;
    }

    @Override
    public List<Map<String, Object>> listQuestions(String category) {
        if (!StringUtils.hasText(category)) {
            return List.of();
        }
        List<PracticeQuestion> rows = practiceQuestionMapper.selectList(
                Wrappers.<PracticeQuestion>lambdaQuery()
                        .eq(PracticeQuestion::getCategoryId, category.trim())
                        .eq(PracticeQuestion::getStatus, 1)
                        .orderByAsc(PracticeQuestion::getSortOrder));
        List<Map<String, Object>> out = new ArrayList<>();
        for (PracticeQuestion q : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", q.getId());
            item.put("text", q.getText());
            PracticeCategory cat = practiceCategoryMapper.selectById(q.getCategoryId());
            item.put("category", cat != null ? cat.getTitle() : q.getCategoryId());
            item.put("categoryId", q.getCategoryId());
            out.add(item);
        }
        return out;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> analyzeVoice(Map<String, Object> body) {
        bumpTodayCount("interview");
        double durationSec = body != null && body.get("durationSec") != null
                ? Double.parseDouble(String.valueOf(body.get("durationSec")))
                : 30.0;
        int score = (int) Math.round(Math.min(95, 68 + durationSec * 0.6 + ThreadLocalRandom.current().nextInt(8)));
        Map<String, Object> radar = Map.of(
                "logic", score - 3,
                "speed", Math.min(98, score + 4),
                "fluency", score - 5,
                "emotion", score,
                "vocabulary", score - 2);
        List<String> suggestions = List.of(
                "语速稍微偏快，建议每句结尾留 0.5 秒停顿",
                "结论先行做得不错，继续保持");
        Map<String, Object> metrics = Map.of(
                "speedWpm", 150 + ThreadLocalRandom.current().nextInt(30),
                "pauseCount", Math.max(2, 8 - (int) (durationSec / 10)),
                "clarity", score - 1);
        return Map.of(
                "score", score,
                "radar", radar,
                "suggestions", suggestions,
                "metrics", metrics);
    }

    @Override
    public Map<String, Object> optimizeText(Map<String, Object> body) {
        String original = body != null ? String.valueOf(body.getOrDefault("text", "")).trim() : "";
        if (!StringUtils.hasText(original)) {
            return Map.of(
                    "original", "",
                    "optimized", "请先输入需要优化的文稿内容。",
                    "changes", List.of());
        }
        String optimized = original
                .replace("我觉得", "我认为")
                .replace("那个", "")
                .replace("挺好的", "具有显著价值")
                .replace("因为", "核心原因在于");
        if (!optimized.startsWith("【结论先行】")) {
            optimized = "【结论先行】" + optimized;
        }
        List<Map<String, Object>> changes = List.of(
                Map.of("offset", 0, "length", 6, "reason", "增加结论先行结构"),
                Map.of("offset", 0, "length", 3, "reason", "口语化改为职场化"));
        return Map.of("original", original, "optimized", optimized, "changes", changes);
    }

    private void bumpTodayCount(String categoryId) {
        PracticeCategory cat = practiceCategoryMapper.selectById(categoryId);
        if (cat != null && cat.getTodayCount() != null) {
            cat.setTodayCount(cat.getTodayCount() + 1);
            practiceCategoryMapper.updateById(cat);
        }
    }
}
