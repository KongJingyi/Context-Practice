package com.team13.context.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 结束训练请求：用户身份由 JWT / {@link com.team13.context.common.UserContext} 提供。
 */
@Data
public class TrainingEndRequest {

    @NotBlank
    private String roomId;

    /** 用户临场表达文本（语音转写或手动输入），供大模型分析 */
    private String transcript;

    /** 可选：覆盖场景名称，不传则根据订单关联场景解析 */
    private String sceneName;
}
