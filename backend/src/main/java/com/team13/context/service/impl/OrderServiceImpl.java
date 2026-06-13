package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team13.context.common.ForbiddenOperationException;
import com.team13.context.common.ResourceNotFoundException;
import com.team13.context.common.UserContext;
import com.team13.context.constant.BusinessStatuses;
import com.team13.context.dto.CreateOrderRequest;
import com.team13.context.dto.MockPayRequest;
import com.team13.context.dto.OrderSummaryResponse;
import com.team13.context.entity.CoachProfile;
import com.team13.context.entity.Order;
import com.team13.context.entity.Payment;
import com.team13.context.entity.Refund;
import com.team13.context.entity.Scene;
import com.team13.context.entity.TrainingRecord;
import com.team13.context.entity.UserProfile;
import com.team13.context.mapper.CoachProfileMapper;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.PaymentMapper;
import com.team13.context.mapper.RefundMapper;
import com.team13.context.mapper.SceneMapper;
import com.team13.context.mapper.TrainingRecordMapper;
import com.team13.context.mapper.UserProfileMapper;
import com.team13.context.metrics.BusinessMetricsRecorder;
import com.team13.context.metrics.MetricKeys;
import com.team13.context.order.PaymentOrderAssertions;
import com.team13.context.service.OrderService;
import com.team13.context.service.support.OrderBookingSupport;
import com.team13.context.service.support.OrderScheduleSupport;
import com.team13.context.service.support.ReviewFlowSupport;
import com.team13.context.service.support.RoomAccessHelper;
import com.team13.context.service.support.UserDisplayHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final TrainingRecordMapper trainingRecordMapper;
    private final SceneMapper sceneMapper;
    private final UserProfileMapper userProfileMapper;
    private final CoachProfileMapper coachProfileMapper;
    private final RefundMapper refundMapper;
    private final BusinessMetricsRecorder businessMetricsRecorder;
    private final RoomAccessHelper roomAccessHelper;
    private final UserDisplayHelper userDisplayHelper;
    private final OrderBookingSupport orderBookingSupport;
    private final ReviewFlowSupport reviewFlowSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPendingOrder(CreateOrderRequest request) {
        Long currentUserId = UserContext.requireUserId();
        LocalDateTime now = LocalDateTime.now();
        Long coachId = request.getCoachId();
        if (coachId == null) {
            CoachProfile coach = coachProfileMapper.selectOne(
                    Wrappers.<CoachProfile>lambdaQuery()
                            .eq(CoachProfile::getStatus, 1)
                            .orderByAsc(CoachProfile::getUserId)
                            .last("LIMIT 1"));
            if (coach != null) {
                coachId = coach.getUserId();
            }
        }
        boolean bookingFlow = isBookingFlow(request);
        if (bookingFlow && coachId != null) {
            orderBookingSupport.requireActiveCoach(coachId);
        }
        OrderScheduleSupport.Window schedule = orderBookingSupport.resolveSchedule(request);
        if (bookingFlow && coachId != null) {
            orderBookingSupport.assertScheduleBookable(coachId, schedule, null);
        }

        BigDecimal amount = request.getAmount();
        if (bookingFlow && coachId != null) {
            amount = orderBookingSupport.resolvePayableAmount(
                    coachId, request.getProductId(), request.getAmount(), request.getCouponId());
        }

        Order order = new Order();
        order.setUserId(currentUserId);
        order.setProductId(request.getProductId());
        order.setCoachId(coachId);
        order.setSceneId(request.getSceneId());
        order.setAmount(amount);
        order.setScheduledStart(schedule.start());
        order.setScheduledEnd(schedule.end());
        order.setStatus(BusinessStatuses.Order.PENDING_PAY);
        order.setPlatformFeeRate(BigDecimal.ZERO);
        order.setPlatformFeeAmount(BigDecimal.ZERO);
        order.setCoachIncomeAmount(BigDecimal.ZERO);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        orderMapper.insert(order);
        businessMetricsRecorder.increment(MetricKeys.ORDER_CREATE_SUCCESS);
        return order.getId();
    }

    private static boolean isBookingFlow(CreateOrderRequest request) {
        return StringUtils.hasText(request.getSlotId())
                || StringUtils.hasText(request.getDate())
                || request.getScheduledStart() != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mockPay(MockPayRequest request) {
        Long currentUserId = UserContext.requireUserId();
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            businessMetricsRecorder.increment(MetricKeys.PAYMENT_FAIL);
            throw new IllegalArgumentException("订单不存在");
        }
        if (!order.getUserId().equals(currentUserId)) {
            businessMetricsRecorder.increment(MetricKeys.PAYMENT_FAIL);
            throw new IllegalArgumentException("无权支付该订单");
        }
        if (BusinessStatuses.Order.PAID.equals(order.getStatus())
                || BusinessStatuses.Order.IN_SERVICE.equals(order.getStatus())
                || BusinessStatuses.Order.COMPLETED.equals(order.getStatus())) {
            Payment existing = paymentMapper.selectOne(
                    Wrappers.<Payment>lambdaQuery().eq(Payment::getOrderId, order.getId()).last("LIMIT 1"));
            if (existing != null && BusinessStatuses.Payment.SUCCESS.equals(existing.getStatus())) {
                try {
                    PaymentOrderAssertions.assertPaymentMatchesOrder(order, existing);
                } catch (IllegalArgumentException e) {
                    businessMetricsRecorder.increment(MetricKeys.PAYMENT_FAIL);
                    throw e;
                }
            }
            return;
        }
        if (!BusinessStatuses.Order.PENDING_PAY.equals(order.getStatus())) {
            businessMetricsRecorder.increment(MetricKeys.PAYMENT_FAIL);
            throw new IllegalArgumentException("订单当前状态不可支付: " + order.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        if (orderBookingSupport.isPayExpired(order, now)) {
            businessMetricsRecorder.increment(MetricKeys.PAYMENT_FAIL);
            throw new IllegalArgumentException("订单已超时未支付，请重新下单");
        }

        Payment payment = paymentMapper.selectOne(
                Wrappers.<Payment>lambdaQuery().eq(Payment::getOrderId, order.getId()).last("LIMIT 1"));

        if (payment == null) {
            Payment row = new Payment();
            row.setOrderId(order.getId());
            row.setChannel(BusinessStatuses.PaymentChannel.MOCK);
            row.setTradeNo("MOCK-" + IdWorker.get32UUID());
            row.setAmount(order.getAmount());
            row.setStatus(BusinessStatuses.Payment.SUCCESS);
            row.setPaidAt(now);
            row.setCreatedAt(now);
            row.setUpdatedAt(now);
            paymentMapper.insert(row);
        } else {
            if (BusinessStatuses.Payment.SUCCESS.equals(payment.getStatus())) {
                try {
                    PaymentOrderAssertions.assertPaymentMatchesOrder(order, payment);
                } catch (IllegalArgumentException e) {
                    businessMetricsRecorder.increment(MetricKeys.PAYMENT_FAIL);
                    throw e;
                }
                order.setStatus(BusinessStatuses.Order.PAID);
                order.setPayAt(payment.getPaidAt() != null ? payment.getPaidAt() : now);
                applyPlatformFee(order);
                order.setUpdatedAt(now);
                orderMapper.updateById(order);
                businessMetricsRecorder.increment(MetricKeys.PAYMENT_SUCCESS);
                return;
            }
            payment.setChannel(BusinessStatuses.PaymentChannel.MOCK);
            if (payment.getTradeNo() == null || payment.getTradeNo().isBlank()) {
                payment.setTradeNo("MOCK-" + IdWorker.get32UUID());
            }
            payment.setAmount(order.getAmount());
            payment.setStatus(BusinessStatuses.Payment.SUCCESS);
            payment.setPaidAt(now);
            payment.setUpdatedAt(now);
            paymentMapper.updateById(payment);
        }

        order.setStatus(BusinessStatuses.Order.PAID);
        order.setPayAt(now);
        applyPlatformFee(order);
        order.setUpdatedAt(now);
        orderMapper.updateById(order);
        businessMetricsRecorder.increment(MetricKeys.PAYMENT_SUCCESS);
    }

    private static void applyPlatformFee(Order order) {
        BigDecimal rate = new BigDecimal("0.15");
        BigDecimal amount = order.getAmount() != null ? order.getAmount() : BigDecimal.ZERO;
        BigDecimal fee = amount.multiply(rate).setScale(2, java.math.RoundingMode.HALF_UP);
        order.setPlatformFeeRate(rate);
        order.setPlatformFeeAmount(fee);
        order.setCoachIncomeAmount(amount.subtract(fee));
    }

    @Override
    public List<OrderSummaryResponse> listHomeReminders(int size) {
        Long currentUserId = UserContext.requireUserId();
        if (size < 1 || size > 10) {
            size = 3;
        }
        Page<Order> orderPage = queryOrderPage(
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getUserId, currentUserId)
                        .in(Order::getStatus,
                                BusinessStatuses.Order.PENDING_PAY,
                                BusinessStatuses.Order.PAID,
                                BusinessStatuses.Order.IN_SERVICE)
                        .orderByDesc(Order::getCreatedAt),
                1,
                50);
        return orderPage.getRecords().stream()
                .map(this::buildOrderSummary)
                .filter(summary -> BusinessStatuses.Order.PENDING_PAY.equals(summary.getStatus())
                        || Boolean.TRUE.equals(summary.getCanEnterRoom()))
                .sorted(Comparator
                        .comparing((OrderSummaryResponse s) -> !Boolean.TRUE.equals(s.getCanEnterRoom()))
                        .thenComparing(
                                OrderSummaryResponse::getScheduledStart,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(size)
                .toList();
    }

    @Override
    public Page<OrderSummaryResponse> listUserOrders(String status, int page, int size) {
        Long currentUserId = UserContext.requireUserId();
        Page<Order> orderPage = queryOrderPage(
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getUserId, currentUserId)
                        .eq(StringUtils.hasText(status), Order::getStatus, status)
                        .orderByDesc(Order::getCreatedAt),
                page,
                size);
        return toSummaryPage(orderPage);
    }

    @Override
    public Page<OrderSummaryResponse> listCoachOrders(String status, int page, int size) {
        Long currentUserId = UserContext.requireUserId();
        Page<Order> orderPage = queryOrderPage(
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getCoachId, currentUserId)
                        .eq(StringUtils.hasText(status), Order::getStatus, status)
                        .orderByDesc(Order::getScheduledStart, Order::getCreatedAt),
                page,
                size);
        return toSummaryPage(orderPage);
    }

    @Override
    public OrderSummaryResponse getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("订单不存在");
        }
        assertCanViewOrder(order);
        return buildOrderSummary(order);
    }

    private Page<Order> queryOrderPage(
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order> wrapper, int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 10;
        }
        return orderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    private Page<OrderSummaryResponse> toSummaryPage(Page<Order> orderPage) {
        List<OrderSummaryResponse> records = orderPage.getRecords().stream()
                .map(this::buildOrderSummary)
                .toList();
        Page<OrderSummaryResponse> result = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        result.setRecords(records);
        return result;
    }

    private OrderSummaryResponse buildOrderSummary(Order order) {
        TrainingRecord training = findLatestTraining(order.getId());
        LocalDateTime now = LocalDateTime.now();
        RoomAccessHelper.EnterDecision decision = roomAccessHelper.evaluateEnter(order, training, now);
        String sceneName = resolveSceneName(order.getSceneId());
        String trainingGoal = resolveTrainingGoal(order.getUserId());
        boolean hasRated = reviewFlowSupport.hasRated(order.getId());
        boolean reportReady = reviewFlowSupport.isReportReady(order, training);
        boolean coachFeedbackPending = reviewFlowSupport.needsCoachFeedback(order, training);

        return OrderSummaryResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .amount(order.getAmount())
                .coachId(order.getCoachId())
                .coachName(order.getCoachId() != null ? userDisplayHelper.resolveNickname(order.getCoachId(), true) : null)
                .coachAvatar(order.getCoachId() != null ? userDisplayHelper.resolveAvatar(order.getCoachId(), true) : null)
                .userId(order.getUserId())
                .userName(userDisplayHelper.resolveNickname(order.getUserId(), false))
                .userAvatar(userDisplayHelper.resolveAvatar(order.getUserId(), false))
                .sceneId(order.getSceneId())
                .sceneName(sceneName)
                .trainingGoal(trainingGoal)
                .userBackground(resolveUserBackground(order.getUserId()))
                .trainingId(training != null ? training.getId() : null)
                .scheduledStart(order.getScheduledStart())
                .scheduledEnd(order.getScheduledEnd())
                .roomId(training != null ? training.getRoomId() : null)
                .trainingStatus(training != null ? training.getStatus() : null)
                .trainingStartedAt(training != null ? training.getStartedAt() : null)
                .canEnterRoom(decision.canEnter())
                .enterDeniedReason(decision.denyReason())
                .payExpireAt(resolvePayExpireAt(order))
                .hasRated(hasRated)
                .canReview(reviewFlowSupport.canReview(order, training))
                .reportReady(reportReady)
                .coachFeedbackPending(coachFeedbackPending)
                .build();
    }

    private Long resolvePayExpireAt(Order order) {
        if (!BusinessStatuses.Order.PENDING_PAY.equals(order.getStatus())) {
            return null;
        }
        LocalDateTime expireAt = orderBookingSupport.payExpireAt(order.getCreatedAt());
        if (expireAt == null) {
            return null;
        }
        return expireAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private TrainingRecord findLatestTraining(Long orderId) {
        if (orderId == null) {
            return null;
        }
        return trainingRecordMapper.selectOne(
                Wrappers.<TrainingRecord>lambdaQuery()
                        .eq(TrainingRecord::getOrderId, orderId)
                        .orderByDesc(TrainingRecord::getCreatedAt)
                        .last("LIMIT 1"));
    }

    private String resolveSceneName(Long sceneId) {
        if (sceneId == null) {
            return null;
        }
        Scene scene = sceneMapper.selectById(sceneId);
        return scene != null ? scene.getName() : null;
    }

    private String resolveTrainingGoal(Long userId) {
        UserProfile profile = userProfileMapper.selectById(userId);
        return profile != null ? profile.getTrainingGoal() : null;
    }

    private String resolveUserBackground(Long userId) {
        UserProfile profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(profile.getIdentityType())) {
            sb.append(profile.getIdentityType().trim());
        }
        if (StringUtils.hasText(profile.getOrganization())) {
            if (!sb.isEmpty()) {
                sb.append(" · ");
            }
            sb.append(profile.getOrganization().trim());
        }
        if (StringUtils.hasText(profile.getRealName()) && sb.isEmpty()) {
            sb.append(profile.getRealName().trim());
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    private void assertCanViewOrder(Order order) {
        Long currentUserId = UserContext.requireUserId();
        boolean isOwner = Objects.equals(order.getUserId(), currentUserId);
        boolean isCoach = Objects.equals(order.getCoachId(), currentUserId);
        if (!isOwner && !isCoach) {
            throw new ForbiddenOperationException("无权查看该订单");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String reason) {
        Long currentUserId = UserContext.requireUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("订单不存在或无权取消");
        }
        if (!BusinessStatuses.Order.PENDING_PAY.equals(order.getStatus())
                && !BusinessStatuses.Order.PAID.equals(order.getStatus())) {
            throw new IllegalArgumentException("当前状态不可取消: " + order.getStatus());
        }
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(BusinessStatuses.Order.CANCELLED);
        order.setCancelAt(now);
        order.setUpdatedAt(now);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long orderId) {
        Long currentUserId = UserContext.requireUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("订单不存在或无权申请退款");
        }
        if (!BusinessStatuses.Order.PAID.equals(order.getStatus())
                && !BusinessStatuses.Order.COMPLETED.equals(order.getStatus())) {
            throw new IllegalArgumentException("当前状态不可退款: " + order.getStatus());
        }
        LocalDateTime now = LocalDateTime.now();
        Refund refund = new Refund();
        refund.setOrderId(orderId);
        refund.setAmount(order.getAmount());
        refund.setReason("用户申请退款");
        refund.setStatus("APPLIED");
        refund.setCreatedAt(now);
        refund.setUpdatedAt(now);
        refundMapper.insert(refund);
        order.setStatus(BusinessStatuses.Order.REFUNDING);
        order.setUpdatedAt(now);
        orderMapper.updateById(order);
    }
}
