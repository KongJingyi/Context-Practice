package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ctx_refund")
public class Refund {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private BigDecimal amount;

    private String reason;

    /** APPLIED / APPROVED / REJECTED / REFUNDED */
    private String status;

    private Long decidedBy;

    private LocalDateTime decidedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
