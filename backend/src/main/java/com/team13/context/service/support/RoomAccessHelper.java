package com.team13.context.service.support;

import com.team13.context.config.RoomProperties;
import com.team13.context.constant.BusinessStatuses;
import com.team13.context.constant.TrainingStatuses;
import com.team13.context.entity.Order;
import com.team13.context.entity.TrainingRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RoomAccessHelper {

    private final RoomProperties roomProperties;
    private final OrderLifecycleSupport orderLifecycleSupport;

    public record EnterDecision(boolean canEnter, String denyReason) {
    }

    public EnterDecision evaluateEnter(Order order, TrainingRecord training, LocalDateTime now) {
        if (order == null) {
            return new EnterDecision(false, "订单不存在");
        }
        String status = order.getStatus();
        if (BusinessStatuses.Order.CANCELLED.equals(status)
                || BusinessStatuses.Order.REFUNDING.equals(status)
                || BusinessStatuses.Order.REFUNDED.equals(status)) {
            return new EnterDecision(false, "订单已取消或已退款");
        }
        if (BusinessStatuses.Order.PENDING_PAY.equals(status)) {
            return new EnterDecision(false, "订单未支付，无法进入训练");
        }
        if (BusinessStatuses.Order.EXPIRED.equals(status)
                || orderLifecycleSupport.shouldExpireNoShow(order, training, now)) {
            return new EnterDecision(false, "预约时间已过，订单已失效");
        }
        if (training != null
                && (TrainingStatuses.ENDED.equals(training.getStatus())
                        || TrainingStatuses.REPORT_READY.equals(training.getStatus()))) {
            return new EnterDecision(false, "训练已结束");
        }
        if (!BusinessStatuses.Order.PAID.equals(status)
                && !BusinessStatuses.Order.IN_SERVICE.equals(status)
                && !BusinessStatuses.Order.COMPLETED.equals(status)) {
            return new EnterDecision(false, "订单当前状态不允许进入: " + status);
        }
        if (BusinessStatuses.Order.COMPLETED.equals(status)) {
            return new EnterDecision(false, "训练已结束");
        }

        if (!orderLifecycleSupport.isWithinEnterWindow(order, training, now)) {
            if (order.getScheduledStart() != null) {
                LocalDateTime earliest = order.getScheduledStart().minusMinutes(roomProperties.getEarlyEnterMinutes());
                if (now.isBefore(earliest)) {
                    return new EnterDecision(false, "请在预约开始前 " + roomProperties.getEarlyEnterMinutes() + " 分钟内进入");
                }
            }
            if (order.getScheduledEnd() != null && now.isAfter(order.getScheduledEnd())
                    && (training == null || !TrainingStatuses.IN_PROGRESS.equals(training.getStatus()))) {
                return new EnterDecision(false, "预约时间已过，订单已失效");
            }
            return new EnterDecision(false, "训练已结束");
        }
        return new EnterDecision(true, null);
    }

    public boolean canShowEnterButton(Order order, TrainingRecord training, LocalDateTime now) {
        return evaluateEnter(order, training, now).canEnter();
    }
}
