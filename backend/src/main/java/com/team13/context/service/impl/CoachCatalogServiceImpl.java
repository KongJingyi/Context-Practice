package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.common.ResourceNotFoundException;
import com.team13.context.entity.CoachProfile;
import com.team13.context.entity.Rating;
import com.team13.context.mapper.CoachProfileMapper;
import com.team13.context.mapper.CoachSceneMapper;
import com.team13.context.mapper.RatingMapper;
import com.team13.context.service.frontend.CoachCatalogService;
import com.team13.context.service.frontend.support.FrontendCoachSupport;
import com.team13.context.service.frontend.support.SceneCategorySupport;
import com.team13.context.service.support.OrderBookingSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CoachCatalogServiceImpl implements CoachCatalogService {

    private final CoachProfileMapper coachProfileMapper;
    private final CoachSceneMapper coachSceneMapper;
    private final RatingMapper ratingMapper;
    private final OrderBookingSupport orderBookingSupport;

    @Override
    public List<Map<String, Object>> listCoaches(
            String sceneId,
            Integer levelMin,
            Integer levelMax,
            Double stars,
            List<String> tags,
            Boolean todayOnly) {
        List<CoachProfile> profiles = coachProfileMapper.selectList(
                Wrappers.<CoachProfile>lambdaQuery()
                        .eq(CoachProfile::getStatus, 1)
                        .orderByAsc(CoachProfile::getUserId));
        List<Long> sceneCoachIds = resolveSceneCoachIds(sceneId);
        List<Map<String, Object>> result = new ArrayList<>();
        int index = 0;
        for (CoachProfile profile : profiles) {
            if (!sceneCoachIds.isEmpty() && !sceneCoachIds.contains(profile.getUserId())) {
                continue;
            }
            Map<String, Object> coach = FrontendCoachSupport.toCoachSummary(profile, sceneId, index++);
            if (!matchesFilter(coach, levelMin, levelMax, stars, tags, todayOnly)) {
                continue;
            }
            result.add(coach);
        }
        result.sort(Comparator.comparing(c -> -((Number) c.get("rating")).doubleValue()));
        return result;
    }

    @Override
    public Map<String, Object> getCoachDetail(String coachId) {
        Long userId = FrontendCoachSupport.parseCoachUserId(coachId);
        if (userId == null) {
            throw new ResourceNotFoundException("陪练员不存在");
        }
        CoachProfile profile = coachProfileMapper.selectById(userId);
        if (profile == null || !Objects.equals(profile.getStatus(), 1)) {
            throw new ResourceNotFoundException("陪练员不存在");
        }
        String sceneId = coachId.contains("-") ? coachId.substring(coachId.indexOf('-') + 1, coachId.lastIndexOf('-')) : "default";
        Map<String, Object> summary = FrontendCoachSupport.toCoachSummary(profile, sceneId, 0);
        return FrontendCoachSupport.enrichDetail(summary);
    }

    @Override
    public List<Map<String, Object>> listCoachReviews(String coachId) {
        Long coachUserId = FrontendCoachSupport.parseCoachUserId(coachId);
        if (coachUserId == null) {
            return List.of();
        }
        List<Rating> ratings = ratingMapper.selectList(
                Wrappers.<Rating>lambdaQuery()
                        .eq(Rating::getCoachId, coachUserId)
                        .eq(Rating::getStatus, 1)
                        .orderByDesc(Rating::getCreatedAt)
                        .last("LIMIT 20"));
        if (ratings.isEmpty()) {
            return List.of(
                    demoReviewSnippet("rv-demo-1", "学员 A", 5, "讲解清晰，追问到位", List.of("非常专业")),
                    demoReviewSnippet("rv-demo-2", "学员 B", 5, "节奏把控很好，收获很大", List.of("氛围感强"))
            );
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (Rating rating : ratings) {
            double avg = (rating.getScoreProfessional() + rating.getScoreAttitude() + rating.getScoreQuality()) / 3.0;
            items.add(reviewSnippet(rating, (int) Math.round(avg), rating.getContent()));
        }
        return items;
    }

    @Override
    public Map<String, Object> getExpertDetail(String expertId) {
        return FrontendCoachSupport.toExpertDetail(getCoachDetail(expertId));
    }

    @Override
    public List<Map<String, Object>> getExpertSlots(String expertId, String date) {
        getCoachDetail(expertId);
        Long coachUserId = FrontendCoachSupport.parseCoachUserId(expertId);
        if (coachUserId == null) {
            return List.of();
        }
        LocalDate day = date != null && !date.isBlank() ? LocalDate.parse(date.trim()) : LocalDate.now();
        return orderBookingSupport.buildSlotOptions(coachUserId, day);
    }

    private List<Long> resolveSceneCoachIds(String sceneId) {
        if (!StringUtils.hasText(sceneId)) {
            return List.of();
        }
        String categoryId = SceneCategorySupport.resolveCategoryId(sceneId.trim());
        List<Long> ids = coachSceneMapper.findCoachIdsByScene(sceneId.trim(), categoryId);
        return ids != null ? ids : List.of();
    }

    private static Map<String, Object> reviewSnippet(Rating rating, int score, String content) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", String.valueOf(rating.getId()));
        item.put("user", Objects.equals(rating.getIsAnonymous(), 1) ? "匿名学员" : "学员");
        item.put("userName", Objects.equals(rating.getIsAnonymous(), 1) ? "匿名学员" : "学员");
        item.put("score", score);
        item.put("content", content);
        item.put("date", rating.getCreatedAt() != null ? rating.getCreatedAt().toLocalDate().toString() : "");
        if (StringUtils.hasText(rating.getTags())) {
            item.put("tags", List.of(rating.getTags().split(",")));
        } else {
            item.put("tags", List.of());
        }
        return item;
    }

    private static Map<String, Object> demoReviewSnippet(
            String id, String userName, int score, String content, List<String> tags) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("user", userName);
        item.put("userName", userName);
        item.put("score", score);
        item.put("content", content);
        item.put("tags", tags);
        item.put("date", "");
        return item;
    }

    @SuppressWarnings("unchecked")
    private static boolean matchesFilter(
            Map<String, Object> coach,
            Integer levelMin,
            Integer levelMax,
            Double stars,
            List<String> tags,
            Boolean todayOnly) {
        int levelNum = ((Number) coach.get("levelNum")).intValue();
        if (levelMin != null && levelNum < levelMin) {
            return false;
        }
        if (levelMax != null && levelNum > levelMax) {
            return false;
        }
        if (stars != null && ((Number) coach.get("rating")).doubleValue() < stars) {
            return false;
        }
        if (Boolean.TRUE.equals(todayOnly) && !Boolean.TRUE.equals(coach.get("availableToday"))) {
            return false;
        }
        if (tags != null && !tags.isEmpty()) {
            List<String> specialtyIds = (List<String>) coach.get("specialtyIds");
            boolean any = tags.stream()
                    .filter(Objects::nonNull)
                    .map(t -> t.toLowerCase(Locale.ROOT))
                    .anyMatch(specialtyIds::contains);
            if (!any) {
                return false;
            }
        }
        return true;
    }
}
