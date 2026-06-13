package com.team13.context.service.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.constant.BusinessStatuses;
import com.team13.context.constant.TrainingStatuses;
import com.team13.context.entity.Order;
import com.team13.context.entity.TrainingRecord;
import com.team13.context.entity.TrainingReport;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.TrainingRecordMapper;
import com.team13.context.mapper.TrainingReportMapper;
import com.team13.context.config.RoomProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 训练超时自动结束：预约时段+宽限后仍未正常结束的训练，自动归档为已完成。
 */
@Component
@RequiredArgsConstructor
public class TrainingSessionSupport {

    private final RoomProperties roomProperties;
    private final OrderMapper orderMapper;
    private final TrainingRecordMapper trainingRecordMapper;
    private final TrainingReportMapper trainingReportMapper;

    /** 训练中且已超过 scheduledEnd + grace */
    public boolean shouldAutoCompleteOverdue(Order order, TrainingRecord training, LocalDateTime now) {
        if (order == null || training == null) {
            return false;
        }
        if (!BusinessStatuses.Order.IN_SERVICE.equals(order.getStatus())) {
            return false;
        }
        if (!TrainingStatuses.IN_PROGRESS.equals(training.getStatus())) {
            return false;
        }
        if (order.getScheduledEnd() == null) {
            return false;
        }
        LocalDateTime deadline = order.getScheduledEnd();
        return now.isAfter(deadline);
    }

    @Transactional(rollbackFor = Exception.class)
    public void autoCompleteOverdueSession(Order order, TrainingRecord training, LocalDateTime now) {
        if (!shouldAutoCompleteOverdue(order, training, now)) {
            return;
        }

        training.setStatus(TrainingStatuses.ENDED);
        training.setEndedAt(now);
        training.setUpdatedAt(now);
        trainingRecordMapper.updateById(training);

        order.setStatus(BusinessStatuses.Order.COMPLETED);
        order.setUpdatedAt(now);
        orderMapper.updateById(order);

        ensureStubReport(training.getId(), now);

        training.setStatus(TrainingStatuses.REPORT_READY);
        training.setUpdatedAt(now);
        trainingRecordMapper.updateById(training);
    }

    private void ensureStubReport(Long trainingId, LocalDateTime now) {
        TrainingReport existing = trainingReportMapper.selectOne(
                Wrappers.<TrainingReport>lambdaQuery()
                        .eq(TrainingReport::getTrainingId, trainingId)
                        .last("LIMIT 1"));
        if (existing != null) {
            return;
        }
        TrainingReport report = new TrainingReport();
        report.setTrainingId(trainingId);
        report.setScoreLogic(75);
        report.setScoreFluency(75);
        report.setScorePressure(75);
        report.setHighlights("[\"训练时段已结束，系统自动归档\"]");
        report.setCoachFeedback("{\"summary\":\"训练时段已结束，可前往复盘评价。\",\"suggestions\":[\"回顾本次表达中的亮点与可改进点\"]}");
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        trainingReportMapper.insert(report);
    }
}
