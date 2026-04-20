package com.team13.context.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 结束训练请求体占位。
 */
@Data
public class TrainingEndRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String roomId;
}
