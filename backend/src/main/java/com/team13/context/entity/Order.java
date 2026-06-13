package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 陪练订单，对应 {@code ctx_order}。
 */
@Data
@TableName("ctx_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long productId;

    private Long coachId;

    private Long sceneId;

    private LocalDateTime scheduledStart;

    private LocalDateTime scheduledEnd;

    private BigDecimal amount;

    /**
     * PENDING_PAY / PAID / CANCELLED / IN_SERVICE / COMPLETED / REFUNDING / REFUNDED
     */
    private String status;

    private BigDecimal platformFeeRate;

    private BigDecimal platformFeeAmount;

    private BigDecimal coachIncomeAmount;

    private LocalDateTime payAt;

    private LocalDateTime cancelAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
