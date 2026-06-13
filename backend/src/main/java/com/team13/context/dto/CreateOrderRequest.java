package com.team13.context.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateOrderRequest {

    private Long productId;

    private Long coachId;

    private Long sceneId;

    /** 专家预约时段 ID，格式 YYYY-MM-DD-H，如 2026-06-05-14 */
    private String slotId;

    /** 预约日期 YYYY-MM-DD，与 slotId 二选一或组合使用 */
    private String date;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledStart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledEnd;

    /** 优惠券 ID，服务端校验后计算应付金额 */
    private String couponId;

    @NotNull
    @DecimalMin(value = "0.01", message = "订单金额必须大于 0")
    private BigDecimal amount;
}
