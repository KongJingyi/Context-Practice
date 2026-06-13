package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单支付记录，与 {@code ctx_payment} 一一对应（每单一条，见唯一索引 uk_ctx_payment_order）。
 */
@Data
@TableName("ctx_payment")
public class Payment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    /** WECHAT / ALIPAY / COUPON / MOCK 等 */
    private String channel;

    private String tradeNo;

    private BigDecimal amount;

    /** INIT / SUCCESS / FAILED / CLOSED */
    private String status;

    private LocalDateTime paidAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
