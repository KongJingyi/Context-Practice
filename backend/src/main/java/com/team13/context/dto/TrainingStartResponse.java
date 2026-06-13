package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TrainingStartResponse {

    private String roomId;
    private Long trainingId;
    private Long orderId;
    private LocalDateTime startedAt;
}
