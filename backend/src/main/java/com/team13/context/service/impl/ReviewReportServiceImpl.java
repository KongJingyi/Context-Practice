package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.common.ForbiddenOperationException;
import com.team13.context.common.ResourceNotFoundException;
import com.team13.context.common.UserContext;
import com.team13.context.constant.BusinessStatuses;
import com.team13.context.entity.CoachProfile;
import com.team13.context.entity.Order;
import com.team13.context.entity.Rating;
import com.team13.context.entity.Scene;
import com.team13.context.entity.TrainingRecord;
import com.team13.context.entity.TrainingReport;
import com.team13.context.mapper.CoachProfileMapper;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.RatingMapper;
import com.team13.context.mapper.SceneMapper;
import com.team13.context.mapper.TrainingRecordMapper;
import com.team13.context.mapper.TrainingReportMapper;
import com.team13.context.constant.TrainingStatuses;
import com.team13.context.service.frontend.ReviewReportService;
import com.team13.context.service.support.ReviewFlowSupport;
import com.team13.context.service.support.TrainingReportPresentationSupport;
import com.team13.context.service.support.UserDisplayHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewReportServiceImpl implements ReviewReportService {

    private static final int MIN_CONTENT_LENGTH = 20;
    private static final int REWARD_XP = 20;

    private final RatingMapper ratingMapper;
    private final OrderMapper orderMapper;
    private final TrainingRecordMapper trainingRecordMapper;
    private final TrainingReportMapper trainingReportMapper;
    private final SceneMapper sceneMapper;
    private final CoachProfileMapper coachProfileMapper;
    private final ReviewFlowSupport reviewFlowSupport;
    private final TrainingReportPresentationSupport reportPresentation;
    private final UserDisplayHelper userDisplayHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitReview(Map<String, Object> body) {
        Long userId = UserContext.requireUserId();
        Long orderId = parseRequiredOrderId(body.get("order_id"));
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (Objects.equals(order.getCoachId(), userId)) {
            return submitCoachFeedback(order, body);
        }
        return submitUserRating(order, userId, body);
    }

    private Map<String, Object> submitUserRating(Order order, Long userId, Map<String, Object> body) {
        Long orderId = order.getId();

        TrainingRecord training = findLatestTraining(orderId);

        if (reviewFlowSupport.hasRated(orderId)) {
            throw new IllegalArgumentException("该订单已评价，请勿重复提交");
        }
        if (!reviewFlowSupport.canReview(order, training)) {
            throw new IllegalArgumentException("当前订单暂不可评价，请确认训练已结束");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> scores = body.get("scores") instanceof Map<?, ?> map
                ? (Map<String, Object>) body.get("scores")
                : Map.of();
        int professional = requireScore(scores, "professional");
        int attitude = requireScore(scores, "attitude");
        int quality = requireScore(scores, "quality");
        String content = String.valueOf(body.getOrDefault("content", "")).trim();
        if (content.length() < MIN_CONTENT_LENGTH) {
            throw new IllegalArgumentException("评价内容不少于 " + MIN_CONTENT_LENGTH + " 字");
        }

        Rating rating = new Rating();
        rating.setOrderId(orderId);
        rating.setUserId(userId);
        rating.setCoachId(order.getCoachId());
        rating.setScoreProfessional(professional);
        rating.setScoreAttitude(attitude);
        rating.setScoreQuality(quality);
        rating.setContent(content);
        rating.setTags(joinTags(body.get("tags")));
        rating.setIsAnonymous(parseAnonymous(body.get("is_anonymous")) ? 1 : 0);
        rating.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        rating.setCreatedAt(now);
        rating.setUpdatedAt(now);
        ratingMapper.insert(rating);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        result.put("rewardXp", REWARD_XP);
        result.put("hasRated", true);
        return result;
    }

    private Map<String, Object> submitCoachFeedback(Order order, Map<String, Object> body) {
        TrainingRecord training = findLatestTraining(order.getId());
        if (training == null) {
            throw new IllegalArgumentException("训练尚未开始，无法提交反馈");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> scores = body.get("scores") instanceof Map<?, ?> map
                ? (Map<String, Object>) body.get("scores")
                : Map.of();
        int logic = pickScore(scores, "logic", "professional", 4);
        int fluency = pickScore(scores, "fluency", "attitude", 4);
        int contentScore = pickScore(scores, "content", "quality", 4);
        int pressure = pickScore(scores, "pressure", null, 3);
        int time = pickScore(scores, "time", null, 4);
        String feedback = String.valueOf(body.getOrDefault("content", "")).trim();
        if (feedback.length() < 10) {
            throw new IllegalArgumentException("反馈内容不少于 10 字");
        }

        TrainingReport report = trainingReportMapper.selectOne(
                Wrappers.<TrainingReport>lambdaQuery().eq(TrainingReport::getTrainingId, training.getId()));
        LocalDateTime now = LocalDateTime.now();
        if (report == null) {
            report = new TrainingReport();
            report.setTrainingId(training.getId());
            report.setCreatedAt(now);
        }
        report.setScoreLogic(logic);
        report.setScoreFluency(fluency);
        report.setScoreContent(contentScore);
        report.setScorePressure(pressure);
        report.setScoreTime(time);
        report.setCoachFeedback(feedback);
        report.setUpdatedAt(now);
        if (report.getId() == null) {
            trainingReportMapper.insert(report);
        } else {
            trainingReportMapper.updateById(report);
        }

        if (!BusinessStatuses.Order.COMPLETED.equals(order.getStatus())) {
            order.setStatus(BusinessStatuses.Order.COMPLETED);
            order.setUpdatedAt(now);
            orderMapper.updateById(order);
        }
        training.setStatus(TrainingStatuses.REPORT_READY);
        training.setUpdatedAt(now);
        trainingRecordMapper.updateById(training);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        result.put("rewardXp", REWARD_XP);
        return result;
    }

    private static int pickScore(Map<String, Object> scores, String primary, String fallback, int defaultVal) {
        if (scores.get(primary) != null) {
            return clampScore(scores.get(primary));
        }
        if (fallback != null && scores.get(fallback) != null) {
            return clampScore(scores.get(fallback));
        }
        return defaultVal;
    }

    private static int clampScore(Object raw) {
        int value = ((Number) raw).intValue();
        if (value < 1 || value > 5) {
            throw new IllegalArgumentException("评分需在 1-5 之间");
        }
        return value;
    }

    @Override
    public Map<String, Object> getReportByOrderId(String orderId) {
        Long oid = parseOrderId(orderId);
        Order order = orderMapper.selectById(oid);
        if (order == null) {
            throw new ResourceNotFoundException("订单不存在");
        }
        assertCanViewOrder(order);

        TrainingRecord record = findLatestTraining(oid);
        TrainingReport report = record != null
                ? trainingReportMapper.selectOne(
                Wrappers.<TrainingReport>lambdaQuery().eq(TrainingReport::getTrainingId, record.getId()))
                : null;
        Scene scene = order.getSceneId() != null ? sceneMapper.selectById(order.getSceneId()) : null;
        CoachProfile coach = order.getCoachId() != null ? coachProfileMapper.selectById(order.getCoachId()) : null;

        int total = reportPresentation.buildTotalScore(report);
        List<Integer> dimensions = reportPresentation.buildDimensions(report);
        int completedSessions = countCompletedSessions(order.getUserId());

        Map<String, Object> orderInfo = new LinkedHashMap<>();
        orderInfo.put("id", String.valueOf(order.getId()));
        orderInfo.put("scene", scene != null ? scene.getName() : "结构化表达陪练");
        orderInfo.put("date", resolveReportDate(record, order));
        orderInfo.put("expert_name", coach != null ? coach.getNickname() : userDisplayHelper.resolveNickname(order.getCoachId(), true));
        orderInfo.put("expert_title", coach != null && StringUtils.hasText(coach.getBio())
                ? coach.getBio().split("·")[0].trim()
                : "认证陪练");

        Map<String, Object> scores = new LinkedHashMap<>();
        scores.put("total", total);
        scores.put("dimensions", dimensions);
        scores.put("average_values", reportPresentation.buildHistoricalAverage(order.getUserId(), total));
        scores.put("initial", reportPresentation.buildInitialBaseline(total));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("order_info", orderInfo);
        data.put("scores", scores);
        data.put("expert_feedback", reportPresentation.buildExpertFeedback(report));
        String videoUrl = record != null && StringUtils.hasText(record.getRecordingUrl())
                ? record.getRecordingUrl() : "";
        data.put("video_url", videoUrl);
        data.put("video_duration_sec", resolveDurationSeconds(record));
        data.put("milestone", reportPresentation.buildMilestone(total));
        data.put("growth_path", reportPresentation.buildGrowthPath(completedSessions));
        data.put("hasRated", reviewFlowSupport.hasRated(order.getId()));
        data.put("canReview", reviewFlowSupport.canReview(order, record));
        return data;
    }

    @Override
    public Map<String, Object> getReviewStatus(Long orderId) {
        Long userId = UserContext.requireUserId();
        Order order = requireOwnedOrder(orderId, userId);
        TrainingRecord training = findLatestTraining(orderId);
        Rating existing = findRating(orderId);
        boolean hasRated = existing != null;
        boolean reportReady = reviewFlowSupport.isReportReady(order, training);
        boolean canReview = reviewFlowSupport.canReview(order, training);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", orderId);
        data.put("hasRated", hasRated);
        data.put("canReview", canReview);
        data.put("reportReady", reportReady);
        data.put("orderStatus", order.getStatus());
        data.put("trainingStatus", training != null ? training.getStatus() : null);
        data.put("coachId", order.getCoachId());
        data.put("coachName", order.getCoachId() != null ? userDisplayHelper.resolveNickname(order.getCoachId(), true) : null);
        data.put("sceneName", resolveSceneName(order.getSceneId()));
        if (existing != null) {
            data.put("review", Map.of(
                    "professional", existing.getScoreProfessional(),
                    "attitude", existing.getScoreAttitude(),
                    "quality", existing.getScoreQuality(),
                    "content", existing.getContent(),
                    "createdAt", existing.getCreatedAt()));
        }
        return data;
    }

    private Order requireOwnedOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new IllegalArgumentException("订单不存在或无权评价");
        }
        return order;
    }

    private void assertCanViewOrder(Order order) {
        Long currentUserId = UserContext.requireUserId();
        boolean isOwner = Objects.equals(order.getUserId(), currentUserId);
        boolean isCoach = Objects.equals(order.getCoachId(), currentUserId);
        if (!isOwner && !isCoach) {
            throw new ForbiddenOperationException("无权查看该报告");
        }
    }

    private TrainingRecord findLatestTraining(Long orderId) {
        return trainingRecordMapper.selectOne(
                Wrappers.<TrainingRecord>lambdaQuery()
                        .eq(TrainingRecord::getOrderId, orderId)
                        .orderByDesc(TrainingRecord::getCreatedAt)
                        .last("LIMIT 1"));
    }

    private Rating findRating(Long orderId) {
        return ratingMapper.selectOne(
                Wrappers.<Rating>lambdaQuery()
                        .eq(Rating::getOrderId, orderId)
                        .eq(Rating::getStatus, 1)
                        .last("LIMIT 1"));
    }

    private int countCompletedSessions(Long userId) {
        if (userId == null) {
            return 1;
        }
        Long count = orderMapper.selectCount(
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getUserId, userId)
                        .eq(Order::getStatus, BusinessStatuses.Order.COMPLETED));
        return count != null && count > 0 ? count.intValue() : 1;
    }

    private static Long parseRequiredOrderId(Object raw) {
        if (raw == null || !StringUtils.hasText(String.valueOf(raw))) {
            throw new IllegalArgumentException("缺少 order_id");
        }
        return parseOrderId(String.valueOf(raw));
    }

    private static Long parseOrderId(String orderId) {
        if (orderId.chars().allMatch(Character::isDigit)) {
            return Long.parseLong(orderId);
        }
        String digits = orderId.replaceAll("\\D+", "");
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("无效的订单号");
        }
        return Long.parseLong(digits);
    }

    private static int requireScore(Map<String, Object> scores, String key) {
        if (scores == null || scores.get(key) == null) {
            throw new IllegalArgumentException("请完成全部评分项");
        }
        int value = ((Number) scores.get(key)).intValue();
        if (value < 1 || value > 5) {
            throw new IllegalArgumentException("评分需在 1-5 之间");
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static String joinTags(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.joining(","));
        }
        String text = String.valueOf(raw).trim();
        return StringUtils.hasText(text) ? text : null;
    }

    private static boolean parseAnonymous(Object raw) {
        if (raw instanceof Boolean b) {
            return b;
        }
        if (raw == null) {
            return false;
        }
        return Boolean.parseBoolean(String.valueOf(raw));
    }

    private String resolveSceneName(Long sceneId) {
        if (sceneId == null) {
            return null;
        }
        Scene scene = sceneMapper.selectById(sceneId);
        return scene != null ? scene.getName() : null;
    }

    private static String resolveReportDate(TrainingRecord record, Order order) {
        if (record != null && record.getEndedAt() != null) {
            return record.getEndedAt().toLocalDate().toString();
        }
        if (record != null && record.getStartedAt() != null) {
            return record.getStartedAt().toLocalDate().toString();
        }
        if (order.getScheduledStart() != null) {
            return order.getScheduledStart().toLocalDate().toString();
        }
        return order.getCreatedAt() != null ? order.getCreatedAt().toLocalDate().toString() : "";
    }

    private static int resolveDurationSeconds(TrainingRecord record) {
        if (record == null || record.getStartedAt() == null || record.getEndedAt() == null) {
            return 900;
        }
        long seconds = java.time.Duration.between(record.getStartedAt(), record.getEndedAt()).getSeconds();
        return (int) Math.max(60, Math.min(seconds, 7200));
    }
}
