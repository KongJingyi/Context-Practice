package com.team13.context.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI 生成训练报告请求体占位。
 */
@Data
public class AiTrainingReportRequest {

    @NotNull
    private Long trainingRecordId;

    @NotBlank
    private String roomId;
}
