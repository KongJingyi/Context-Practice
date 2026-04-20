package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 陪练预约 / 订单（支付与履约状态由业务枚举扩展）。
 */
@Data
@TableName("ctx_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 预约的陪练产品或套餐 ID */
    private Long productId;

    /** 预约开始时间 */
    private LocalDateTime scheduledStart;

    /** 订单金额 */
    private BigDecimal amount;

    /** 订单状态：待支付、已支付、已取消、已完成等 */
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
