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

        if (order.getScheduledStart() != null) {
            LocalDateTime earliest = order.getScheduledStart().minusMinutes(roomProperties.getEarlyEnterMinutes());
            if (now.isBefore(earliest)) {
                return new EnterDecision(false, "请在预约开始前 " + roomProperties.getEarlyEnterMinutes() + " 分钟内进入");
            }
        }
        if (order.getScheduledEnd() != null) {
            LocalDateTime latest = order.getScheduledEnd().plusMinutes(roomProperties.getGraceAfterEndMinutes());
            if (now.isAfter(latest)) {
                return new EnterDecision(false, "已超过训练结束时间");
            }
        }
        if (training == null && BusinessStatuses.Order.PAID.equals(status)) {
            return new EnterDecision(true, null);
        }
        return new EnterDecision(true, null);
    }

    public boolean canShowEnterButton(Order order, TrainingRecord training, LocalDateTime now) {
        return evaluateEnter(order, training, now).canEnter();
    }
}
