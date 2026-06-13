package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderSummaryResponse {

    private Long orderId;
    private String status;
    private BigDecimal amount;
    private Long coachId;
    private String coachName;
    private String coachAvatar;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long sceneId;
    private String sceneName;
    private String trainingGoal;

    /** 学员背景摘要（身份/机构等） */
    private String userBackground;

    private Long trainingId;

    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private String roomId;
    private String trainingStatus;
    private LocalDateTime trainingStartedAt;
    private Boolean canEnterRoom;
    private String enterDeniedReason;

    /** 待支付截止时间（epoch 毫秒），仅 PENDING_PAY 时有值 */
    private Long payExpireAt;

    /** 是否已提交训练评价 */
    private Boolean hasRated;

    /** 是否可进入评价页（训练结束且未评价） */
    private Boolean canReview;

    /** AI/训练报告是否已生成 */
    private Boolean reportReady;

    /** 陪练是否待提交课后反馈 */
    private Boolean coachFeedbackPending;
}
