package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.ai.AiReportFacade;
import com.team13.context.ai.AiReportJsonSupport;
import com.team13.context.common.ForbiddenOperationException;
import com.team13.context.common.UserContext;
import com.team13.context.constant.BusinessStatuses;
import com.team13.context.constant.TrainingStatuses;
import com.team13.context.dto.AiTrainingReportRequest;
import com.team13.context.dto.TrainingEndRequest;
import com.team13.context.dto.TrainingStartRequest;
import com.team13.context.dto.TrainingStartResponse;
import com.team13.context.entity.Order;
import com.team13.context.entity.Payment;
import com.team13.context.entity.TrainingRecord;
import com.team13.context.entity.TrainingReport;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.PaymentMapper;
import com.team13.context.mapper.TrainingRecordMapper;
import com.team13.context.mapper.TrainingReportMapper;
import com.team13.context.metrics.BusinessMetricsRecorder;
import com.team13.context.metrics.MetricKeys;
import com.team13.context.order.PaymentOrderAssertions;
import com.team13.context.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRecordMapper trainingRecordMapper;
    private final TrainingReportMapper trainingReportMapper;
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final AiReportFacade aiReportFacade;
    private final AiReportJsonSupport aiReportJsonSupport;
    private final BusinessMetricsRecorder businessMetricsRecorder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrainingStartResponse startSessionByOrderId(Long orderId) {
        Long currentUserId = UserContext.requireUserId();
        if (orderId == null) {
            businessMetricsRecorder.increment(MetricKeys.TRAINING_START_REJECT);
            throw new IllegalArgumentException("orderId 不能为空");
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            businessMetricsRecorder.increment(MetricKeys.TRAINING_START_REJECT);
            throw new IllegalArgumentException("订单不存在，无法开始训练");
        }
        if (!order.getUserId().equals(currentUserId)) {
            businessMetricsRecorder.increment(MetricKeys.TRAINING_START_REJECT);
            throw new IllegalArgumentException("无权操作该订单");
        }
        if (!BusinessStatuses.Order.PAID.equals(order.getStatus())
                && !BusinessStatuses.Order.IN_SERVICE.equals(order.getStatus())) {
            businessMetricsRecorder.increment(MetricKeys.TRAINING_START_REJECT);
            throw new IllegalArgumentException("订单未支付或状态不允许开始训练，当前状态: " + order.getStatus());
        }

        Payment payment = paymentMapper.selectOne(
                Wrappers.<Payment>lambdaQuery()
                        .eq(Payment::getOrderId, orderId)
                        .eq(Payment::getStatus, BusinessStatuses.Payment.SUCCESS)
                        .last("LIMIT 1"));
        if (payment == null) {
            businessMetricsRecorder.increment(MetricKeys.TRAINING_START_REJECT);
            throw new IllegalArgumentException("订单缺少成功支付记录，无法开始训练");
        }
        try {
            PaymentOrderAssertions.assertPaymentMatchesOrder(order, payment);
        } catch (IllegalArgumentException e) {
            businessMetricsRecorder.increment(MetricKeys.TRAINING_START_REJECT);
            throw e;
        }

        TrainingRecord inProgress = trainingRecordMapper.selectOne(
                Wrappers.<TrainingRecord>lambdaQuery()
                        .eq(TrainingRecord::getOrderId, orderId)
                        .eq(TrainingRecord::getStatus, TrainingStatuses.IN_PROGRESS)
                        .orderByDesc(TrainingRecord::getCreatedAt)
                        .last("LIMIT 1"));
        if (inProgress != null) {
            businessMetricsRecorder.increment(MetricKeys.TRAINING_START_SUCCESS);
            return TrainingStartResponse.builder()
                    .roomId(inProgress.getRoomId())
                    .trainingId(inProgress.getId())
                    .orderId(orderId)
                    .startedAt(inProgress.getStartedAt())
                    .build();
        }

        LocalDateTime orderNow = LocalDateTime.now();
        order.setStatus(BusinessStatuses.Order.IN_SERVICE);
        order.setUpdatedAt(orderNow);
        orderMapper.updateById(order);

        String roomId = IdWorker.get32UUID();
        TrainingRecord record = new TrainingRecord();
        record.setUserId(order.getUserId());
        record.setOrderId(orderId);
        record.setRoomId(roomId);
        record.setStatus(TrainingStatuses.IN_PROGRESS);
        record.setStartedAt(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        trainingRecordMapper.insert(record);

        businessMetricsRecorder.increment(MetricKeys.TRAINING_START_SUCCESS);
        return TrainingStartResponse.builder()
                .roomId(roomId)
                .trainingId(record.getId())
                .orderId(orderId)
                .startedAt(record.getStartedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrainingStartResponse startTraining(TrainingStartRequest request) {
        return startSessionByOrderId(request.getOrderId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String endTraining(TrainingEndRequest request) {
        Long currentUserId = UserContext.requireUserId();
        TrainingRecord record = trainingRecordMapper.selectOne(
                Wrappers.<TrainingRecord>lambdaQuery()
                        .eq(TrainingRecord::getRoomId, request.getRoomId())
                        .last("LIMIT 1"));
        if (record == null) {
            return null;
        }
        if (!canEndTraining(record, currentUserId)) {
            throw new ForbiddenOperationException("无权结束该训练");
        }
        if (TrainingStatuses.ENDED.equals(record.getStatus())
                || TrainingStatuses.REPORT_READY.equals(record.getStatus())) {
            TrainingReport existingReport = trainingReportMapper.selectOne(
                    Wrappers.<TrainingReport>lambdaQuery()
                            .eq(TrainingReport::getTrainingId, record.getId())
                            .last("LIMIT 1"));
            if (existingReport != null && existingReport.getCoachFeedback() != null) {
                return existingReport.getCoachFeedback();
            }
            return null;
        }

        LocalDateTime endedAt = LocalDateTime.now();
        record.setStatus(TrainingStatuses.ENDED);
        record.setEndedAt(endedAt);
        record.setUpdatedAt(endedAt);
        trainingRecordMapper.updateById(record);

        if (record.getStartedAt() != null) {
            long seconds = Duration.between(record.getStartedAt(), endedAt).getSeconds();
            if (seconds > 0) {
                businessMetricsRecorder.addLong(MetricKeys.TRAINING_DURATION_SECONDS_SUM, seconds);
            }
        }
        businessMetricsRecorder.increment(MetricKeys.TRAINING_END_SUCCESS);

        Long linkedOrderId = record.getOrderId();
        if (linkedOrderId != null) {
            Order order = orderMapper.selectById(linkedOrderId);
            if (order != null && BusinessStatuses.Order.IN_SERVICE.equals(order.getStatus())) {
                LocalDateTime now = LocalDateTime.now();
                order.setStatus(BusinessStatuses.Order.COMPLETED);
                order.setUpdatedAt(now);
                orderMapper.updateById(order);
            }
        }

        String reportJson = generateAndPersistReport(record, buildSessionSummary(request));
        record.setStatus(TrainingStatuses.REPORT_READY);
        record.setUpdatedAt(LocalDateTime.now());
        trainingRecordMapper.updateById(record);
        return reportJson;
    }

    @Override
    public String generateAiReport(AiTrainingReportRequest request) {
        Long currentUserId = UserContext.requireUserId();
        TrainingRecord record = trainingRecordMapper.selectById(request.getTrainingRecordId());
        if (record == null) {
            throw new IllegalArgumentException("训练记录不存在");
        }
        if (!record.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("无权查看该训练记录");
        }

        TrainingReport existing = trainingReportMapper.selectOne(
                Wrappers.<TrainingReport>lambdaQuery()
                        .eq(TrainingReport::getTrainingId, record.getId())
                        .last("LIMIT 1"));
        if (existing != null && existing.getCoachFeedback() != null) {
            return existing.getCoachFeedback();
        }
        return generateAndPersistReport(record, null);
    }

    private boolean canEndTraining(TrainingRecord record, Long currentUserId) {
        if (Objects.equals(record.getUserId(), currentUserId)) {
            return true;
        }
        if (record.getOrderId() == null) {
            return false;
        }
        Order order = orderMapper.selectById(record.getOrderId());
        return order != null && Objects.equals(order.getCoachId(), currentUserId);
    }

    private String generateAndPersistReport(TrainingRecord record, String sessionSummary) {
        String rawJson = aiReportFacade.generateTrainingReport(record, sessionSummary);
        AiReportJsonSupport.ParsedAiReport parsed = aiReportJsonSupport.parse(rawJson);
        LocalDateTime now = LocalDateTime.now();

        TrainingReport report = trainingReportMapper.selectOne(
                Wrappers.<TrainingReport>lambdaQuery()
                        .eq(TrainingReport::getTrainingId, record.getId())
                        .last("LIMIT 1"));
        if (report == null) {
            report = new TrainingReport();
            report.setTrainingId(record.getId());
            report.setCreatedAt(now);
        }
        report.setScoreLogic(parsed.scoreLogic());
        report.setScoreFluency(parsed.scoreFluency());
        report.setScorePressure(parsed.scorePressure());
        report.setHighlights(parsed.highlightsJson());
        report.setCoachFeedback(parsed.coachFeedbackJson());
        report.setUpdatedAt(now);

        if (report.getId() == null) {
            trainingReportMapper.insert(report);
        } else {
            trainingReportMapper.updateById(report);
        }
        return parsed.coachFeedbackJson();
    }

    private static String buildSessionSummary(TrainingEndRequest request) {
        String transcript = request.getTranscript();
        if (request.getSceneName() != null && !request.getSceneName().isBlank()) {
            return "场景：" + request.getSceneName().trim() + "\n表达内容："
                    + (transcript == null ? "" : transcript.trim());
        }
        return transcript;
    }
}
