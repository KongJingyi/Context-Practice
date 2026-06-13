package com.team13.context.service.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.constant.BusinessStatuses;
import com.team13.context.constant.TrainingStatuses;
import com.team13.context.entity.Order;
import com.team13.context.entity.Rating;
import com.team13.context.entity.TrainingRecord;
import com.team13.context.entity.TrainingReport;
import com.team13.context.mapper.RatingMapper;
import com.team13.context.mapper.TrainingReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewFlowSupport {

    private final RatingMapper ratingMapper;
    private final TrainingReportMapper trainingReportMapper;

    public boolean hasRated(Long orderId) {
        if (orderId == null) {
            return false;
        }
        Long count = ratingMapper.selectCount(
                Wrappers.<Rating>lambdaQuery()
                        .eq(Rating::getOrderId, orderId)
                        .eq(Rating::getStatus, 1));
        return count != null && count > 0;
    }

    public boolean isReportReady(Order order, TrainingRecord training) {
        if (order == null || training == null) {
            return false;
        }
        if (!TrainingStatuses.REPORT_READY.equals(training.getStatus())
                && !TrainingStatuses.ENDED.equals(training.getStatus())) {
            return false;
        }
        TrainingReport report = trainingReportMapper.selectOne(
                Wrappers.<TrainingReport>lambdaQuery().eq(TrainingReport::getTrainingId, training.getId()));
        return report != null;
    }

    public boolean canReview(Order order, TrainingRecord training) {
        if (order == null || training == null) {
            return false;
        }
        if (!BusinessStatuses.Order.COMPLETED.equals(order.getStatus())) {
            return false;
        }
        if (!isReportReady(order, training)) {
            return false;
        }
        return !hasRated(order.getId());
    }

    /** 陪练是否仍需提交课后五维反馈（AI 初稿已生成但人工评分未落库）。 */
    public boolean needsCoachFeedback(Order order, TrainingRecord training) {
        if (order == null || training == null) {
            return false;
        }
        if (!BusinessStatuses.Order.COMPLETED.equals(order.getStatus())
                && !BusinessStatuses.Order.IN_SERVICE.equals(order.getStatus())) {
            return false;
        }
        if (!TrainingStatuses.ENDED.equals(training.getStatus())
                && !TrainingStatuses.REPORT_READY.equals(training.getStatus())) {
            return false;
        }
        TrainingReport report = trainingReportMapper.selectOne(
                Wrappers.<TrainingReport>lambdaQuery().eq(TrainingReport::getTrainingId, training.getId()));
        if (report == null) {
            return true;
        }
        return report.getScoreContent() == null || report.getScoreTime() == null;
    }
}
