package com.team13.context.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开始训练请求体占位。
 */
@Data
public class TrainingStartRequest {

    @NotNull
    private Long userId;

    /** 可选：关联预约订单 */
    private Long orderId;

    @NotBlank
    private String scenarioCode;
}
