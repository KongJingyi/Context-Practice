package com.team13.context.service.frontend.support;

import com.team13.context.entity.CoachProfile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class FrontendCoachSupport {

    private static final String[] JOB_TITLES = {
            "前字节技术总监", "10 年 HRBP / 面试官", "麦肯锡沟通教练",
            "上市公司培训负责人", "群面金牌导师", "高管演讲顾问"
    };

    private static final String[][] TAG_SETS = {
            {"连环追问", "项目拆解", "STAR 法则"},
            {"压力面", "八大问", "价值观引导"},
            {"群面控场", "观点提炼", "节奏把控"},
            {"复盘答辩", "数据叙事", "结论先行"},
            {"预算争取", "向上管理", "利益对齐"}
    };

    private FrontendCoachSupport() {
    }

    public static Long parseCoachUserId(String coachId) {
        if (coachId == null || coachId.isBlank()) {
            return null;
        }
        if (coachId.chars().allMatch(Character::isDigit)) {
            return Long.parseLong(coachId);
        }
        int lastDash = coachId.lastIndexOf('-');
        if (lastDash >= 0 && lastDash < coachId.length() - 1) {
            String tail = coachId.substring(lastDash + 1);
            if (tail.chars().allMatch(Character::isDigit)) {
                return Long.parseLong(tail);
            }
        }
        return null;
    }

    public static String buildCoachId(String sceneId, Long userId) {
        String scene = sceneId == null || sceneId.isBlank() ? "default" : sceneId;
        return "coach-" + scene + "-" + userId;
    }

    public static Map<String, Object> toCoachSummary(CoachProfile profile, String sceneId, int index) {
        long userId = profile.getUserId();
        int seed = (int) (userId % 1000 + index * 17);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        double rating = 4.2 + (seed % 8) * 0.1;
        int levelNum = levelNumFromProfile(profile, seed);
        String level = levelNum >= 5 ? "gold" : levelNum >= 3 ? "senior" : "rookie";
        String[] tags = parseServiceTags(profile.getServiceTags(), seed);
        List<String> specialtyIds = parseSpecialties(profile.getSpecialties());
        BigDecimal price = profile.getPricePer30m() != null ? profile.getPricePer30m() : BigDecimal.valueOf(128);
        String jobTitle = resolveJobTitle(profile);

        Map<String, Object> coach = new LinkedHashMap<>();
        coach.put("id", buildCoachId(sceneId, userId));
        coach.put("name", profile.getNickname() != null ? profile.getNickname() : "陪练员");
        coach.put("jobTitle", jobTitle);
        coach.put("avatarUrl", profile.getAvatarUrl());
        coach.put("online", seed % 3 != 0);
        coach.put("availableToday", seed % 4 != 0);
        coach.put("rating", Math.round(rating * 10.0) / 10.0);
        coach.put("orderCount", 50 + seed * 3);
        coach.put("skillTags", List.of(tags));
        coach.put("specialtyIds", specialtyIds.isEmpty() ? List.of("logic", "pressure") : specialtyIds);
        coach.put("price", price.intValue());
        coach.put("sessionMinutes", 30);
        coach.put("levelNum", levelNum);
        coach.put("level", level);
        coach.put("activityScore", 60 + seed % 40);
        coach.put("categoryId", sceneId != null ? sceneId : "job-tech-deep");
        coach.put("theme", "indigo");
        coach.put("bio", profile.getBio() != null ? profile.getBio() : "专注结构化表达训练");
        coach.put("highlights", List.of("结论先行", "STAR 拆解", "压力追问"));
        return coach;
    }

    public static Map<String, Object> enrichDetail(Map<String, Object> summary) {
        Map<String, Object> detail = new LinkedHashMap<>(summary);
        detail.put("ratingDistribution", List.of(
                mapOf("stars", 5, "count", 120),
                mapOf("stars", 4, "count", 45),
                mapOf("stars", 3, "count", 8),
                mapOf("stars", 2, "count", 2),
                mapOf("stars", 1, "count", 1)
        ));
        detail.put("certificates", List.of(
                mapOf("id", "c1", "title", "认证陪练员", "imageUrl", "")
        ));
        detail.put("successStories", List.of(
                mapOf("id", "s1", "title", "3 周突破压力面", "subtitle", "学员小李", "metric", "+22 分")
        ));
        detail.put("radar", List.of(
                mapOf("key", "logic", "label", "逻辑", "value", 92),
                mapOf("key", "pressure", "label", "抗压", "value", 88),
                mapOf("key", "star", "label", "STAR", "value", 90),
                mapOf("key", "speech", "label", "表达", "value", 85),
                mapOf("key", "social", "label", "社交", "value", 80)
        ));
        return detail;
    }

    public static Map<String, Object> toExpertDetail(Map<String, Object> coach) {
        Map<String, Object> expert = new LinkedHashMap<>();
        expert.put("id", coach.get("id"));
        expert.put("name", coach.get("name"));
        expert.put("jobTitle", coach.get("jobTitle"));
        expert.put("avatarUrl", coach.get("avatarUrl"));
        expert.put("rating", coach.get("rating"));
        expert.put("orderCount", coach.get("orderCount"));
        expert.put("price", coach.get("price"));
        expert.put("sessionMinutes", coach.get("sessionMinutes"));
        expert.put("intro", coach.get("bio"));
        expert.put("domains", coach.get("skillTags"));
        expert.put("reviews", List.of(
                mapOf("user", "学员 A", "score", 5, "content", "讲解清晰，追问到位"),
                mapOf("user", "学员 B", "score", 5, "content", "帮助很大，推荐")
        ));
        return expert;
    }

    public static List<Map<String, Object>> buildDailySlots(String date) {
        List<Map<String, Object>> slots = new ArrayList<>();
        for (int h = 9; h <= 18; h++) {
            Map<String, Object> slot = new LinkedHashMap<>();
            slot.put("id", date + "-" + h);
            slot.put("label", String.format(Locale.CHINA, "%02d:00-%02d:00", h, h + 1));
            slot.put("booked", ThreadLocalRandom.current().nextBoolean());
            slots.add(slot);
        }
        return slots;
    }

    private static int levelNumFromProfile(CoachProfile profile, int seed) {
        if (profile.getLevelId() != null) {
            long levelId = profile.getLevelId();
            if (levelId >= 3) {
                return 5;
            }
            if (levelId == 2) {
                return 3;
            }
        }
        return 2 + seed % 4;
    }

    private static List<String> parseSpecialties(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String[] parts = raw.split("[,，\\s]+");
        List<String> ids = new ArrayList<>();
        for (String part : parts) {
            if (!part.isBlank()) {
                ids.add(part.trim().toLowerCase(Locale.ROOT));
            }
        }
        return ids;
    }

    private static String resolveJobTitle(CoachProfile profile) {
        if (profile.getBio() != null && !profile.getBio().isBlank()) {
            String bio = profile.getBio().trim();
            int sep = bio.indexOf('·');
            if (sep > 0) {
                return bio.substring(0, sep).trim();
            }
            if (bio.length() <= 24) {
                return bio;
            }
            return bio.substring(0, 24) + "…";
        }
        return JOB_TITLES[(int) (profile.getUserId() % JOB_TITLES.length)];
    }

    private static String[] parseServiceTags(String raw, int seed) {
        if (raw != null && !raw.isBlank()) {
            String[] parts = raw.split("[,，]+");
            List<String> tags = new ArrayList<>();
            for (String part : parts) {
                String t = part.trim();
                if (!t.isEmpty()) {
                    tags.add(t);
                }
            }
            if (!tags.isEmpty()) {
                return tags.toArray(new String[0]);
            }
        }
        return TAG_SETS[seed % TAG_SETS.length];
    }

    private static Map<String, Object> mapOf(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return map;
    }
}
