package com.team13.context.service.support;

import com.team13.context.config.RoomProperties;
import com.team13.context.constant.BusinessStatuses;
import com.team13.context.constant.TrainingStatuses;
import com.team13.context.entity.Order;
import com.team13.context.entity.TrainingRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 订单展示态与生命周期：进房窗口、失效、取消、角标文案。
 */
@Component
@RequiredArgsConstructor
public class OrderLifecycleSupport {

    private final RoomProperties roomProperties;

    public record OrderDisplayState(
            String ribbonLabel,
            String displayPhase,
            boolean canCancel,
            boolean canRefund,
            boolean expired,
            boolean sessionEnded) {
    }

    /** 已支付但未进房，且预约结束时间已过 → 标记失效 */
    public boolean shouldExpireNoShow(Order order, TrainingRecord training, LocalDateTime now) {
        if (order == null || !BusinessStatuses.Order.PAID.equals(order.getStatus())) {
            return false;
        }
        if (training != null) {
            return false;
        }
        return order.getScheduledEnd() != null && now.isAfter(order.getScheduledEnd());
    }

    /** 训练已开始但已超过预约结束+宽限（含已自动归档为 COMPLETED 的订单） */
    public boolean isSessionEnded(Order order, TrainingRecord training, LocalDateTime now) {
        if (order == null || training == null || order.getScheduledEnd() == null) {
            return false;
        }
        if (!TrainingStatuses.REPORT_READY.equals(training.getStatus())
                && !TrainingStatuses.ENDED.equals(training.getStatus())
                && !TrainingStatuses.IN_PROGRESS.equals(training.getStatus())) {
            return false;
        }
        LocalDateTime deadline = order.getScheduledEnd();
        if (!now.isAfter(deadline)) {
            return false;
        }
        return BusinessStatuses.Order.COMPLETED.equals(order.getStatus())
                || BusinessStatuses.Order.IN_SERVICE.equals(order.getStatus());
    }

    public OrderDisplayState resolveDisplay(Order order, TrainingRecord training, LocalDateTime now) {
        if (order == null) {
            return new OrderDisplayState("", "UNKNOWN", false, false, false, false);
        }
        String status = order.getStatus();
        boolean expired = BusinessStatuses.Order.EXPIRED.equals(status)
                || shouldExpireNoShow(order, training, now);

        if (BusinessStatuses.Order.PENDING_PAY.equals(status)) {
            return new OrderDisplayState("待支付", "PENDING_PAY", true, false, false, false);
        }
        if (BusinessStatuses.Order.CANCELLED.equals(status)) {
            return new OrderDisplayState("已取消", "CANCELLED", false, false, false, false);
        }
        if (BusinessStatuses.Order.REFUNDING.equals(status)) {
            return new OrderDisplayState("退款中", "REFUNDING", false, false, false, false);
        }
        if (BusinessStatuses.Order.REFUNDED.equals(status)) {
            return new OrderDisplayState("已退款", "REFUNDED", false, false, false, false);
        }
        if (expired) {
            return new OrderDisplayState("已失效", "EXPIRED", false, false, true, false);
        }
        if (isSessionEnded(order, training, now)) {
            return new OrderDisplayState("已结束", "SESSION_ENDED", false, false, false, true);
        }
        if (BusinessStatuses.Order.COMPLETED.equals(status)) {
            return new OrderDisplayState("已完成", "COMPLETED", false, false, false, false);
        }
        if (BusinessStatuses.Order.IN_SERVICE.equals(status)
                || (training != null && TrainingStatuses.IN_PROGRESS.equals(training.getStatus()))) {
            return new OrderDisplayState("训练中", "IN_TRAINING", false, false, false, false);
        }
        if (BusinessStatuses.Order.PAID.equals(status)) {
            LocalDateTime start = order.getScheduledStart();
            LocalDateTime end = order.getScheduledEnd();
            if (start != null) {
                LocalDateTime earliest = start.minusMinutes(roomProperties.getEarlyEnterMinutes());
                if (now.isBefore(earliest)) {
                    return new OrderDisplayState("即将开始", "UPCOMING", true, true, false, false);
                }
                if (end != null && !now.isAfter(end)) {
                    return new OrderDisplayState("可进入", "ENTERABLE", true, true, false, false);
                }
            }
            return new OrderDisplayState("已失效", "EXPIRED", false, false, true, false);
        }
        return new OrderDisplayState("", status, false, false, false, false);
    }

    public boolean canCancel(Order order, TrainingRecord training, LocalDateTime now) {
        return resolveDisplay(order, training, now).canCancel();
    }

    public boolean canRefund(Order order, TrainingRecord training, LocalDateTime now) {
        return resolveDisplay(order, training, now).canRefund();
    }

    /** 进房窗口：预约开始前 N 分钟至预约结束；训练中可宽限至 scheduledEnd + grace */
    public boolean isWithinEnterWindow(Order order, TrainingRecord training, LocalDateTime now) {
        if (order == null || order.getScheduledStart() == null) {
            return true;
        }
        LocalDateTime earliest = order.getScheduledStart().minusMinutes(roomProperties.getEarlyEnterMinutes());
        if (now.isBefore(earliest)) {
            return false;
        }
        LocalDateTime latest = order.getScheduledEnd();
        if (latest == null) {
            return true;
        }
        if (training != null && TrainingStatuses.IN_PROGRESS.equals(training.getStatus())) {
            latest = latest.plusMinutes(roomProperties.getGraceAfterEndMinutes());
        }
        return !now.isAfter(latest);
    }
}
