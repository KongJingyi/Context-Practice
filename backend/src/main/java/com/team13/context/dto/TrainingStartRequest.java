package com.team13.context.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开始训练请求：用户身份由 JWT / {@link com.team13.context.common.UserContext} 提供。
 */
@Data
public class TrainingStartRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    private String scenarioCode;
}
